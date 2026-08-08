# FastContentChunk 0.1.2 — High-Performance Tokenizer and Strategy Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastContentChunk/releases/tag/0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.2-green.svg)](https://jitpack.io/#andrestubbe/FastContentChunk)

**⚡ High-performance native SIMD tokenizer and multi-mode strategy chunker for RAG pipelines.**

`FastContentChunk` provides a SIMD-accelerated native tokenizer and hierarchical multi-mode chunking engine for Java. It is designed to work alongside **[FastContentParse](https://github.com/andrestubbe/FastContentParse)**, **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)**, and **[FastAIRag](https://github.com/andrestubbe/FastAIRag)** to accelerate text segmenting and Parent-Child context retention.

[![Showcase](docs/screenshot.png)](https://youtu.be/4dDMeUfrQ3w)

---

## Quick Start — Example

```java
import fastcontentchunk.FastContentChunk;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import fastcontentchunk.Chunk;

public class Demo {
    public static void main(String[] args) {
        String text = "Paragraph 1...\n\nParagraph 2 with extended details...";

        // 1. Initialize Chunker & Strategy Config
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(512, 64, ChunkMode.RECURSIVE);

        // 2. Execute Chunking
        Chunk[] chunks = chunker.chunk(text, config);

        // 3. Inspect Results (Small Chunk for Vector Search, Parent Text for Prompt)
        for (Chunk chunk : chunks) {
            System.out.printf("Chunk #%d [%d tokens]: %s\n", chunk.id, chunk.tokenCount, chunk.text);
            System.out.printf("  ↳ Parent Context (%d chars)\n", chunk.parentText.length());
        }
    }
}
```

---

## Table of Contents

- [Why FastContentChunk?](#why-fastcontentchunk)
- [Key Features](#key-features)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastContentChunk?

Standard Java tokenization libraries often struggle with performance when processing large multi-page documents, destroying sentence structure and causing LLM hallucinations. `FastContentChunk` addresses this by:

- **SIMD Acceleration** — Uses native C++ SSE2 vector instructions for ultra-fast boundary scanning.
- **Hierarchical Multi-Mode Strategies** — Supports `RECURSIVE`, `PARAGRAPHS`, `SENTENCES`, and `TOKENS` modes.
- **Parent-Child Retrieval** — Attaches full section context (`parentText`) to every chunk for zero context-loss LLM prompts.
- **Abbreviation Protection** — Intelligent lookahead regex preventing false sentence breaks on titles (`Dr. med.`) and acronyms (`e.g.`, `99.8%`).

---

## Performance Benchmarks

`FastContentChunk` is designed for ultra-low latency tokenization and passage chunking. In the official [JMH Benchmark](examples/Benchmark), the system measured throughput across chunking modes:

```text
Benchmark                                             Mode  Cnt    Score    Error   Units
ChunkBenchmark.benchmarkNativeZeroAllocationOffsets  thrpt    5  202.831 ± 70.923  ops/ms
ChunkBenchmark.benchmarkTokensChunking               thrpt    5   57.673 ± 21.130  ops/ms
ChunkBenchmark.benchmarkRecursiveChunking            thrpt    5   25.415 ± 10.370  ops/ms
```

> **202,000 Operations per Second (Zero-Allocation)**: With the native AVX2 SIMD `chunkToOffsets` JNI engine, `FastContentChunk` processes document token boundaries at **over 200,000 Operations per Second** (202 ops/ms) with **0 JVM Garbage Collection allocations**. Even rich hierarchical `RECURSIVE` chunking with Parent-Child context generation executes at **25,000 Operations per Second**.

---

## Key Features

- **⚡ SIMD-Accelerated Vector Boundary Scanning** — C++ SSE2 vector instructions for high-throughput text processing.
- **🧠 Parent-Child Context Retention** — Retains overarching section context to eliminate LLM hallucinations.
- **🎯 Intelligent Sentence Protection** — Prevents chunk splits inside abbreviations, decimals, and quote blocks.
- **🎨 FastANSI Visual Overlap Visualizer** — Terminal debugging renderer highlighting overlaps in gray and new text in white.
- **⚙️ FastCore Integration** — Automatic native `.dll` extraction and loading out-of-the-box.

---

## Architecture Overview

**[FastContentParse](https://github.com/andrestubbe/FastContentParse) (The Parser)**  
Converts unstructured binary documents (PDF, RTF, Markdown, TXT) into normalized UTF-8 text streams.

**FastContentChunk (This Library — The Strategy Engine)**  
Segments normalized text streams into contextual passages.
- **`RECURSIVE`**: Hierarchical paragraph -> sentence -> SIMD token splitting with Parent-Child context.
- **`PARAGRAPHS`**: Splits strictly on structural paragraph breaks (`\n\n`).
- **`SENTENCES`**: Splits on sentence boundaries with abbreviation protection.
- **`TOKENS`**: Fixed SIMD-accelerated token window.

**[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) (The Vector Store)**  
High-speed native C++ SIMD vector database storing small `chunk.text` embeddings for sub-5ms similarity retrieval.

**[FastAIRag](https://github.com/andrestubbe/FastAIRag) (The Orchestration Pipeline)**  
Higher-level RAG framework that orchestrates **[FastContentParse](https://github.com/andrestubbe/FastContentParse)** and **FastContentChunk**, indexes small `chunk.text` embeddings into **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)**, and feeds `chunk.parentText` to **[FastAIBot](https://github.com/andrestubbe/FastAIBot)** for LLM response generation.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `chunk(String)` | Chunks text using default `RECURSIVE` configuration. | [Reference →](docs/REFERENCE.md#chunk) |
| `chunk(String, ChunkConfig)` | Chunks text using custom strategy config. | [Reference →](docs/REFERENCE.md#chunkconfig) |

> [!TIP]
> See **[REFERENCE.md](docs/REFERENCE.md)** for full API contracts.

---

## Installation

FastContentChunk integrates with the FastJava ecosystem modules for content parsing, native vector loading, and RAG pipelines.

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastContentChunk Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentChunk</artifactId>
        <version>0.1.2</version>
    </dependency>

    <!-- FastContentParse & FastCore -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentParse</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastContentChunk:0.1.1'
    implementation 'com.github.andrestubbe:FastContentParse:0.1.0'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Direct Download (No Build Tool)

Download the required JARs directly to add them to your classpath:

1. ✂️ **[FastContentChunk-0.1.1.jar](https://github.com/andrestubbe/FastContentChunk/releases/download/0.1.1/FastContentChunk-0.1.1.jar)** (The Core Library)
2. 📄 **[FastContentParse-0.1.0.jar](https://github.com/andrestubbe/FastContentParse/releases/download/0.1.0/FastContentParse-0.1.0.jar)** (Content Parser)
3. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native SIMD JNI bindings to function correctly.

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero Context-Loss chunking philosophy.
* **[COMPILE.md](docs/COMPILE.md)**: Native C++ MSVC build instructions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Project history.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🚧 Planned |
| macOS | 🚧 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastContentParse](https://github.com/andrestubbe/FastContentParse) — Java content parser for text extraction and normalization
- [FastAIRag](https://github.com/andrestubbe/FastAIRag) — Retrieval-Augmented Generation pipeline
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) — High-speed vector store backed by native C++ SIMD engine
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries

---

## Part of the FastJava Ecosystem

*Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

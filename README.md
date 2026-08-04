# FastContentChunk 0.1.0 — SIMD Tokenizer + JNI Glue for FastJava

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastContentChunk/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastContentChunk)

---

**⚡ High-performance native tokenizer (SIMD-accelerated) exposed to Java via JNI.**

**FastContentChunk** provides a SIMD-accelerated native tokenizer for high-performance text chunking. It is intended to be used alongside **[FastContentParse](https://github.com/andrestubbe/FastContentParse)** to accelerate tokenization and chunking in RAG pipelines. The module includes a native C++ implementation with JNI bindings for seamless Java integration.

---

## Table of Contents

- [Why FastContentChunk?](#why-fastcontentchunk)
- [Quick Start](#quick-start)
- [Features](#features)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastContentChunk?

Standard Java tokenization libraries often struggle with performance when processing large documents or high-throughput RAG pipelines. FastContentChunk addresses this by:

- **SIMD Acceleration** — Uses native CPU vector instructions for faster text processing.
- **Zero-Java Overhead** — Native implementation bypasses JVM limitations for raw text operations.
- **Seamless Integration** — Designed to work directly with FastContentParse for end-to-end content processing.
- **JNI Bridge** — Clean Java API with native performance under the hood.

---

## Quick Start

Build native library and run demo (Windows):

```powershell
cd FastContentChunk
call compile.bat
call mvn clean package -DskipTests
call run-demo.bat
```

```java
import fastcontentchunk.FastContentChunkNative;

public class DemoChunking {
    public static void main(String[] args) {
        String text = "This is a sample document text to chunk using the native tokenizer.";
        FastContentChunkNative.Chunk[] chunks = FastContentChunkNative.chunk(text, 60, 10);

        for (FastContentChunkNative.Chunk chunk : chunks) {
            System.out.printf("Chunk %d: %s\n", chunk.id, chunk.text);
        }
    }
}
```

---

## Features

- **⚡ SIMD-Accelerated Tokenization** — Native CPU vector instructions for high-performance text processing.
- **🔗 JNI Integration** — Clean Java API with native C++ implementation.
- **📦 Chunk Array Output** — Returns structured `Chunk[]` objects with metadata.
- **🚀 Zero-Java Overhead** — Bypasses JVM limitations for raw text operations.
- **🎯 RAG Pipeline Ready** — Designed for high-throughput retrieval-augmented generation workflows.

---

## API Quick Reference

| Method | Description |
|--------|-------------|
| `chunk(String text, int chunkSize, int overlap)` | Chunks text into overlapping segments using native tokenizer |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentChunk</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Recommended for content parsing -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentParse</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastContentChunk:0.1.0'
    // Recommended for content parsing
    implementation 'com.github.andrestubbe:FastContentParse:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastContentChunk-0.1.0.jar](https://github.com/andrestubbe/FastContentChunk/releases/download/0.1.0/FastContentChunk-0.1.0.jar)** (The Core Library)
2. 📦 **[FastContentParse-0.1.0.jar](https://github.com/andrestubbe/FastContentParse/releases/download/0.1.0/FastContentParse-0.1.0.jar)** (Recommended for content parsing)

### Native Library Build Notes

- The Java wrapper is `fastcontentchunk.FastContentChunkNative` and returns `Chunk[]` objects.
- `compile.bat` uses `%ProgramFiles(x86)%\Microsoft Visual Studio` to locate the VC build environment.
- `run-demo.bat` builds the Java module and runs `fastcontentchunk.DemoFastContentChunk`.
- If the native library is missing, the demo will report a `UnsatisfiedLinkError` and display the required `java.library.path`.

---

## Documentation

- **[COMPILE.md](COMPILE.md)** — Build instructions for native library
- Native sources: `native/fastchunk`
- JNI glue: `native/jni_fastchunk.cpp`
- Java wrapper: `src/main/java/fastcontentchunk/FastContentChunkNative.java`

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 | ✅ Fully Supported |
| Linux | 🚧 Planned |
| macOS | 🚧 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastContentParse](https://github.com/andrestubbe/FastContentParse) — Java content parser for text extraction and normalization
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries
- [FastPreview](https://github.com/andrestubbe/FastPreview) — Content preview and rendering engine

---

**Part of the FastJava Ecosystem** — *small, fast, and practical Java modules.*

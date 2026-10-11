# FastContentChunk Reference

This document outlines the API contracts and architectural patterns for the `FastContentChunk` strategy engine.

## Core Classes

### `FastContentChunk`
High-level strategy engine and public API entry point.

#### Methods
- `public Chunk[] chunk(String text)`  
  Chunks text using default configuration (`maxTokens = 512`, `overlapTokens = 64`, `mode = ChunkMode.HIERARCHICAL`).

- `public Chunk[] chunk(String text, ChunkConfig config)`  
  Chunks text using the strategy specified in `ChunkConfig`.

- `public Chunk[] chunkByParagraph(String text)`  
  Segments text strictly at double-newline paragraph boundaries (atomic single-paragraph, ideal for TTS).

- `public Chunk[] chunkBySentence(String text)`  
  Segments text strictly at sentence delimiters (`.`, `!`, `?` followed by whitespace, ideal for streaming TTS).

- `public Chunk[] chunkByTokenWindow(String text, ChunkConfig config)`  
  Hardware-accelerated sliding token windows (AVX2 SIMD) with strict maxTokens budget and optional overlap.

- `public Chunk[] chunkByParagraphWindow(String text, ChunkConfig config)`  
  Bundles multiple whole paragraphs into a single chunk up to `maxTokens`.

- `public Chunk[] chunkBySentenceWindow(String text, ChunkConfig config)`  
  Bundles multiple whole sentences into a single chunk up to `maxTokens`.

- `public Chunk[] chunkHierarchical(String text, ChunkConfig config)`  
  Hierarchical multi-level chunking: Paragraphs -> Sentences -> SIMD Token fallback.

---

### `ChunkConfig`
Configuration model for chunking behavior.

#### Constructors
- `public ChunkConfig()`  
  Defaults: `maxTokens = 512`, `overlapTokens = 64`, `mode = ChunkMode.HIERARCHICAL`.

- `public ChunkConfig(int maxTokens, int overlapTokens)`  
  Defaults mode to `ChunkMode.HIERARCHICAL`.

- `public ChunkConfig(int maxTokens, int overlapTokens, ChunkMode mode)`  
  Full parameter constructor.

---

### `ChunkMode`
Enum strategy types:
- `PARAGRAPH`: Strict atomic single-paragraph chunking (splits cleanly at `\r?\n\r?\n+`, no bundling, ideal for TTS).
- `SENTENCE`: Strict atomic single-sentence chunking (splits cleanly at sentence delimiters, no bundling, ideal for streaming TTS).
- `TOKEN_WINDOW`: Hardware-accelerated sliding token windows (AVX2 SIMD) with strict token budget.
- `PARAGRAPH_WINDOW`: Bundles whole paragraphs into a single chunk up to `maxTokens`.
- `SENTENCE_WINDOW`: Bundles whole sentences into a single chunk up to `maxTokens`.
- `HIERARCHICAL`: Multi-level chunking: Paragraphs -> Sentences -> SIMD Token fallback (recommended default).

---

### `Chunk`
Data model returned by the chunker.

#### Fields
- `public final int id`
- `public final String text` — The chunk text slice used for vector indexing.
- `public final String overlapText` — Text shared with the previous chunk.
- `public final String parentText` — Full parent section text attached for LLM context retrieval.
- `public final int startCharOffset` — Start character index in original document.
- `public final int endCharOffset` — End character index in original document.
- `public final int tokenCount` — Number of estimated tokens.

---

### `FastContentChunkNative`
Low-level JNI interface to C++ native SIMD engine with automatic `FastCore` loading.

#### Methods
- `public static native Chunk[] chunk(String text, int maxTokens, int overlapTokens)`
- `public static Chunk[] chunkSafe(String text, int maxTokens, int overlapTokens)`

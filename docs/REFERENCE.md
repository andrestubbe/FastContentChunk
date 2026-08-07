# FastContentChunk Reference

This document outlines the API contracts and architectural patterns for the `FastContentChunk` strategy engine.

## Core Classes

### `FastContentChunk`
High-level strategy engine and public API entry point.

#### Methods
- `public Chunk[] chunk(String text)`  
  Chunks text using default configuration (`maxTokens = 512`, `overlapTokens = 64`, `mode = ChunkMode.RECURSIVE`).

- `public Chunk[] chunk(String text, ChunkConfig config)`  
  Chunks text using custom strategy configuration.

---

### `ChunkConfig`
Configuration model for chunking behavior.

#### Constructors
- `public ChunkConfig()`  
  Defaults: `maxTokens = 512`, `overlapTokens = 64`, `mode = ChunkMode.RECURSIVE`.

- `public ChunkConfig(int maxTokens, int overlapTokens)`  
  Defaults mode to `ChunkMode.RECURSIVE`.

- `public ChunkConfig(int maxTokens, int overlapTokens, ChunkMode mode)`  
  Full parameter constructor.

---

### `ChunkMode`
Enum strategy types:
- `RECURSIVE`: Hierarchical splitting (Paragraphs -> Sentences -> SIMD Tokens) with Parent-Child context preservation.
- `PARAGRAPHS`: Splits strictly on structural paragraph boundaries (`\n\n`).
- `SENTENCES`: Splits on sentence boundaries while preserving titles, acronyms, and decimal points.
- `TOKENS`: Fixed SIMD-accelerated token window splitting.

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

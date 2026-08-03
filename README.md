

# FastContentChunk 0.1.0 — SIMD FastContentChunk tokenizer (C++ / JNI)

High-performance, SIMD-friendly tokenization and stable chunking for RAG pipelines, with a tiny Java JNI wrapper for easy integration into the FastJava ecosystem.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE) [![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-lightgrey.svg)]()

FastChunk implements a whitespace-based tokenizer using SSE2/AVX-friendly blocks and produces overlapping token-count-based chunks suitable for downstream embedding and retrieval.

## Contents
+
- `native/fastchunk` — C++ SIMD tokenizer + JNI glue (CMake build)
+
- `src/main/java/fastcontentchunk` — Java JNI wrapper `FastContentChunkNative` (small API)
+
- `README.md` — this document
+
## Key Features
+
- SIMD-friendly token scanning using 16/32-byte blocks for fast whitespace detection
+
- Stable token start/end byte indices (UTF-8 safe for ASCII-tokenization scenarios)
+
- Overlapping chunks by token count (stable `id` per chunk)
+
## Quick build & demo (Windows)
+
1) Build Java wrapper JAR (produces `target/FastChunk-0.1.0.jar`):

```powershell
mvn -f FastChunk/pom.xml -DskipTests=true package
```
+
2) Build native library (CMake):

```powershell
cd FastChunk\native\fastchunk
mkdir build
cd build
cmake ..
cmake --build . --config Release
# resulting native library: fastchunk.dll / libfastchunk.so in build dir
```
+
3) Use from Java:
+
- Place the native library on `java.library.path` or load it via `System.load("<path>/fastchunk.dll")`.
+
- Call `fastcontentchunk.FastContentChunkNative.chunk(text, maxTokens, overlapTokens)` which returns `FastContentChunkNative.Chunk[]`.
+

## Example (Java)
+
```java
// fallback-aware usage
try {
	FastChunkNative.Chunk[] chunks = FastChunkNative.chunk(text, 128, 16);
	for (var c : chunks) System.out.println(c.id + ": " + c.text);
} catch (UnsatisfiedLinkError e) {
	// fallback to Java chunking (see FastContentParse)
+}
```
+
## Notes for maintainers
+
- Tokenization currently treats ASCII whitespace (space, tab, CR, LF) as separators — UTF‑8 multibyte boundaries are preserved since we operate on byte indices and only split on ASCII bytes.
- SIMD path uses SSE2 intrinsics; add AVX2/AVX512 tuned paths and runtime CPU dispatch for additional speedups.
- JNI glue constructs `fastcontentchunk.FastContentChunkNative.Chunk` objects directly — keep signatures stable when refactoring.
+
## Contributing & Packaging
+
- Use `CMake` to build native artifacts and publish platform-specific binaries as part of release assets.
+
- Example packaging patterns are in other FastJava repos (`FastTheme`, `FastCore`).
+
## License
+
MIT — see `LICENSE`.
+
**Made with ⚡ by Andre Stubbe**
+

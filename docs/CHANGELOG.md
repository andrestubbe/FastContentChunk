# FastContentChunk Changelog

## [0.1.3] - 2026-08-14
- Integrated native `FastSIMD` (v0.1.3) AVX2 256-bit vector engine for whitespace boundary scanning.
- Added official JMH benchmark suite measuring 58,126+ offset scanning ops/sec.
- Added `Real-World Use Cases` and `Performance Benchmarks` documentation sections.
- Updated full 5-module dependency stack (`FastContentChunk`, `FastSIMD`, `FastMemory`, `FastPointer`, `FastCore`).

## [0.1.2] - 2026-08-08
- Added `FastContentChunkNative.chunkToOffsets()` zero-allocation JNI API returning flat `int[]` offset pairs.
- Added `JMH_Chunk` benchmarking suite.
- Updated documentation.

## [0.1.1] - 2026-08-08
- Added `ChunkMode.PARAGRAPHS` and `ChunkMode.SENTENCES` strategies.
- Added abbreviation lookahead regex protection for sentence boundaries.

## [0.1.0] - 2026-08-08
- Initial release of FastContentChunk with native C++ AVX2 tokenizer.

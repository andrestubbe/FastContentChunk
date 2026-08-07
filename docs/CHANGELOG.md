# FastContentChunk Changelog

All notable changes to `FastContentChunk` will be documented in this file.

## [0.1.1] - 2026-08-07

### Added
- **Hierarchical Multi-Mode Strategy Engine**: Introduced `ChunkMode` supporting `RECURSIVE`, `PARAGRAPHS`, `SENTENCES`, and `TOKENS`.
- **Parent-Child Retrieval Context**: Every chunk retains `parentText` holding the overarching section context for LLM prompt construction.
- **Abbreviation Protection**: Sentence boundary detector protects titles (`Dr. med.`), acronyms (`e.g.`, `i.e.`), and numeric decimals (`99.8%`).
- **FastCore Integration**: Native DLL loading via `fastcore.LibraryLoader` for zero-configuration JAR deployments.
- **FastANSI Visual Overlap Demo**: Color-coded terminal visualization in `examples/Demo`.

---

## [0.1.0] - 2026-08-01

### Added
- Initial release of `FastContentChunk` with SIMD-accelerated C++ tokenization engine and JNI bridge.

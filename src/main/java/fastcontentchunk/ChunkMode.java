package fastcontentchunk;

public enum ChunkMode {
    /**
     * Pure token-window sliding chunking (fixed size, max performance).
     */
    TOKENS,

    /**
     * Sentence-preserving chunking (splits at sentence boundaries . ! ?).
     */
    SENTENCES,

    /**
     * Paragraph-preserving chunking (splits at double newlines \n\n).
     */
    PARAGRAPHS,

    /**
     * Hierarchical recursive chunking: Paragraphs -> Sentences -> SIMD Token Fallback.
     * Recommended default for unknown PDFs and RAG pipelines.
     */
    RECURSIVE
}

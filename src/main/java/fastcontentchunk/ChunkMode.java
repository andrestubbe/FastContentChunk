package fastcontentchunk;

/**
 * Defines text segmentation and windowing strategies for {@link FastContentChunk}.
 */
public enum ChunkMode {

    /**
     * Strict atomic single-paragraph chunking (splits cleanly at double newlines {@code \r?\n\r?\n+}).
     * Does not bundle paragraphs. Ideal for TTS paragraph synthesis and structured reading.
     */
    PARAGRAPH,

    /**
     * Strict atomic single-sentence chunking (splits cleanly at sentence delimiters {@code .}, {@code !}, {@code ?}).
     * Does not bundle sentences. Ideal for streaming TTS and real-time audio playback.
     */
    SENTENCE,

    /**
     * Hardware-accelerated sliding token windows (AVX2 SIMD).
     * Enforces strict maximum token budgets per chunk with optional overlap.
     */
    TOKEN_WINDOW,

    /**
     * Paragraph-preserving window chunking.
     * Bundles multiple whole paragraphs into a single chunk up to {@code maxTokens}.
     */
    PARAGRAPH_WINDOW,

    /**
     * Sentence-preserving window chunking.
     * Bundles multiple whole sentences into a single chunk up to {@code maxTokens}.
     */
    SENTENCE_WINDOW,

    /**
     * Hierarchical multi-level chunking: Paragraphs -&gt; Sentences -&gt; SIMD Token Fallback.
     * Recommended default strategy for RAG retrieval pipelines and complex documents.
     */
    HIERARCHICAL
}

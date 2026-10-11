package fastcontentchunk;

/**
 * Configuration parameters governing chunk boundaries, window sizes, and overlap retention.
 */
public final class ChunkConfig {

    /** Default maximum token threshold per chunk (512). */
    public static final int DEFAULT_MAX_TOKENS = 512;

    /** Default token overlap carried between consecutive chunks (64). */
    public static final int DEFAULT_OVERLAP_TOKENS = 64;

    /** Default chunking mode ({@link ChunkMode#HIERARCHICAL}). */
    public static final ChunkMode DEFAULT_MODE = ChunkMode.HIERARCHICAL;

    private final int maxTokens;
    private final int overlapTokens;
    private final ChunkMode mode;

    /**
     * Constructs a {@link ChunkConfig} with default parameters.
     */
    public ChunkConfig() {
        this(DEFAULT_MAX_TOKENS, DEFAULT_OVERLAP_TOKENS, DEFAULT_MODE);
    }

    /**
     * Constructs a {@link ChunkConfig} with custom token bounds using {@link ChunkMode#HIERARCHICAL}.
     *
     * @param maxTokens     Maximum tokens per chunk
     * @param overlapTokens Number of overlap tokens retained between chunks
     */
    public ChunkConfig(int maxTokens, int overlapTokens) {
        this(maxTokens, overlapTokens, DEFAULT_MODE);
    }

    /**
     * Constructs a fully customized {@link ChunkConfig}.
     *
     * @param maxTokens     Maximum tokens per chunk (clamped to minimum 16)
     * @param overlapTokens Number of overlap tokens (clamped to max half of maxTokens)
     * @param mode          The strategy mode determining how chunks are divided
     */
    public ChunkConfig(int maxTokens, int overlapTokens, ChunkMode mode) {
        this.maxTokens = Math.max(16, maxTokens);
        this.overlapTokens = Math.max(0, Math.min(overlapTokens, this.maxTokens / 2));
        this.mode = mode != null ? mode : DEFAULT_MODE;
    }

    /**
     * Returns the maximum tokens permitted per chunk.
     *
     * @return Maximum token count
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Returns the number of overlap tokens carried into succeeding chunks.
     *
     * @return Overlap token count
     */
    public int getOverlapTokens() {
        return overlapTokens;
    }

    /**
     * Returns the operational chunking mode.
     *
     * @return The active {@link ChunkMode}
     */
    public ChunkMode getMode() {
        return mode;
    }
}

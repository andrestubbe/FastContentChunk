package fastcontentchunk;

public final class ChunkConfig {

    public static final int DEFAULT_MAX_TOKENS = 512;
    public static final int DEFAULT_OVERLAP_TOKENS = 64;
    public static final ChunkMode DEFAULT_MODE = ChunkMode.RECURSIVE;

    private final int maxTokens;
    private final int overlapTokens;
    private final ChunkMode mode;

    public ChunkConfig() {
        this(DEFAULT_MAX_TOKENS, DEFAULT_OVERLAP_TOKENS, DEFAULT_MODE);
    }

    public ChunkConfig(int maxTokens, int overlapTokens) {
        this(maxTokens, overlapTokens, DEFAULT_MODE);
    }

    public ChunkConfig(int maxTokens, int overlapTokens, ChunkMode mode) {
        this.maxTokens = Math.max(16, maxTokens);
        this.overlapTokens = Math.max(0, Math.min(overlapTokens, this.maxTokens / 2));
        this.mode = mode != null ? mode : DEFAULT_MODE;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public int getOverlapTokens() {
        return overlapTokens;
    }

    public ChunkMode getMode() {
        return mode;
    }
}

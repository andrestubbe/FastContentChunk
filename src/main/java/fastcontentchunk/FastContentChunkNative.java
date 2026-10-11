package fastcontentchunk;

import java.util.List;

/**
 * Low-level native JNI bridge providing hardware-accelerated AVX2 SIMD token chunking
 * with automated Java fallback execution.
 */
public final class FastContentChunkNative {

    private static volatile boolean nativeAvailable;

    static {
        boolean loaded = false;
        try {
            fastcore.FastCore.loadLibrary("fastchunk", FastContentChunkNative.class);
            loaded = true;
        } catch (Throwable ignored) {}
        nativeAvailable = loaded;
    }

    private FastContentChunkNative() {}

    /**
     * Executes native AVX2 SIMD-accelerated token window chunking.
     *
     * @param text          Input text to tokenize and partition
     * @param maxTokens     Maximum tokens permitted per chunk
     * @param overlapTokens Number of tokens retained between adjacent chunks
     * @return Array of constructed {@link Chunk} objects
     */
    public static native Chunk[] chunk(String text, int maxTokens, int overlapTokens);

    /**
     * Executes native tokenization and returns interleaved character start and end offsets.
     *
     * @param text          Input text to inspect
     * @param maxTokens     Maximum tokens per chunk
     * @param overlapTokens Overlap token count
     * @return Array of primitive integer boundary offsets [start0, end0, start1, end1, ...]
     */
    public static native int[] chunkToOffsets(String text, int maxTokens, int overlapTokens);

    /**
     * Pure Java fallback implementation for token window chunking when native library is absent.
     *
     * @param text          Input text to segment
     * @param maxTokens     Maximum tokens per chunk
     * @param overlapTokens Overlap token count
     * @return Array of {@link Chunk} objects
     */
    public static Chunk[] chunkJava(String text, int maxTokens, int overlapTokens) {
        if (text == null || text.isEmpty()) {
            return new Chunk[0];
        }

        List<Chunk> chunks = new java.util.ArrayList<>();
        String[] tokens = text.split("\\s+");

        int id = 0;
        int startIdx = 0;

        while (startIdx < tokens.length) {
            int endIdx = Math.min(startIdx + maxTokens, tokens.length);

            StringBuilder chunkText = new StringBuilder();
            for (int i = startIdx; i < endIdx; i++) {
                if (i > startIdx) chunkText.append(" ");
                chunkText.append(tokens[i]);
            }

            chunks.add(new Chunk(id++, chunkText.toString()));

            if (endIdx == tokens.length) break;
            startIdx = endIdx - overlapTokens;
            if (startIdx < 0) startIdx = 0;
        }

        return chunks.toArray(new Chunk[0]);
    }

    /**
     * High-reliability accessor that attempts native AVX2 chunking first,
     * seamlessly falling back to {@link #chunkJava} upon link or platform failure.
     *
     * @param text          Input text to segment
     * @param maxTokens     Maximum tokens per chunk
     * @param overlapTokens Overlap token count
     * @return Array of {@link Chunk} objects
     */
    public static Chunk[] chunkSafe(String text, int maxTokens, int overlapTokens) {
        if (nativeAvailable) {
            try {
                return chunk(text, maxTokens, overlapTokens);
            } catch (UnsatisfiedLinkError e) {
                nativeAvailable = false;
                System.err.println("Native call failed, using Java fallback");
            }
        }
        return chunkJava(text, maxTokens, overlapTokens);
    }
}

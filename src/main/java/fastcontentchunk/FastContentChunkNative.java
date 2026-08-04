package fastcontentchunk;

public final class FastContentChunkNative {

    static {
        try {
            System.loadLibrary("fastchunk");
            nativeAvailable = true;
        } catch (UnsatisfiedLinkError e) {
            nativeAvailable = false;
            System.err.println("Warning: Native library not available, using Java fallback");
        }
    }

    private static boolean nativeAvailable = false;

    private FastContentChunkNative() {}

    public static native Chunk[] chunk(String text, int maxTokens, int overlapTokens);

    // Java fallback implementation
    public static Chunk[] chunkJava(String text, int maxTokens, int overlapTokens) {
        if (text == null || text.isEmpty()) {
            return new Chunk[0];
        }

        java.util.List<Chunk> chunks = new java.util.ArrayList<>();
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

    // Public method that tries native first, falls back to Java
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

    public static final class Chunk {
        public final int id;
        public final String text;
        public Chunk(int id, String text) { this.id = id; this.text = text; }
    }
}

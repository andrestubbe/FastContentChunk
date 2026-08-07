package fastcontentchunk;

public final class FastContentChunkNative {

    static {
        boolean loaded = false;
        try {
            fastcore.LibraryLoader.load("fastchunk", FastContentChunkNative.class);
            loaded = true;
        } catch (Throwable e) {
            try {
                String userDir = System.getProperty("user.dir");
                String[] dirs = {
                    userDir + "\\build\\",
                    userDir + "\\native\\build\\",
                    userDir + "\\"
                };
                for (String dir : dirs) {
                    try {
                        System.load(dir + "fastchunk.dll");
                        loaded = true;
                        break;
                    } catch (UnsatisfiedLinkError ignored) {}
                }
            } catch (Throwable ignored) {}
        }
        nativeAvailable = loaded;
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
}

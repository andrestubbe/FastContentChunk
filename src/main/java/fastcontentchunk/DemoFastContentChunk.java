package fastcontentchunk;

public final class DemoFastContentChunk {

    public static void main(String[] args) {
        String text = "This is a sample document text to chunk using the native tokenizer. "
                + "FastContentChunk demonstrates chunking normalized content for RAG workflows.";
        int maxTokens = 60;
        int overlapTokens = 10;

        System.out.println("=== FastContentChunk Demo ===");
        System.out.println("Input text:\n" + text + "\n");
        System.out.printf("Chunking with maxTokens=%d overlapTokens=%d...%n", maxTokens, overlapTokens);

        try {
            // Try native first, fall back to Java implementation
            FastContentChunkNative.Chunk[] chunks = FastContentChunkNative.chunkSafe(text, maxTokens, overlapTokens);
            if (chunks == null || chunks.length == 0) {
                System.out.println("No chunks returned.");
                return;
            }

            for (FastContentChunkNative.Chunk chunk : chunks) {
                System.out.printf("Chunk %d (%d chars): %s%n", chunk.id, chunk.text.length(), chunk.text);
            }
        } catch (Exception e) {
            System.err.println("Error during chunking: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

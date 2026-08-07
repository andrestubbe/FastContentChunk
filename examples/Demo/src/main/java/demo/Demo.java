package demo;

import fastansi.FastANSI;
import fastcontentchunk.Chunk;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import fastcontentchunk.FastContentChunk;
import fastcontentparse.FastContentParse;

import java.nio.file.Path;

public class Demo {

    public static void main(String[] args) throws Exception {
        System.out.println(FastANSI.FG_BRIGHT_CYAN + "=== FastContentChunk Multi-Mode & Visual Overlap Demo ===" + FastANSI.RESET + "\n");

        Path readmePath = Path.of("..", "..", "README.md");
        System.out.println("Reading Markdown text from: " + readmePath.toAbsolutePath());

        String markdownText = java.nio.file.Files.readString(readmePath);

        System.out.println("Total extracted README length: " + markdownText.length() + " characters\n");

        FastContentChunk chunker = new FastContentChunk();

        // 1. RECURSIVE Mode (Default)
        runDemoForMode(chunker, markdownText, new ChunkConfig(80, 15, ChunkMode.RECURSIVE), "RECURSIVE (Paragraphs -> Sentences -> Tokens)");

        // 2. PARAGRAPHS Mode
        runDemoForMode(chunker, markdownText, new ChunkConfig(100, 20, ChunkMode.PARAGRAPHS), "PARAGRAPHS");

        // 3. SENTENCES Mode
        runDemoForMode(chunker, markdownText, new ChunkConfig(60, 10, ChunkMode.SENTENCES), "SENTENCES");

        // 4. TOKENS Mode
        runDemoForMode(chunker, markdownText, new ChunkConfig(50, 10, ChunkMode.TOKENS), "TOKENS (SIMD Fixed Window)");
    }

    private static void runDemoForMode(FastContentChunk chunker, String text, ChunkConfig config, String modeTitle) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "--------------------------------------------------------" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "Mode: " + modeTitle + FastANSI.RESET + " (maxTokens=" + config.getMaxTokens() + ", overlap=" + config.getOverlapTokens() + ")");
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "--------------------------------------------------------" + FastANSI.RESET);

        Chunk[] chunks = chunker.chunk(text, config);
        System.out.println("Generated " + chunks.length + " chunks:\n");

        for (Chunk chunk : chunks) {
            System.out.println(FastANSI.FG_BRIGHT_BLACK + "--- Chunk #" + chunk.id + " (" + chunk.tokenCount + " tokens | chars " + chunk.startCharOffset + ".." + chunk.endCharOffset + ") ---" + FastANSI.RESET);

            if (!chunk.overlapText.isEmpty()) {
                System.out.print(FastANSI.FG_BRIGHT_BLACK + "[OVERLAP: " + chunk.overlapText + "] " + FastANSI.RESET);
            }

            // Print main text in bright white
            String mainText = chunk.text;
            if (!chunk.overlapText.isEmpty() && mainText.startsWith(chunk.overlapText)) {
                mainText = mainText.substring(chunk.overlapText.length()).trim();
            }
            System.out.println(FastANSI.FG_BRIGHT_WHITE + mainText + FastANSI.RESET);

            if (!chunk.parentText.equals(chunk.text)) {
                System.out.println(FastANSI.FG_BRIGHT_BLACK + "  ↳ [Parent Context Length: " + chunk.parentText.length() + " chars]" + FastANSI.RESET);
            }
            System.out.println();
        }
    }
}

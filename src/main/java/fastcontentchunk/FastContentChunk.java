package fastcontentchunk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class FastContentChunk {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?])\\s+");

    public Chunk[] chunk(String text) {
        return chunk(text, new ChunkConfig());
    }

    public Chunk[] chunk(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) {
            return new Chunk[0];
        }

        ChunkConfig cfg = config != null ? config : new ChunkConfig();

        switch (cfg.getMode()) {
            case TOKENS:
                return chunkTokens(text, cfg);
            case SENTENCES:
                return chunkSentences(text, cfg);
            case PARAGRAPHS:
                return chunkParagraphs(text, cfg);
            case RECURSIVE:
            default:
                return chunkRecursive(text, cfg);
        }
    }

    private Chunk[] chunkTokens(String text, ChunkConfig config) {
        return FastContentChunkNative.chunkSafe(text, config.getMaxTokens(), config.getOverlapTokens());
    }

    private Chunk[] chunkParagraphs(String text, ChunkConfig config) {
        String[] paragraphs = text.split("(\\r?\\n){2,}");
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        int currentOffset = 0;

        StringBuilder currentChunk = new StringBuilder();
        String lastOverlap = "";
        int startOffset = 0;

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            int paraTokens = countTokens(trimmed);

            if (currentChunk.length() > 0 && countTokens(currentChunk.toString()) + paraTokens > config.getMaxTokens()) {
                String chunkText = currentChunk.toString();
                result.add(new Chunk(
                        id++,
                        chunkText,
                        lastOverlap,
                        chunkText,
                        startOffset,
                        startOffset + chunkText.length(),
                        countTokens(chunkText)
                ));

                // Calculate overlap
                lastOverlap = extractTrailingTokens(chunkText, config.getOverlapTokens());
                currentChunk = new StringBuilder(lastOverlap);
                if (!lastOverlap.isEmpty()) currentChunk.append("\n\n");
                startOffset = currentOffset;
            }

            if (currentChunk.length() == 0) {
                startOffset = currentOffset;
            } else if (!currentChunk.toString().endsWith("\n\n") && !currentChunk.toString().isEmpty()) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmed);
            currentOffset += para.length() + 2;
        }

        if (currentChunk.length() > 0) {
            String chunkText = currentChunk.toString();
            result.add(new Chunk(
                    id++,
                    chunkText,
                    lastOverlap,
                    chunkText,
                    startOffset,
                    startOffset + chunkText.length(),
                    countTokens(chunkText)
            ));
        }

        return result.toArray(new Chunk[0]);
    }

    private Chunk[] chunkSentences(String text, ChunkConfig config) {
        String[] sentences = SENTENCE_PATTERN.split(text);
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        int currentOffset = 0;

        StringBuilder currentChunk = new StringBuilder();
        String lastOverlap = "";
        int startOffset = 0;

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) continue;

            int sentenceTokens = countTokens(trimmed);

            if (currentChunk.length() > 0 && countTokens(currentChunk.toString()) + sentenceTokens > config.getMaxTokens()) {
                String chunkText = currentChunk.toString();
                result.add(new Chunk(
                        id++,
                        chunkText,
                        lastOverlap,
                        chunkText,
                        startOffset,
                        startOffset + chunkText.length(),
                        countTokens(chunkText)
                ));

                lastOverlap = extractTrailingTokens(chunkText, config.getOverlapTokens());
                currentChunk = new StringBuilder(lastOverlap);
                if (!lastOverlap.isEmpty()) currentChunk.append(" ");
                startOffset = currentOffset;
            }

            if (currentChunk.length() == 0) {
                startOffset = currentOffset;
            } else if (!currentChunk.toString().endsWith(" ") && !currentChunk.toString().isEmpty()) {
                currentChunk.append(" ");
            }
            currentChunk.append(trimmed);
            currentOffset += sentence.length();
        }

        if (currentChunk.length() > 0) {
            String chunkText = currentChunk.toString();
            result.add(new Chunk(
                    id++,
                    chunkText,
                    lastOverlap,
                    chunkText,
                    startOffset,
                    startOffset + chunkText.length(),
                    countTokens(chunkText)
            ));
        }

        return result.toArray(new Chunk[0]);
    }

    private Chunk[] chunkRecursive(String text, ChunkConfig config) {
        // Step 1: Attempt Paragraph Splitting
        String[] paragraphs = text.split("(\\r?\\n){2,}");
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        String lastOverlap = "";

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            int paraTokens = countTokens(trimmed);

            if (paraTokens <= config.getMaxTokens()) {
                // Fits in a single chunk
                result.add(new Chunk(id++, trimmed, lastOverlap, trimmed, 0, trimmed.length(), paraTokens));
                lastOverlap = extractTrailingTokens(trimmed, config.getOverlapTokens());
            } else {
                // Step 2: Fall back to Sentence Splitting for long paragraphs
                Chunk[] sentenceChunks = chunkSentences(trimmed, config);
                for (Chunk sc : sentenceChunks) {
                    result.add(new Chunk(
                            id++,
                            sc.text,
                            lastOverlap,
                            trimmed, // Parent context is the full paragraph
                            sc.startCharOffset,
                            sc.endCharOffset,
                            sc.tokenCount
                    ));
                    lastOverlap = sc.overlapText;
                }
            }
        }

        return result.toArray(new Chunk[0]);
    }

    private static int countTokens(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return s.trim().split("\\s+").length;
    }

    private static String extractTrailingTokens(String text, int maxTokens) {
        if (text == null || text.isEmpty() || maxTokens <= 0) return "";
        String[] tokens = text.trim().split("\\s+");
        if (tokens.length <= maxTokens) return text.trim();

        StringBuilder sb = new StringBuilder();
        for (int i = tokens.length - maxTokens; i < tokens.length; i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(tokens[i]);
        }
        return sb.toString();
    }
}

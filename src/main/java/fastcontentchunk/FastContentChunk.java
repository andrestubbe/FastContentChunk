package fastcontentchunk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * High-performance text chunking engine for RAG pipelines, LLM prompt engineering, and speech synthesis.
 * Supports strict boundary modes (Paragraph, Sentence) alongside AVX2-accelerated sliding token windows.
 */
public final class FastContentChunk {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?])\\s+");

    /**
     * Default constructor creating a standard chunking engine instance.
     */
    public FastContentChunk() {}

    /**
     * Chunks the given text using default {@link ChunkConfig} settings ({@link ChunkMode#HIERARCHICAL}).
     *
     * @param text The input text to segment
     * @return Array of segmented {@link Chunk} objects
     */
    public Chunk[] chunk(String text) {
        return chunk(text, new ChunkConfig());
    }

    /**
     * Chunks the given text according to the specified {@link ChunkConfig} strategy.
     *
     * @param text   The input text to segment
     * @param config The chunking parameters and mode
     * @return Array of segmented {@link Chunk} objects
     */
    public Chunk[] chunk(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) {
            return new Chunk[0];
        }

        ChunkConfig cfg = config != null ? config : new ChunkConfig();

        switch (cfg.getMode()) {
            case PARAGRAPH:
                return chunkByParagraph(text);
            case SENTENCE:
                return chunkBySentence(text);
            case TOKEN_WINDOW:
                return chunkByTokenWindow(text, cfg);
            case PARAGRAPH_WINDOW:
                return chunkByParagraphWindow(text, cfg);
            case SENTENCE_WINDOW:
                return chunkBySentenceWindow(text, cfg);
            case HIERARCHICAL:
            default:
                return chunkHierarchical(text, cfg);
        }
    }

    /**
     * Segments text strictly at double-newline paragraph boundaries ({@code \r?\n\r?\n+}).
     * Does not combine or pack paragraphs, making it ideal for TTS paragraph synthesis.
     *
     * @param text The input text to segment
     * @return Array of paragraph {@link Chunk} objects
     */
    public Chunk[] chunkByParagraph(String text) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        String[] raw = text.split("(\\r?\\n){2,}");
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        int searchFrom = 0;

        for (String p : raw) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;

            int start = text.indexOf(trimmed, searchFrom);
            if (start < 0) start = searchFrom;
            int end = start + trimmed.length();
            searchFrom = end;

            result.add(new Chunk(
                    id++,
                    trimmed,
                    "",
                    trimmed,
                    start,
                    end,
                    countTokens(trimmed)
            ));
        }
        return result.toArray(new Chunk[0]);
    }

    /**
     * Segments text strictly at individual sentence boundaries (punctuation {@code .}, {@code !}, {@code ?} followed by whitespace).
     * Yields one atomic chunk per sentence, delivering minimal latency for real-time streaming speech.
     *
     * @param text The input text to segment
     * @return Array of single-sentence {@link Chunk} objects
     */
    public Chunk[] chunkBySentence(String text) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        String[] raw = SENTENCE_PATTERN.split(text);
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        int searchFrom = 0;

        for (String s : raw) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) continue;

            int start = text.indexOf(trimmed, searchFrom);
            if (start < 0) start = searchFrom;
            int end = start + trimmed.length();
            searchFrom = end;

            result.add(new Chunk(
                    id++,
                    trimmed,
                    "",
                    trimmed,
                    start,
                    end,
                    countTokens(trimmed)
            ));
        }
        return result.toArray(new Chunk[0]);
    }

    /**
     * Segments text using hardware-accelerated AVX2 SIMD token sliding windows.
     * Enforces strict token count budgets with optional overlap tokens.
     *
     * @param text   The input text to tokenize
     * @param config Configuration specifying maxTokens and overlapTokens
     * @return Array of token-window {@link Chunk} objects
     */
    public Chunk[] chunkByTokenWindow(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        ChunkConfig cfg = config != null ? config : new ChunkConfig();
        return FastContentChunkNative.chunkSafe(text, cfg.getMaxTokens(), cfg.getOverlapTokens());
    }

    /**
     * Packs consecutive paragraphs into sliding windows without splitting individual paragraphs,
     * continuing until the accumulated token budget ({@code maxTokens}) is reached.
     *
     * @param text   The input text containing paragraph blocks
     * @param config Configuration defining the token budget and overlap
     * @return Array of windowed paragraph {@link Chunk} objects
     */
    public Chunk[] chunkByParagraphWindow(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        ChunkConfig cfg = config != null ? config : new ChunkConfig();
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

            if (currentChunk.length() > 0 && countTokens(currentChunk.toString()) + paraTokens > cfg.getMaxTokens()) {
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

                lastOverlap = extractTrailingTokens(chunkText, cfg.getOverlapTokens());
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

    /**
     * Packs consecutive sentences into sliding windows without cutting sentences in half,
     * accumulating text until {@code maxTokens} is reached and transferring overlap tokens.
     *
     * @param text   The input text containing sentences
     * @param config Configuration defining maximum window size and token overlap
     * @return Array of windowed sentence {@link Chunk} objects
     */
    public Chunk[] chunkBySentenceWindow(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        ChunkConfig cfg = config != null ? config : new ChunkConfig();
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

            if (currentChunk.length() > 0 && countTokens(currentChunk.toString()) + sentenceTokens > cfg.getMaxTokens()) {
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

                lastOverlap = extractTrailingTokens(chunkText, cfg.getOverlapTokens());
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

    /**
     * Recursively decomposes text down a hierarchy: paragraphs that exceed {@code maxTokens}
     * are further split into sentence chunks, and sentence chunks exceeding the budget fall back to SIMD token chunks.
     *
     * @param text   The raw document text
     * @param config Configuration governing token limits and overlap
     * @return Array of hierarchical {@link Chunk} objects
     */
    public Chunk[] chunkHierarchical(String text, ChunkConfig config) {
        if (text == null || text.trim().isEmpty()) return new Chunk[0];
        ChunkConfig cfg = config != null ? config : new ChunkConfig();
        String[] paragraphs = text.split("(\\r?\\n){2,}");
        List<Chunk> result = new ArrayList<>();
        int id = 0;
        String lastOverlap = "";

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            int paraTokens = countTokens(trimmed);

            if (paraTokens <= cfg.getMaxTokens()) {
                result.add(new Chunk(id++, trimmed, lastOverlap, trimmed, 0, trimmed.length(), paraTokens));
                lastOverlap = extractTrailingTokens(trimmed, cfg.getOverlapTokens());
            } else {
                Chunk[] sentenceChunks = chunkBySentenceWindow(trimmed, cfg);
                for (Chunk sc : sentenceChunks) {
                    result.add(new Chunk(
                            id++,
                            sc.text,
                            lastOverlap,
                            trimmed,
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

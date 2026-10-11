package fastcontentchunk;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FastContentChunkTest {

    @Test
    void testTextLessThan32Bytes() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.TOKEN_WINDOW);
        Chunk[] chunks = chunker.chunk("Short text under 32 chars", config);
        assertTrue(chunks.length > 0);
        assertEquals("Short", chunks[0].text.split(" ")[0]);
    }

    @Test
    void testTextExactMultiple32Bytes() {
        String text = "12345678901234567890123456789012";
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(10, 2, ChunkMode.TOKEN_WINDOW);
        Chunk[] chunks = chunker.chunk(text, config);
        assertTrue(chunks.length > 0);
    }

    @Test
    void testStrictParagraphChunking() {
        FastContentChunk chunker = new FastContentChunk();
        String text = "Paragraph 1\n\nParagraph 2\n\nParagraph 3";
        Chunk[] chunks = chunker.chunkByParagraph(text);
        assertEquals(3, chunks.length);
        assertEquals("Paragraph 1", chunks[0].text);
        assertEquals("Paragraph 2", chunks[1].text);
        assertEquals("Paragraph 3", chunks[2].text);
    }

    @Test
    void testStrictSentenceChunking() {
        FastContentChunk chunker = new FastContentChunk();
        String text = "First sentence. Second sentence! Third sentence?";
        Chunk[] chunks = chunker.chunkBySentence(text);
        assertEquals(3, chunks.length);
        assertEquals("First sentence.", chunks[0].text);
        assertEquals("Second sentence!", chunks[1].text);
        assertEquals("Third sentence?", chunks[2].text);
    }

    @Test
    void testWhitespaceOnly() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.HIERARCHICAL);
        Chunk[] chunks = chunker.chunk("   \t\n\r   ", config);
        assertEquals(0, chunks.length);
    }

    @Test
    void testEmptyText() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.HIERARCHICAL);
        Chunk[] chunks = chunker.chunk("", config);
        assertEquals(0, chunks.length);
    }
}

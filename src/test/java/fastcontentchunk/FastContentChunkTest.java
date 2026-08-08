package fastcontentchunk;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FastContentChunkTest {

    @Test
    void testTextLessThan32Bytes() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.TOKENS);
        Chunk[] chunks = chunker.chunk("Short text under 32 chars", config);
        assertTrue(chunks.length > 0);
        assertEquals("Short", chunks[0].text.split(" ")[0]);
    }

    @Test
    void testTextExactMultiple32Bytes() {
        // Exactly 32 characters
        String text = "12345678901234567890123456789012";
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(10, 2, ChunkMode.TOKENS);
        Chunk[] chunks = chunker.chunk(text, config);
        assertTrue(chunks.length > 0);
    }

    @Test
    void testWhitespaceOnly() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.RECURSIVE);
        Chunk[] chunks = chunker.chunk("   \t\n\r   ", config);
        assertEquals(0, chunks.length);
    }

    @Test
    void testEmptyText() {
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(5, 1, ChunkMode.RECURSIVE);
        Chunk[] chunks = chunker.chunk("", config);
        assertEquals(0, chunks.length);
    }
}

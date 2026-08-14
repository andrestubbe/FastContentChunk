package fastcontentchunk.benchmark;

import fastcontentchunk.FastContentChunk;
import fastcontentchunk.FastContentChunkNative;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 2, time = 1)
@Fork(1)
public class JMH_Chunk {

    private FastContentChunk chunker;
    private ChunkConfig recursiveConfig;
    private String documentText;

    @Setup
    public void setup() {
        chunker = new FastContentChunk();
        recursiveConfig = new ChunkConfig(128, 16, ChunkMode.RECURSIVE);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("Section ").append(i).append("\n");
            sb.append("This is paragraph ").append(i).append(" with SIMD accelerated token boundaries. ");
            sb.append("Dr. med. Schmidt reported a 99.8% precision rate in automated RAG document chunking. ");
            sb.append("The quick brown fox jumps over the lazy dog.\n\n");
        }
        documentText = sb.toString();
    }

    @Benchmark
    public Object benchmarkNativeAVX2Offsets() {
        return FastContentChunkNative.chunkToOffsets(documentText, 128, 16);
    }

    @Benchmark
    public Object benchmarkRecursiveChunking() {
        return chunker.chunk(documentText, recursiveConfig);
    }
}

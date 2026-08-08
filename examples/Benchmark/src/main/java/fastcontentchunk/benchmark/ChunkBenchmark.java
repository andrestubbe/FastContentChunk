package fastcontentchunk.benchmark;

import fastcontentchunk.Chunk;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import fastcontentchunk.FastContentChunk;
import fastcontentchunk.FastContentChunkNative;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ChunkBenchmark {

    private FastContentChunk chunker;
    private ChunkConfig configRecursive;
    private ChunkConfig configTokens;
    private String sampleDocumentText;

    @Setup(Level.Trial)
    public void setup() {
        chunker = new FastContentChunk();
        configRecursive = new ChunkConfig(512, 64, ChunkMode.RECURSIVE);
        configTokens = new ChunkConfig(512, 64, ChunkMode.TOKENS);
        
        sampleDocumentText = "§ 1 Executive Summary, Scope and Framework Objectives. " +
                "This comprehensive specification outlines the benchmarking framework for high-throughput Java RAG pipelines. " +
                "It evaluates layout parsing, sentence boundary detection, paragraph preservation, and multi-turn context retention.\n\n" +
                "§ 2 Deep Architectural Analysis: The Limits of Naive Token Chunking. " +
                "The architectural foundation of modern Retrieval-Augmented Generation (RAG) pipelines relies heavily on semantic preservation. " +
                "When documents are chunked naively using fixed character boundaries, critical contextual connections between sentences are severed.\n\n" +
                "§ 3 System Performance Matrix & Microsecond Benchmarks. " +
                "Benchmark testing reveals that Recursive Hybrid chunking delivers a 96.8% recall precision rate while maintaining high throughput.";
    }

    @Benchmark
    public Chunk[] benchmarkRecursiveChunking() {
        return chunker.chunk(sampleDocumentText, configRecursive);
    }

    @Benchmark
    public Chunk[] benchmarkTokensChunking() {
        return chunker.chunk(sampleDocumentText, configTokens);
    }

    @Benchmark
    public int[] benchmarkNativeZeroAllocationOffsets() {
        return FastContentChunkNative.chunkToOffsets(sampleDocumentText, 512, 64);
    }
}

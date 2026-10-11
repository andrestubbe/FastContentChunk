package fastcontentchunk;

/**
 * Immutable data model representing an individual segment of chunked text.
 * Contains positional offsets, text payload, and token metrics.
 */
public final class Chunk {

    /** Sequential identifier for this chunk within its parent document. */
    public final int id;

    /** The primary text content of this chunk. */
    public final String text;

    /** Overlap text carried over from the preceding chunk, if configured. */
    public final String overlapText;

    /** The complete parent text from which this chunk was derived. */
    public final String parentText;

    /** Inclusive character start index within the parent text. */
    public final int startCharOffset;

    /** Exclusive character end index within the parent text. */
    public final int endCharOffset;

    /** Estimated token count for this chunk. */
    public final int tokenCount;

    /**
     * Constructs a fully qualified {@link Chunk} instance.
     *
     * @param id              Sequential identifier
     * @param text            The text content of the chunk
     * @param overlapText     Overlap text from the previous chunk
     * @param parentText      The parent document text
     * @param startCharOffset Inclusive start offset in characters
     * @param endCharOffset   Exclusive end offset in characters
     * @param tokenCount      Estimated token count
     */
    public Chunk(int id, String text, String overlapText, String parentText, int startCharOffset, int endCharOffset, int tokenCount) {
        this.id = id;
        this.text = text != null ? text : "";
        this.overlapText = overlapText != null ? overlapText : "";
        this.parentText = parentText != null ? parentText : this.text;
        this.startCharOffset = startCharOffset;
        this.endCharOffset = endCharOffset;
        this.tokenCount = tokenCount;
    }

    /**
     * Convenience constructor creating a standalone {@link Chunk} with default offsets.
     *
     * @param id   Sequential identifier
     * @param text The text content of the chunk
     */
    public Chunk(int id, String text) {
        this(id, text, "", text, 0, text != null ? text.length() : 0, text != null ? text.split("\\s+").length : 0);
    }
}

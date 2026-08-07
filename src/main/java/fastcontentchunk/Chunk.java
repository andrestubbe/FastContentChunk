package fastcontentchunk;

public final class Chunk {

    public final int id;
    public final String text;
    public final String overlapText;
    public final String parentText;
    public final int startCharOffset;
    public final int endCharOffset;
    public final int tokenCount;

    public Chunk(int id, String text, String overlapText, String parentText, int startCharOffset, int endCharOffset, int tokenCount) {
        this.id = id;
        this.text = text != null ? text : "";
        this.overlapText = overlapText != null ? overlapText : "";
        this.parentText = parentText != null ? parentText : this.text;
        this.startCharOffset = startCharOffset;
        this.endCharOffset = endCharOffset;
        this.tokenCount = tokenCount;
    }

    public Chunk(int id, String text) {
        this(id, text, "", text, 0, text != null ? text.length() : 0, text != null ? text.split("\\s+").length : 0);
    }
}

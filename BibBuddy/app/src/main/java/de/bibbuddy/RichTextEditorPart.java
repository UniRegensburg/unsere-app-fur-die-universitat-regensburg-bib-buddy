package de.bibbuddy;

public class RichTextEditorPart {

    private final int start;
    private final int end;

    public RichTextEditorPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public boolean isValid() {
        return this.start < this.end;
    }

}
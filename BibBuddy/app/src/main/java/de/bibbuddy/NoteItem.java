package de.bibbuddy;

public class NoteItem {

    private final int id;
    private final String name;
    private final int type;
    private final String text;
    private final String createDate;
    private final String modDate;
    private final int noteFileId;

    public NoteItem(int id, String name, int type, String text, String createDate, String modDate, int noteFileId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.text = text;
        this.createDate = createDate;
        this.modDate = modDate;
        this.noteFileId = noteFileId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getModDate() {
        return modDate;
    }

    public int getNoteFileId() {
        return noteFileId;
    }

}

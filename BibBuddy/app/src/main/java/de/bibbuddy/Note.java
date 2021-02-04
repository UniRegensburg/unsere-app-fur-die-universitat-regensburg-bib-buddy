package de.bibbuddy;

public class Note {
    private Long id;
    private String name;
    private Integer type;
    private String text;
    private Long createDate;
    private Long modDate;
    private Long noteFileId;

    public Note(Long id, String name, Integer type, String text, Long createDate, Long modDate, Long noteFileId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.text = text;
        this.createDate = createDate;
        this.modDate = modDate;
        this.noteFileId = noteFileId;
    }

    //id is autoincrementing on database
    public Note(String name, Integer type, String text, Long createDate, Long modDate) {
        this.name = name;
        this.type = type;
        this.text = text;
        this.createDate = createDate;
        this.modDate = modDate;
    }

    public Note() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getModDate() {
        return modDate;
    }

    public void setModDate(Long modDate) {
        this.modDate = modDate;
    }

    public Long getNoteFileId() {
        return noteFileId;
    }

    public void setNoteFileId(Long noteFileId) {
        this.noteFileId = noteFileId;
    }
}

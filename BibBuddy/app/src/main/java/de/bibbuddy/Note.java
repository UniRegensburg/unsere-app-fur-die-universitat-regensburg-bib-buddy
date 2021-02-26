package de.bibbuddy;

/**
 * The Note class maps the data of the database from the table Note.
 *
 * @author Sarah Kurek
 */
public class Note {

  private Long id;
  private String name;
  private Integer type;
  private String text;
  private Long createDate;
  private Long modDate;
  private Long noteFileId;


  /**
   * Empty constructor to create an note-object that will be filled with necessary params later.
   */
  public Note() {
  }

  /**
   * Constructor to set up a new note with only name, type and text.
   *
   * @param name name of the note object
   * @param type type of the note object (text, voice or picture)
   * @param text text content of the note object (as long as it is a type "text")
   */
  public Note(String name, Integer type, String text) {
    this.name = name;
    this.type = type;
    this.text = text;
  }

  /**
   * Note-class creates a note object with necessary meta-data.
   * This constructor is needed for creating a specific note with a known id.
   *
   * @param id         id of the note object
   * @param name       name of the note object
   * @param type       type of the note object (text, voice or picture)
   * @param text       text content of the note object (as long as it is a type "text")
   * @param createDate creation date of the note object
   * @param modDate    modification date of the note object
   * @param noteFileId id of the noteFile object contained in the note object
   */
  public Note(Long id, String name, Integer type, String text, Long createDate, Long modDate,
              Long noteFileId) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.text = text;
    this.createDate = createDate;
    this.modDate = modDate;
    this.noteFileId = noteFileId;
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

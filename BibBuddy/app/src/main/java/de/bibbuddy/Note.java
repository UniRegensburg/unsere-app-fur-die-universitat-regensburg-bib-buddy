package de.bibbuddy;

/**
 * The Note class maps the data of the database from the table Note.
 *
 * @author Sarah Kurek
 */
@SuppressWarnings("unused")
public class Note {

  private final String name;
  private final NoteTypeLut type;
  private final String text;
  private Long id;

  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  // Can be used in the future and is here only to check for possible bugs
  private Long createDate;

  private Long modDate;
  private Long noteFileId;
  private String noteFilePath;

  /**
   * Constructor to set up a new note with only name, type and text.
   *
   * @param name name of the note object
   * @param type type of the note object (text, voice or picture)
   * @param text text content of the note object (as long as it is a type "text")
   */
  public Note(String name, NoteTypeLut type, String text) {
    this.name = name;
    this.type = type;
    this.text = text;
  }

  /**
   * Constructor to set up a new note with only name, type and text.
   *
   * @param name         name of the note object
   * @param type         type of the note object (text, voice or picture)
   * @param noteFilePath path to the file of the note object
   */
  public Note(String name, NoteTypeLut type, String text, String noteFilePath) {
    this.name = name;
    this.type = type;
    this.text = text;
    this.noteFilePath = noteFilePath;
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
  public Note(Long id, String name, NoteTypeLut type, String text, Long createDate, Long modDate,
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

  public String getName() {
    return name;
  }

  public NoteTypeLut getType() {
    return type;
  }

  public String getText() {
    return text;
  }

  public Long getModDate() {
    return modDate;
  }

  public Long getNoteFileId() {
    return noteFileId;
  }

  public String getNoteFilePath() {
    return noteFilePath;
  }

}

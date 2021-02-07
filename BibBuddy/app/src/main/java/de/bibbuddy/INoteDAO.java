package de.bibbuddy;

import java.util.List;

public interface INoteDAO {

    boolean create(Note note);

    Note findById(long id);

    List<Note> findAll();

    void delete(Long id);
    
    void updateNote(Long id, String name, int type, String text, Long createDate, Long modDate, Long noteFileId);

    boolean linkNoteWithBook(Long noteId, Long bookId);

    List<Note> getAllNotesForBook(Long bookId);

}

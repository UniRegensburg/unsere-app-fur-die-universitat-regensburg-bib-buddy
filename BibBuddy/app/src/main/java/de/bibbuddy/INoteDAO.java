package de.bibbuddy;

import java.util.List;

public interface INoteDAO {

    boolean create(Note note);

    Note findById(long id);

    List<Note> findAll();

    void delete(Long id);

}

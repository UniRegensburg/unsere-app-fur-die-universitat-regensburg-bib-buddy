package de.bibbuddy;

import java.util.List;

public interface IAuthorDAO {

    boolean create(Author author);

    Author findById(Long id);

    List<Author> findAll();

    void delete(Long id);

}

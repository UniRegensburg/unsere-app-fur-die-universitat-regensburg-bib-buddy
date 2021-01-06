package de.bibbuddy;

import java.util.List;

public interface IBookDAO {

    boolean create(Book book, Author author);

    Book findById(Long id);

    List<Book> findAll();

    void delete(Long id);

}

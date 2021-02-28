package de.bibbuddy;

import java.util.List;

public interface InterfaceBookDao {

  boolean create(Book book);

  Book findById(Long id);

  List<Book> findAll();

  void delete(Long id);

}

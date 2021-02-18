package de.bibbuddy;

import java.util.List;

public interface InterfaceAuthorDao {

  boolean create(Author author);

  Author findById(Long id);

  List<Author> findAll();

  void delete(Long id);

}

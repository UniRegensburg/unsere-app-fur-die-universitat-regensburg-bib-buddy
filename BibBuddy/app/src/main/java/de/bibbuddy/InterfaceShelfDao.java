package de.bibbuddy;

import java.util.List;

public interface InterfaceShelfDao {

  boolean create(Shelf shelf);

  Shelf findById(long id);

  List<Shelf> findAll();

  void delete(Long id);

}

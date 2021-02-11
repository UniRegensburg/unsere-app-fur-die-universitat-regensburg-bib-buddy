package de.bibbuddy;

import java.util.List;

public interface ITagDAO {

  boolean create(Tag tag);

  Tag findById(Long id);

  List<Tag> findAll();

  void delete(Long id);

}

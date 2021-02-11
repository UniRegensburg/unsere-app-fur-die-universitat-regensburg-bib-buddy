package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthorDao implements IAuthorDAO {

  private final DatabaseHelper dbHelper;

  public AuthorDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  @Override
  public boolean create(Author author) {
    long currentTime = System.currentTimeMillis() / 1_000L;
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
      contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());
      contentValues.put(DatabaseHelper.TITLE, author.getTitle());
      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);

      long id = db.insert(DatabaseHelper.TABLE_NAME_AUTHOR, null, contentValues);

      author.setId(id);

    } catch (SQLiteException ex) {
      return false;
    } finally {
      db.close();
    }

    return true;
  }

  // get single author entry
  @Override
  public Author findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR, new String[] {DatabaseHelper._ID,
        DatabaseHelper.FIRST_NAME, DatabaseHelper.LAST_NAME, DatabaseHelper.TITLE,
        DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE},
        DatabaseHelper._ID + "=?",
        new String[] {String.valueOf(id)}, null, null, null, null);


    Author author = null;
    if (cursor != null) {
      cursor.moveToFirst();

      author = new Author(
          Long.parseLong(cursor.getString(0)), // Id
          cursor.getString(1), // First name
          cursor.getString(2), // Last name
          cursor.getString(3), // Title
          Integer.parseInt(cursor.getString(4)), // Create date
          Integer.parseInt(cursor.getString(5)) // Mod date
      );
      cursor.close();
    }
    return author;
  }

  // get all authors in a list view
  @Override
  public List<Author> findAll() {
    List<Author> authorList = new ArrayList<Author>();
    // Select All Query
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        Author author = new Author();

        author.setId(Long.parseLong(cursor.getString(0))); // Id
        author.setFirstName(cursor.getString(1)); // First name
        author.setLastName(cursor.getString(2)); // Last name
        author.setTitle(cursor.getString(3)); // Title
        author.setCreateDate(Integer.parseInt(cursor.getString(4))); // Create date
        author.setModDate(Integer.parseInt(cursor.getString(5))); // Mod date

        // Adding author to list
        authorList.add(author);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return authorList;
  }

  // delete single author entry
  @Override
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_AUTHOR, DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)});

    db.close();
  }

  public boolean existAuthor(Author author) {
    List<Author> dbAuthorList = findAll();
    for (Author dbAuthor : dbAuthorList) {
      //  compare authors with title, firstname and lastname only
      if (Objects.equals(author.getTitle(), dbAuthor.getTitle())
          && Objects.equals(author.getFirstName(), dbAuthor.getFirstName())
          && Objects.equals(author.getLastName(), dbAuthor.getLastName())) {
        return true;
      }
    }

    return false;
  }

  public void createAuthors(List<Author> authorList) {
    for (Author author : authorList) {
      if (!existAuthor(author)) {
        create(author);
      }
    }
  }

  public List<Long> getAuthorIds(List<Author> authorList) {
    List<Long> authorIds = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    String selectQuery =
        "SELECT  " + DatabaseHelper._ID + " FROM " + DatabaseHelper.TABLE_NAME_AUTHOR
            + " WHERE ";

    StringBuilder partSqlQuery = new StringBuilder();

    for (Author author : authorList) {
      boolean andPart = false;

      if (partSqlQuery.length() > 0) {
        partSqlQuery.append(" OR ( ");
      } else {
        partSqlQuery.append(" ( ");
      }

      if (author.getFirstName() != null) {
        partSqlQuery.append(DatabaseHelper.FIRST_NAME + "=\"").append(author.getFirstName())
            .append("\"");
        andPart = true;
      }

      if (author.getLastName() != null) {
        if (andPart) {
          partSqlQuery.append(" AND ");
        }
        partSqlQuery.append(DatabaseHelper.LAST_NAME + "=\"").append(author.getLastName())
            .append("\"");
        andPart = true;
      }

      if (author.getTitle() != null) {
        if (andPart) {
          partSqlQuery.append(" AND ");
        }
        partSqlQuery.append(DatabaseHelper.TITLE + "=\"").append(author.getTitle()).append("\"");
      }

      partSqlQuery.append(" ) ");
    }

    // execute sql query
    selectQuery += partSqlQuery;
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        Long id = Long.parseLong(cursor.getString(0));
        authorIds.add(id);

      } while (cursor.moveToNext());
      cursor.close();
    }

    return authorIds;
  }
}

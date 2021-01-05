package de.bibbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Names
    public static final String TABLE_NAME_AUTHOR = "author";
    public static final String TABLE_NAME_TAG = "tag";
    public static final String TABLE_NAME_SHELF = "shelf";
    public static final String TABLE_NAME_NOTE_FILE = "note_file";
    public static final String TABLE_NAME_NOTE_TYPE_LUT = "note_type_lut";
    public static final String TABLE_NAME_NOTE = "note";
    public static final String TABLE_NAME_BOOK = "book";
    public static final String TABLE_NAME_SHELF_BOOK_LNK = "shelf_book_lnk";
    public static final String TABLE_NAME_BOOK_LINK = "book_link";
    public static final String TABLE_NAME_BOOK_TAG_LNK = "book_tag_lnk";
    public static final String TABLE_NAME_NOTE_TAG_LNK = "note_tag_lnk";

    // Table columns
    public static final String _ID = "id";
    public static final String NAME = "name";
    public static final String TITLE = "title";
    public static final String CREATE_DATE = "creation_date";
    public static final String MOD_DATE = "modifikation_date";
    public static final String ISBN = "isbn";
    public static final String SUBTITLE = "subtitle";
    public static final String PUB_YEAR = "publication_year";
    public static final String PUBLISHER = "publisher";
    public static final String VOLUME = "volume";
    public static final String EDITION = "edition";
    public static final String ADD_INFOS = "additional_infos";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String COMMENT = "comment";
    public static final String TEXT = "text";
    public static final String TYPE = "type";
    public static final String FILE = "file";
    public static final String SHELF_ID = "shelf_id";
    public static final String BOOK_ID = "book_id";
    public static final String AUTHOR_ID = "author_id";
    public static final String NOTE_ID = "note_id";
    public static final String TAG_ID = "tag_id";
    public static final String NOTE_FILE_ID = "note_file_id";
    public static final String BOOK_ORIGIN_ID = "book_origin_id";
    public static final String BOOK_REF_ID = "book_referred_id";


    // Database Information
    private static final String DB_NAME = "BibBuddyStorage.db"; // DB

    // database version
    private static final int DB_VERSION = 1;

    // Table queries
    private static final String CREATE_TABLE_AUTHOR =
            "CREATE TABLE " + TABLE_NAME_AUTHOR
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + FIRST_NAME + " VARCHAR, "
                    + LAST_NAME + " VARCHAR, "
                    + TITLE + " VARCHAR, "
                    + CREATE_DATE + " DATETIME NOT NULL, "
                    + MOD_DATE + " DATETIME NOT NULL"
                    + ");";


    private static final String CREATE_TABLE_TAG =
            "CREATE TABLE " + TABLE_NAME_TAG
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " VARCHAR NOT NULL "
                    + ");";


    private static final String CREATE_TABLE_SHELF =
            "CREATE TABLE " + TABLE_NAME_SHELF
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " VARCHAR NOT NULL, "
                    + CREATE_DATE + " DATETIME NOT NULL, "
                    + MOD_DATE + " DATETIME NOT NULL, "
                    + SHELF_ID + " INTEGER "
                    + " );";


    private static final String CREATE_TABLE_NOTE_FILE =
            "CREATE TABLE " + TABLE_NAME_NOTE_FILE
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + FILE + " BLOB NOT NULL"
                    + " );";


    private static final String CREATE_TABLE_NOTE_TYPE_LUT =
            "CREATE TABLE " + TABLE_NAME_NOTE_TYPE_LUT
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " VARCHAR NOT NULL "
                    + " );";


    private static final String CREATE_TABLE_NOTE =
            "CREATE TABLE " + TABLE_NAME_NOTE
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " VARCHAR NOT NULL, "
                    + TYPE + " INTEGER NOT NULL, "
                    + TEXT + " TEXT, "
                    + CREATE_DATE + " DATETIME NOT NULL, "
                    + MOD_DATE + " DATETIME NOT NULL, "
                    + NOTE_FILE_ID + " INTEGER, "

                    + " CONSTRAINT " + TABLE_NAME_NOTE + "_FK_1 FOREIGN KEY (" + TYPE
                    + ") REFERENCES " + TABLE_NAME_NOTE_TYPE_LUT + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_NOTE + "_FK_2 FOREIGN KEY (" + NOTE_FILE_ID
                    + ") REFERENCES " + TABLE_NAME_NOTE_FILE + "(" + _ID + ")"
                    + " );";


    private static final String CREATE_TABLE_BOOK =
            "CREATE TABLE " + TABLE_NAME_BOOK
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + ISBN + " VARCHAR NOT NULL, "
                    + TITLE + " VARCHAR, "
                    + SUBTITLE + " VARCHAR, "
                    + PUB_YEAR + " INTEGER(4), "
                    + PUBLISHER + " VARCHAR, "
                    + VOLUME + " VARCHAR, "
                    + EDITION + " VARCHAR, "
                    + ADD_INFOS + " TEXT, "
                    + CREATE_DATE + " DATETIME NOT NULL, "
                    + MOD_DATE + " DATETIME NOT NULL, "
                    + AUTHOR_ID + " INTEGER, "
                    + NOTE_ID + " INTEGER, "

                    + " CONSTRAINT " + TABLE_NAME_BOOK + "_FK_1 FOREIGN KEY (" + AUTHOR_ID
                    + ") REFERENCES " + TABLE_NAME_AUTHOR + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_BOOK + "_FK_2 FOREIGN KEY (" + NOTE_ID
                    + ") REFERENCES " + TABLE_NAME_NOTE + "(" + _ID + ")"
                    + " );";


    private static final String CREATE_TABLE_SHELF_BOOK_LNK =
            "CREATE TABLE " + TABLE_NAME_SHELF_BOOK_LNK
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + SHELF_ID + " INTEGER NOT NULL, "
                    + BOOK_ID + " INTEGER NOT NULL, "

                    + " CONSTRAINT " + TABLE_NAME_SHELF_BOOK_LNK + "_FK_1 FOREIGN KEY (" + SHELF_ID
                    + ") REFERENCES " + TABLE_NAME_SHELF + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_SHELF_BOOK_LNK + "_FK_2 FOREIGN KEY (" + BOOK_ID
                    + ") REFERENCES " + TABLE_NAME_BOOK + "(" + _ID + ")"
                    + " );";


    private static final String CREATE_TABLE_BOOK_LINK =
            "CREATE TABLE " + TABLE_NAME_BOOK_LINK
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + BOOK_ORIGIN_ID + " INTEGER NOT NULL, "
                    + BOOK_REF_ID + " INTEGER NOT NULL, "
                    + COMMENT + " TEXT, "
                    + CREATE_DATE + " DATETIME NOT NULL, "
                    + MOD_DATE + " DATETIME NOT NULL, "

                    + " CONSTRAINT " + TABLE_NAME_BOOK_LINK + "_FK_1 FOREIGN KEY (" + BOOK_ORIGIN_ID
                    + ") REFERENCES " + TABLE_NAME_BOOK + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_BOOK_LINK + "_FK_2 FOREIGN KEY (" + BOOK_REF_ID
                    + ") REFERENCES " + TABLE_NAME_BOOK + "(" + _ID + ")"
                    + " );";


    private static final String CREATE_TABLE_NOTE_TAG_LNK =
            "CREATE TABLE " + TABLE_NAME_NOTE_TAG_LNK
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + TAG_ID + " INTEGER NOT NULL, "
                    + NOTE_ID + " INTEGER NOT NULL, "

                    + " CONSTRAINT " + TABLE_NAME_NOTE_TAG_LNK + "_FK_1 FOREIGN KEY (" + TAG_ID
                    + ") REFERENCES " + TABLE_NAME_TAG + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_NOTE_TAG_LNK + "_FK_2 FOREIGN KEY (" + NOTE_ID
                    + ") REFERENCES " + TABLE_NAME_NOTE + "(" + _ID + ")"
                    + " );";


    private static final String CREATE_TABLE_BOOK_TAG_LNK =
            "CREATE TABLE " + TABLE_NAME_BOOK_TAG_LNK
                    + "( "
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + TAG_ID + " INTEGER NOT NULL, "
                    + BOOK_ID + " INTEGER NOT NULL, "

                    + " CONSTRAINT " + TABLE_NAME_BOOK_TAG_LNK + "_FK_1 FOREIGN KEY (" + TAG_ID
                    + ") REFERENCES " + TABLE_NAME_TAG + "(" + _ID + "),"

                    + " CONSTRAINT " + TABLE_NAME_BOOK_TAG_LNK + "_FK_2 FOREIGN KEY (" + BOOK_ID
                    + ") REFERENCES " + TABLE_NAME_BOOK + "(" + _ID + ")"
                    + " );";


    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_AUTHOR);
        db.execSQL(CREATE_TABLE_TAG);
        db.execSQL(CREATE_TABLE_SHELF);
        db.execSQL(CREATE_TABLE_NOTE_FILE);
        db.execSQL(CREATE_TABLE_NOTE_TYPE_LUT);
        db.execSQL(CREATE_TABLE_NOTE);
        db.execSQL(CREATE_TABLE_BOOK);
        db.execSQL(CREATE_TABLE_SHELF_BOOK_LNK);
        db.execSQL(CREATE_TABLE_BOOK_LINK);
        db.execSQL(CREATE_TABLE_BOOK_TAG_LNK);
        db.execSQL(CREATE_TABLE_NOTE_TAG_LNK);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

}


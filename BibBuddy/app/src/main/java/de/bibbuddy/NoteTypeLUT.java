package de.bibbuddy;

public enum NoteTypeLUT {

    TEXT(1), IMAGE(2), AUDIO(4), OTHER(5);

    private final int id;

    NoteTypeLUT(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

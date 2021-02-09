package de.bibbuddy;

public class NoteAudioItem extends NoteItem {
    public NoteAudioItem(String name, Long id) {
        super(name, R.drawable.microphone, id);
    }

    public NoteAudioItem(String name, Long id, Long bookId) {
        super(name, R.drawable.microphone, id, bookId);
    }
}

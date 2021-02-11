package de.bibbuddy;

public class NoteAudioItem extends NoteItem {
    public NoteAudioItem(String modDate, String name, Long id) {
        super(modDate, name, R.drawable.microphone, id);
    }

    public NoteAudioItem(String modDate, String name, Long id, Long bookId) {
        super(modDate, name, R.drawable.microphone, id, bookId);
    }
}

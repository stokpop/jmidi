package nl.stokpop.midiflux;

public class Note {
    private int note;
    private int duration;

    public Note(int note, int duration) {
        this.note = note;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public int getNote() {
        return note;
    }
}

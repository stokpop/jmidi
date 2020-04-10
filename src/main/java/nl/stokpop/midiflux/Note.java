package nl.stokpop.midiflux;

public class Note {
    private int note;
    private int durationMs;
    private int velocity;

    public Note(int note, int durationMs, int velocity) {
        this.note = note;
        this.durationMs = durationMs;
        this.velocity = velocity;
    }

    public Note(int note) {
        this(note, 200);
    }

    public Note(int note, int durationMs) {
        this(note, durationMs, 70);
    }

    public int getDurationMs() {
        return durationMs;
    }

    public int getNote() {
        return note;
    }

    public int getVelocity() { return velocity; }

    @Override
    public String toString() {
        return "Note{" +
                "note=" + note +
                ", duration=" + durationMs +
                ", velocity=" + velocity +
                '}';
    }
}

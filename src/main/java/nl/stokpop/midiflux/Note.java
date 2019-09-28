package nl.stokpop.midiflux;

public class Note {
    private int note;
    private int duration;
    private int velocity;

    public Note(int note, int duration, int velocity) {
        this.note = note;
        this.duration = duration;
        this.velocity = velocity;
    }

    public Note(int note) {
        this(note, 200);
    }

    public Note(int note, int duration) {
        this(note, duration, 70);
    }

    public int getDuration() {
        return duration;
    }

    public int getNote() {
        return note;
    }

    public int getVelocity() { return velocity; }

    @Override
    public String toString() {
        return "Note{" +
                "note=" + note +
                ", duration=" + duration +
                ", velocity=" + velocity +
                '}';
    }
}

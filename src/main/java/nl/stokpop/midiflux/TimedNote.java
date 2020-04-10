package nl.stokpop.midiflux;

public class TimedNote {
    private long time;
    private Note note;

    public TimedNote(long time, Note note) {
        this.time = time;
        this.note = note;
    }

    public TimedNote(long time, int note, int duration, int velocity) {
        this.time = time;
        this.note = new Note(note, duration, velocity);
    }

    public long getTime() {
        return time;
    }

    public Note getNote() {
        return note;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimedNote{");
        sb.append("time=").append(time);
        sb.append(", note=").append(note);
        sb.append('}');
        return sb.toString();
    }
}

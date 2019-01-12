package nl.stokpop.jmidi;

public class MidiBytesToTempo implements MidiBytesConverter {

    public static final MidiBytesToTempo singleton = new MidiBytesToTempo();
    
    @Override
    public String convertBytes(final byte[] midiBytes) {
        int tempo = (midiBytes[0] & 0xff) << 16 | (midiBytes[1] & 0xff) << 8 | (midiBytes[2] & 0xff);
        int bpm = 60_000_000 / tempo;
        return String.format("%-3s beats per minute", String.valueOf(bpm));
    }
}

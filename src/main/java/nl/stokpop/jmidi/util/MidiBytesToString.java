package nl.stokpop.jmidi.util;

public class MidiBytesToString implements MidiBytesConverter {

    static final MidiBytesToString singleton = new MidiBytesToString();
    
    @Override
    public String convertBytes(final byte[] midiBytes) {
        // skip first two type byte
        if (midiBytes.length < 1) {
            return "";
        }

        return new String(midiBytes);
    }
}

package nl.stokpop.jmidi;

import java.util.Arrays;

public class MidiBytesToString implements MidiBytesConverter {

    public static final MidiBytesToString singleton = new MidiBytesToString();
    
    @Override
    public String convertBytes(final byte[] midiBytes) {
        // skip first two type byte
        if (midiBytes.length < 1) {
            return "";
        }

        return new String(midiBytes);
    }
}

package nl.stokpop.jmidi.util;

public class MidiBytesToIntList implements MidiBytesConverter {

    static final MidiBytesToIntList singleton = new MidiBytesToIntList();

    @Override
    public String convertBytes(final byte[] midiData) {
        if (midiData.length < 1) {
            return "";
        }
        StringBuilder message = new StringBuilder();
        message.append(midiData[0]);
        for (int i = 1; i < midiData.length; i++) {
            message.append(",").append(midiData[i]);
        }
        return message.toString();
    }
}

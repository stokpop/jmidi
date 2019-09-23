package nl.stokpop.jmidi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class MidiUtil {

    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private MidiUtil() {}

    public static MidiEvent makeEvent(int command, int channel,
                               int note, int velocity, long tick)
    {

        MidiEvent event = null;

        try {

            ShortMessage shortMessage = new ShortMessage();
            shortMessage.setMessage(command, channel, note, velocity);

            event = new MidiEvent(shortMessage, tick);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        return event;
    }
}

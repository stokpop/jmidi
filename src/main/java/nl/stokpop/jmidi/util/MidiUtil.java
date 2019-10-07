package nl.stokpop.jmidi.util;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MidiUtil {

    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private static final Map<Integer, KeyInfo> keyToKeyInfo = new HashMap<>();

    private MidiUtil() {}

    private static class KeyInfo {
        private long timestamp;
        private long velocity;

        public KeyInfo(long timestamp, long velocity) {
            this.timestamp = timestamp;
            this.velocity = velocity;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getVelocity() {
            return velocity;
        }
    }

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

    public static String printMessageInfo(final MidiMessage message) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int channel = sm.getChannel();
            int command = sm.getCommand();
            if (command == NOTE_ON || command == NOTE_OFF) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];
                int velocity = sm.getData2();
                return String.format("Channel %-3s Note %-3s %s octave=%d key=%-3d velocity=%-3d", channel, command == NOTE_ON ? "on" : "off", noteName, octave, key, velocity);
            }
            else if (command == 0xB0) {
                return String.format("Channel %-3s Command: Control Change (CC) data1=%d data2=%d", channel, sm.getData1(), sm.getData2());
            }
            else if (command == 0xC0) {
                return String.format("Channel %-3s Command: Program Change data1 (program): %s", channel, sm.getData1());
            }
            else if (command == 0xD0) {
                return String.format("Channel %-3s Command: After touch data1 (pressure): %s", channel, sm.getData1());
            } else {
                return String.format("Channel %-3s Command: %s, data1: %d, data2: %d", channel, command, sm.getData1(), sm.getData2());
            }
        } else {
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                int type = metaMessage.getType();
                MetaMessageType messageType = MetaMessageType.codeToType(type);
                return String.format("MetaMessage (%1$-14s): %2$s", messageType.getName(), messageType.convertBytes(((MetaMessage) message).getData()));
            }
            else {
                return "Other message: " + message.getClass();
            }
        }
    }

    public static Optional<String> printCode(final MidiMessage message, final long timestamp) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int command = sm.getCommand();
            if (command == NOTE_ON) {
                int key = sm.getData1();
                int velocity = sm.getData2();
                KeyInfo keyInfo = new KeyInfo(timestamp, velocity);
                keyToKeyInfo.put(key, keyInfo);
            }
            else if (command == NOTE_OFF) {
                int key = sm.getData1();
                KeyInfo keyInfo = keyToKeyInfo.getOrDefault(key, new KeyInfo(System.nanoTime(),0));

                return Optional.of(String.format("new Note(%d, %d, %d),", key, (timestamp - keyInfo.getTimestamp())/1000, keyInfo.getVelocity()));
            }
        }
        return Optional.empty();
    }
}

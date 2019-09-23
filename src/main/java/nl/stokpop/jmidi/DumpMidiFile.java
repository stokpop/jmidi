package nl.stokpop.jmidi;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class DumpMidiFile {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Please add midi file as parameter, and optional track number to play that track.");
            System.exit(2);
        }

        String midiFile = args[0];

        String midiFileAbsPath = midiFile.startsWith("/") ? midiFile : "/" + midiFile;
        Sequence sequence = MidiSystem.getSequence(DumpMidiFile.class.getResourceAsStream(midiFileAbsPath));

        if (args.length == 2) {
            int trackNumber = Integer.parseInt(args[1]);
            playTrack(sequence, trackNumber);
        }
        else {
            printMidiInfo(sequence);
        }

    }

    private static void playTrack(Sequence sequence, int trackNumber) {



    }

    private static void printMidiInfo(Sequence sequence) {
        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            sayLn("Track " + trackNumber + ": size = " + track.size());

            sayLn();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                say(String.format("@%1$-9d ", event.getTick()));
                MidiMessage message = event.getMessage();
                printMessageInfo(message);
            }
            sayLn();
        }
    }

    private static void printMessageInfo(final MidiMessage message) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            say(String.format("Channel=%-2d ", sm.getChannel()));
            int command = sm.getCommand();
            if (command == MidiUtil.NOTE_ON || command == MidiUtil.NOTE_OFF) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = MidiUtil.NOTE_NAMES[note];
                int velocity = sm.getData2();
                sayLn(String.format("Note %-3s %s%d key=%-3d velocity=%-3d", command == MidiUtil.NOTE_ON ? "on" : "off", noteName, octave, key, velocity));
            }
            else if (command == 0xB0) {
                sayLn(String.format("Command: Control Change (CC) data1=%d data2=%d", sm.getData1(), sm.getData2()));
            }
            else if (command == 0xC0) {
                sayLn("Command: Program Change data1 (program):" + sm.getData1());
            }
            else if (command == 0xD0) {
                sayLn("Command: Aftertouch data1 (pressure):" + sm.getData1());
            } else {
                sayLn("Command:" + command);
            }
        } else {
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                int type = metaMessage.getType();
                MetaMessageType messageType = MetaMessageType.codeToType(type);
                sayLn(String.format("MetaMessage (%1$-14s): %2$s", messageType.getName(), messageType.convertBytes(((MetaMessage) message).getData())));
            }
            else {
                sayLn("Other message: " + message.getClass());
            }
        }
    }

    private static void sayLn(final String message) {
        System.out.println(message);
    }

    private static void say(final String message) {
        System.out.print(message);
    }

    private static void sayLn() {
        sayLn("");
    }
}
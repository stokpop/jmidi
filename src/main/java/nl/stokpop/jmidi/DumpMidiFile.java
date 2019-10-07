package nl.stokpop.jmidi;

import nl.stokpop.jmidi.util.MidiUtil;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class DumpMidiFile {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Please add midi file as parameter, and optional track number to play that track.");
            System.exit(1);
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
                MidiUtil.printMessageInfo(message);
            }
            sayLn();
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
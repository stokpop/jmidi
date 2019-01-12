package nl.stokpop;

// Java program showing how to change the instrument type
import javax.sound.midi.*;
import java.util.*;

public class MyMidiPlayer1 {

    public static void main(String[] args) throws Exception
    {

        MyMidiPlayer1 player = new MyMidiPlayer1();

        Scanner in = new Scanner(System.in);
        System.out.println("Enter the instrument to be played");
        int instrument = 88;
        System.out.println("Enter the note to be played");
        int note = 40;

        Soundbank defaultSoundbank = MidiSystem.getSynthesizer().getDefaultSoundbank();

        Instrument[] instruments = defaultSoundbank.getInstruments();

        for (Instrument midiInstrument : instruments) {
            System.out.println("Ins: " + midiInstrument);
        }


        player.setUpPlayer(instrument, note);
    }

    public void setUpPlayer(int instrument, int note)
    {

        try {

            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            Sequence sequence = new Sequence(Sequence.PPQ, 4);
            Track track = sequence.createTrack();

            for (int i = 0; i < 128; i++) {

                int tick = i * 2;
                
                // Set the instrument type
                track.add(makeEvent(192, 1, i, 0, tick));

                // Add a note on event with specified note
                track.add(makeEvent(144, 1, note, 100, tick));

                // Add a note off event with specified note
                track.add(makeEvent(128, 1, note, 100, tick + 5));
            }
            sequencer.setSequence(sequence);
            sequencer.start();

            while (true) {

                if (!sequencer.isRunning()) {
                    sequencer.close();
                    System.exit(1);
                }
                Thread.sleep(100);
            }
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public MidiEvent makeEvent(int command, int channel,
                               int note, int velocity, int tick)
    {

        MidiEvent event = null;

        try {

            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, note, velocity);

            event = new MidiEvent(a, tick);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        return event;
    }
} 

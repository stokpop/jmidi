package nl.stokpop.jmidi;

// Java program showing how to change the instrument type
import javax.sound.midi.*;
import java.util.*;

public class JMidiPlayer {

    public static void main(String[] args) throws Exception
    {

        JMidiPlayer player = new JMidiPlayer();

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
                track.add(MidiUtil.makeEvent(192, 1, i, 0, tick));

                // Add a note on event with specified note
                track.add(MidiUtil.makeEvent(144, 1, note, 100, tick));

                // Add a note off event with specified note
                track.add(MidiUtil.makeEvent(128, 1, note, 100, tick + 5));
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


} 

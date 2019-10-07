package nl.stokpop.jmidi;

import nl.stokpop.jmidi.util.MidiUtil;

import javax.sound.midi.*;
import java.util.*;

public class JMidiPlayer {

    public static void main(String[] args) throws Exception
    {

        JMidiPlayer player = new JMidiPlayer();

        int instrument = 88;
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

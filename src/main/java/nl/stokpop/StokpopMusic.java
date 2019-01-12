package nl.stokpop;

import nl.stokpop.jmidi.JMidi;

import javax.sound.midi.MidiUnavailableException;

public class StokpopMusic {

    public static void main(String[] args) {
        JMidi jMidi = new JMidi();

        try {
            jMidi.createSequencer();
        } catch (MidiUnavailableException e) {
            System.err.println(e);
        }
    }
}

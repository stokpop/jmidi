package nl.stokpop.midiflux;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class MidiExternal {

    public static void main(String[] args) throws MidiUnavailableException {

        MidiController.printDevices();

        //MidiController.printInstruments();

        try {
            String midiFile = "disco.mid";
            InputStream music = MidiExternal.class.getClassLoader().getResourceAsStream(midiFile);
            if (music == null) {
                throw new RuntimeException("File not found: " + midiFile);
            }
            Sequence sequence = MidiSystem.getSequence(music);
            Optional<MidiDevice> midiOutDevice = MidiController.openMidiDevice(2);
            if (midiOutDevice.isPresent()) {
                startExternal(midiOutDevice.get(), sequence);
                midiOutDevice.get().close();
            }
            else {
                System.out.println("No midi device found.");
            }

        } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
            System.err.printf("Error: %s%n", e);
        }

    }

    public static void startExternal(MidiDevice midiOutDevice, Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
        System.out.println("Start external play.");

        Receiver midiOutReceiver = midiOutDevice.getReceiver();

        Sequencer midiOutSequencer = MidiSystem.getSequencer(false);

        //Add the new MIDI out device here.
        midiOutSequencer.getTransmitter().setReceiver(midiOutReceiver);
        midiOutSequencer.open();

        midiOutSequencer.setSequence(sequence);

        midiOutSequencer.start();

    }
}

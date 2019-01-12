package nl.stokpop.jmidi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public class JMidi {

    public void createSequencer() throws MidiUnavailableException {

        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : midiDeviceInfo) {
            System.out.printf("[%s] [%s] [%s] [%s]%n", info.getName(), info.getDescription(), info.getVendor(), info.getVersion());
        }

        Sequencer sequencer = MidiSystem.getSequencer();

    }

}

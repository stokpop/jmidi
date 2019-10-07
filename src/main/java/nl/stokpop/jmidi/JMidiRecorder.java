package nl.stokpop.jmidi;

import nl.stokpop.jmidi.util.MidiUtil;

import javax.sound.midi.*;
import java.util.Optional;

public class JMidiRecorder {

    public static void main(String[] args) throws MidiUnavailableException {
        MidiController.printDevices();
        if (args.length != 1) {
            System.err.println("Please provide part of midi device name as argument.");
            System.exit(1);
        }
        String name = args[0];
        MidiController.openMidiDeviceTransmitter(name)
                .ifPresentOrElse(JMidiRecorder::record, () -> { throw new RuntimeException("Midi transmitter with name " + name + " not found."); });
    }

    private static void record(MidiDevice transmitterDevice) {

        try {
            Transmitter transmitter = transmitterDevice.getTransmitter();
            transmitter.setReceiver(new Receiver() {
                @Override
                public void send(MidiMessage message, long timeStamp) {
                    System.out.printf("%-10d played: %s%n", timeStamp, MidiUtil.printMessageInfo(message));
                    // MidiUtil.printCode(message, timeStamp).ifPresent(System.out::println);
                }

                @Override
                public void close() {

                }
            });
        } catch (MidiUnavailableException e) {
           throw new RuntimeException(e);
        }

    }


}

package nl.stokpop.midiflux;

import reactor.core.publisher.Flux;

import javax.sound.midi.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class MidiController {
    public static void printInstruments() throws MidiUnavailableException {
        System.out.println("Print devices");
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        Instrument[] availableInstruments = synthesizer.getAvailableInstruments();

        Flux.fromArray(availableInstruments)
                .doOnNext(i -> System.out.println(i.getName())).blockLast();

    }

    public static void printDevices() throws MidiUnavailableException {
        System.out.println("Print devices");
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        for (int i = 0, midiDeviceInfosLength = midiDeviceInfos.length; i < midiDeviceInfosLength; i++) {
            MidiDevice.Info info = midiDeviceInfos[i];
            MidiDevice midiOutDevice = MidiSystem.getMidiDevice(info);
            int maxReceivers = midiOutDevice.getMaxReceivers();
            int maxTransmitters = midiOutDevice.getMaxTransmitters();
            System.out.printf("[%3d] %30s, %34s, %24s, mt: %2d, mr: %2d%n", i, info.getDescription(), info.getName(), info.getVendor(), maxTransmitters, maxReceivers);
        }
    }

    public static Optional<MidiDevice> openMidiDevice(int index) {
        System.out.printf("List and open midiflux device: %d%n", index);
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        try {
            MidiDevice midiOutDevice = MidiSystem.getMidiDevice(midiDeviceInfos[index]);
            midiOutDevice.open();
            return Optional.of(midiOutDevice);
        } catch (MidiUnavailableException e) {
            System.out.println("Midi device not available: " + e);
            return Optional.empty();
        }

    }

    public static Optional<MidiDevice> openMidiDevice(String name) throws MidiUnavailableException {
        System.out.printf("List and open midiflux device: %s%n", name);
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        Optional<MidiDevice> midiDevice = Stream.of(midiDeviceInfos)
                .filter(m -> m.getName().contains(name))
                .map(wrapInRuntimeException(MidiSystem::getMidiDevice))
                .filter(d -> d.getMaxReceivers() != 0)
                .findFirst();

        if (midiDevice.isPresent()) {
            midiDevice.get().open();
        }

        return midiDevice;
    }

    public static void playNote(Receiver midiOutReceiver, Note note, Channel channel) {

        try {
            ShortMessage msg1 = new ShortMessage();
            msg1.setMessage(ShortMessage.NOTE_ON, channel.getNr(), note.getNote(), note.getVelocity());
            midiOutReceiver.send(msg1, 0);

            try {
                Thread.sleep(note.getDuration());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ShortMessage msg2 = new ShortMessage();
            msg2.setMessage(ShortMessage.NOTE_ON, channel.getNr(), note.getNote(), 0);
            midiOutReceiver.send(msg2, 0);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<T,R> {
        R apply(T t) throws Exception;
    }

    public static <T,R> Function<T,R> wrapInRuntimeException(CheckedFunction<T,R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

package nl.stokpop.jmidi;

import nl.stokpop.midiflux.Channel;
import nl.stokpop.midiflux.Note;
import reactor.core.publisher.Flux;

import javax.sound.midi.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MidiController {

    private final static char CHAR_INFINITY = '\u221E';

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
            String maxReceivers = toInfinityAndBeyond(midiOutDevice.getMaxReceivers());
            String maxTransmitters = toInfinityAndBeyond(midiOutDevice.getMaxTransmitters());
            System.out.printf("[%3d] %30s, %34s, %24s, trs: %2s, rcs: %2s%n", i, info.getDescription(), info.getName(), info.getVendor(), maxTransmitters, maxReceivers);
        }
    }

    private static String toInfinityAndBeyond(int number) {
        return number == -1 ? String.valueOf(CHAR_INFINITY) : String.valueOf(number);
    }

    public static Optional<MidiDevice> openMidiDevice(int index) {
        System.out.printf("List and open midi device: %d%n", index);
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


    public static Optional<MidiDevice> openMidiDeviceReciever(String name) throws MidiUnavailableException {
        System.out.printf("List and open receiver midi device: %s%n", name);
        return openMidiDevice(name, d -> d.getMaxReceivers() != 0);
    }

    public static Optional<MidiDevice> openMidiDeviceTransmitter(String name) throws MidiUnavailableException {
        System.out.printf("List and open transmitter midi device: %s%n", name);
        return openMidiDevice(name, d -> d.getMaxTransmitters() != 0);
    }

    private static Optional<MidiDevice> openMidiDevice(String name, Predicate<MidiDevice> midiDevicePredicate) throws MidiUnavailableException {
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        Optional<MidiDevice> midiDevice = Stream.of(midiDeviceInfos)
                .filter(m -> m.getName().contains(name))
                .map(wrapInRuntimeException(MidiSystem::getMidiDevice))
                .filter(midiDevicePredicate)
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

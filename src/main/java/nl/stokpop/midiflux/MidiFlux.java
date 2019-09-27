package nl.stokpop.midiflux;

import reactor.core.publisher.Flux;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MidiFlux {

    public MidiFlux() {

    }

    public static void main(String[] args) throws MidiUnavailableException {

        MidiFlux.printDevices();

        MidiFlux.printInstruments();

        MidiFlux stokpopMidiFlux = new MidiFlux();

        try {

            String midiFile = "disco.mid";
            InputStream music = MidiFlux.class.getClassLoader().getResourceAsStream(midiFile);
            if (music == null) {
                throw new RuntimeException("File not found: " + midiFile);
            }
            Sequence sequence = MidiSystem.getSequence(music);
            MidiDevice midiOutDevice = openMidiDevice(0);
            stokpopMidiFlux.startExternal(midiOutDevice, sequence);
            //stokpopMidiFlux.startSimple(sequence);
            stokpopMidiFlux.startFlux(midiOutDevice);

            midiOutDevice.close();


        } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
            System.err.printf("Error: %s%n", e);
        }

    }

    private static void printInstruments() throws MidiUnavailableException {
        System.out.println("Print devices");
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        Instrument[] availableInstruments = synthesizer.getAvailableInstruments();

        Flux.fromArray(availableInstruments)
                .doOnNext(i -> System.out.println(i.getName())).blockLast();

    }

    private void startSimple(Sequence sequence) throws InvalidMidiDataException, MidiUnavailableException {
        System.out.println("Start simple play.");
        Sequencer sequencer;
        sequencer = MidiSystem.getSequencer();
        sequencer.open();

//            for (Track track : sequence.getTracks())
//            {
//                for (int c = 0; c < 16; c++)
//                    track.add(new MidiEvent(
//                            new ShortMessage(ShortMessage.CONTROL_CHANGE, c, 7, 0),
//                            track.ticks()));
//            }

        sequencer.setSequence(sequence);
        sequencer.start();

    }

    public void startExternal(MidiDevice midiOutDevice, Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
        System.out.println("Start external play.");

        Receiver midiOutReceiver = midiOutDevice.getReceiver();

        Sequencer midiOutSequencer = MidiSystem.getSequencer(false);


        //Add the new MIDI out device here.
        midiOutSequencer.getTransmitter().setReceiver(midiOutReceiver);
        midiOutSequencer.open();

        midiOutSequencer.setSequence(sequence);

        midiOutSequencer.start();

    }

    private static void printDevices() throws MidiUnavailableException {
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

    private static MidiDevice openMidiDevice(int index) throws MidiUnavailableException {
        System.out.printf("List and open midiflux device: %d%n", index);
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        MidiDevice midiOutDevice = MidiSystem.getMidiDevice(midiDeviceInfos[index]);
        midiOutDevice.open();

        return midiOutDevice;
    }

    public void startFlux(MidiDevice midiOutDevice) throws MidiUnavailableException {
        System.out.println("Start flux.");

        Receiver midiOutReceiver = midiOutDevice.getReceiver();

        List<Note> notes = List.of(
                new Note(50, 400),
                new Note(33, 200),
                new Note(45, 500),
                new Note(50, 400),
                new Note(33, 200),
                new Note(45, 500),
                new Note(60, 400),
                new Note(33, 200),
                new Note(40, 500)
                );


        Flux<Note> noteGenerator = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(notes.get(state % notes.size()));
                    return state + 1;
                });

        noteGenerator
                .doOnNext(note -> playNote(midiOutReceiver, note))
                .blockLast();

        midiOutReceiver.close();

    }

    private static void playNote(Receiver midiOutReceiver, Note note) {

        try {
            ShortMessage msg1 = new ShortMessage();
            msg1.setMessage(ShortMessage.NOTE_ON, 0, note.getNote(), 70);
            midiOutReceiver.send(msg1, 0);

            try {
                Thread.sleep(note.getDuration());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ShortMessage msg2 = new ShortMessage();
            msg2.setMessage(ShortMessage.NOTE_ON, 0, note.getNote(), 0);
            midiOutReceiver.send(msg2, 0);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

}
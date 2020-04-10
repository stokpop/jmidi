package nl.stokpop.midiflux;

import nl.stokpop.jmidi.MidiController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MidiFlux {

    public static final int BASE_DRUM = 36;
    public static final int SNARE_DRUM = 38;

    public MidiFlux() {

    }

    public static void main(String[] args) throws MidiUnavailableException {

        MidiController.printDevices();

        if (args.length != 1) {
            System.err.println("Please provide part of midi device name as argument.");
            System.exit(1);
        }
        String name = args[0];

        MidiFlux stokpopMidiFlux = new MidiFlux();

        try {

            Optional<MidiDevice> midiOutDevice = MidiController.openMidiDeviceReceiver(name);

            if (midiOutDevice.isPresent()) {
                stokpopMidiFlux.startFlux(midiOutDevice.get());
            }
            else {
                System.out.println("Midi device not available.");
            }

            //midiOutDevice.close();


        } catch (MidiUnavailableException e) {
            System.err.printf("Error: %s%n", e);
        }

    }


    public void startFlux(MidiDevice midiOutDevice) throws MidiUnavailableException {
        System.out.println("Start flux.");

        Receiver midiOutReceiver = midiOutDevice.getReceiver();

//        List<Note> notes = List.of(
//                new Note(50, 400, 70),
//                new Note(33, 200, 70),
//                new Note(45, 500, 70),
//                new Note(50, 400, 77),
//                new Note(33, 200, 70),
//                new Note(45, 500, 70),
//                new Note(60, 400, 45),
//                new Note(40, 200, 70),
//                new Note(42, 400, 100),
//                new Note(40, 500, 70)
//                );

        List<Note> notes = List.of(
                new Note(55, 500, 61),
                new Note(59, 500, 65),
                new Note(53, 500, 63),
                new Note(52, 500, 84)
        );

        List<Note> drums = List.of(
                new Note(BASE_DRUM, 500, 90),
                new Note(SNARE_DRUM, 500, 90),
                new Note(BASE_DRUM, 250, 90),
                new Note(BASE_DRUM, 250, 90),
                new Note(SNARE_DRUM, 500, 90)
        );

        Flux<Note> noteGenerator = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(notes.get(state % notes.size()));
                    return state + 1;
                });

        noteGenerator
                .log()
                .doOnNext(note -> MidiController.playNote(midiOutReceiver, note, Channel.c1))
                .subscribeOn(Schedulers.newSingle("tones"))
                .subscribe();

        Flux<Note> drumGenerator = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(drums.get(state % drums.size()));
                    return state + 1;
                });

        drumGenerator
                .log()
                .doOnNext(note -> MidiController.playNote(midiOutReceiver, note, Channel.c2))
                .subscribeOn(Schedulers.newSingle("drums"))
                .subscribe();

//        try {s
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }

    private List<Note> generateNotes() {

        return IntStream.range(1,100).mapToObj(i -> new Note(i, 100)).collect(Collectors.toList());

    }

}
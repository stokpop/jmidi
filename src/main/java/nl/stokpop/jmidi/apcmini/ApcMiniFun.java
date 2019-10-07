package nl.stokpop.jmidi.apcmini;

import nl.stokpop.jmidi.MidiController;
import reactor.core.publisher.Flux;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nl.stokpop.jmidi.apcmini.LedColor.green;
import static nl.stokpop.jmidi.apcmini.LedColor.off;
import static nl.stokpop.jmidi.apcmini.LedColor.red;
import static nl.stokpop.jmidi.apcmini.LedColor.yellow;
import static nl.stokpop.jmidi.apcmini.LedColor.yellow_blink;

/**
 * Check specs here: https://getsatisfaction.com/akai_professional/topics/midi-information-for-apc-mini
 */
public class ApcMiniFun {
    
    protected static final int LED_DELAY = 25;

    public ApcMiniFun() {

    }

    public static void main(String[] args) throws MidiUnavailableException {

        MidiController.printDevices();

        String name = "APC";

        ApcMiniFun apcMiniFun = new ApcMiniFun();

        try {

            Optional<MidiDevice> midiOutDevice = MidiController.openMidiDeviceReciever(name);

            if (midiOutDevice.isPresent()) {
                MidiDevice midiDevice = midiOutDevice.get();
                apcMiniFun.start(midiDevice);
                midiDevice.close();
            }
            else {
                System.out.println("Midi device not available.");
            }
        } catch (MidiUnavailableException e) {
            System.err.printf("Error: %s%n", e);
        }

    }

    private void start(MidiDevice midiDevice) throws MidiUnavailableException {
        Receiver receiver = midiDevice.getReceiver();

        Flux.range(0, 64)
                .map(i -> new Button(i, green))
                .log()
                .map(Button::midiMessage)
                .delayElements(Duration.ofMillis(LED_DELAY))
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

        Flux.range(0, 64)
                .map(i -> new Button(i, yellow))
                .log()
                .map(Button::midiMessage)
                .delayElements(Duration.ofMillis(LED_DELAY))
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

        Flux.range(0, 64)
                .map(i -> new Button(i, red))
                .log()
                .map(Button::midiMessage)
                .delayElements(Duration.ofMillis(LED_DELAY))
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

        Flux.range(0, 64)
                .map(i -> new Button(i, off))
                .log()
                .map(Button::midiMessage)
                .delayElements(Duration.ofMillis(LED_DELAY))
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

        createLayout(ledLayout())
                .map(Button::midiMessage)
                .log()
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

    }

    Flux<Button> createLayout(LedColor[][] ledColors) {
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttons.add(new Button((8 * i + j), ledColors[i][j]));
            }
        }
        return Flux.fromIterable(buttons);
    }
    
    LedColor[][] ledLayout() {
        return new LedColor[][] {
            { green, off, green, off, green, red, off, yellow },
            { yellow, off, green, off, green, red, off, yellow },
            { green, off, green, off, green, red, off, yellow },
            { green, off, green, off, green, red, off, yellow },
            { green, off, green, off, green, red, off, yellow },
            { green, off, green, off, green, red, off, yellow },
            { green, off, green, off, yellow_blink, red, off, yellow },
            { green, off, green, off, green, red, off, yellow }
        };
    }

}
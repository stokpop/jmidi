package nl.stokpop.jmidi.apcmini;

import nl.stokpop.jmidi.MidiController;
import reactor.core.publisher.Flux;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
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
    
    protected static final int LED_DELAY = 20;
    protected static final int LETTER_DELAY = 500;

    public ApcMiniFun() {

    }

    public static void main(String[] args) throws MidiUnavailableException, IOException, FontFormatException {

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
                .log()
                .map(Button::midiMessage)
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();

        Flux.fromArray("HELLO WORLD 1234567890".split(""))
                .log()
                .delayElements(Duration.ofMillis(LETTER_DELAY))
                .map(s -> s.charAt(0))
                .doOnNext(c -> createLetter(receiver, c))
                .blockLast();
        
    }

    private void createLetter(Receiver receiver, char a) {
        createLayout(createCharLeds(a))
                .log()
                .map(Button::midiMessage)
                .doOnNext(m -> receiver.send(m, 0L))
                .subscribe();
    }

    LedColor[][] createCharLeds(char c) {

        BitSet bits = HexToBits.convertTo6x8charIn8x8Raster(c);

        LedColor[][] ledColors = new LedColor[8][8];

        int sizeX = 8;
        int sizeY = 8;

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                ledColors[i][j] = bits.get(i * 8 + (8 - j)) ? green : off;
            }
        }
        return ledColors;
    }

    Flux<Button> createLayout(LedColor[][] ledColors) {
        int sizeX = 8;
        int sizeY = 8;
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                buttons.add(new Button((sizeX * i + j), ledColors[sizeX - 1 - i][j]));
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
package nl.stokpop.jmidi.apcmini;

import nl.stokpop.jmidi.MidiController;
import reactor.core.publisher.Flux;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
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
 *
 * Be sure to connect an apc-mini with usb.
 */
public class ApcMiniFun {
    
    private static final int LED_DELAY = 20;
    private static final int LETTER_DELAY = 80;
    private static final int SIZE_X = 8;
    private static final int SIZE_Y = 8;


    private ApcMiniFun() {

    }

    public static void main(String[] args) throws MidiUnavailableException {

        MidiController.printDevices();

        String name = "APC";

        ApcMiniFun apcMiniFun = new ApcMiniFun();

        try {

            Optional<MidiDevice> midiOutDevice = MidiController.openMidiDeviceReceiver(name);

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

        int count = 0;

        String message = "  HELLO WORLD! WHAT A COOL WAY TO SHOW SOME LETTERS! Signed by Peter Paul !@&#^*$^(<.>,;' ";

        BitSet c1 = BitSet.valueOf(HexToBits.convertTo6x8charIn8x8Raster(message.charAt(0)));
        BitSet c2 = BitSet.valueOf(HexToBits.convertTo6x8charIn8x8Raster(message.charAt(1)));

        while (count++ < 1_000_000) {

            if (count % 8 == 0) {
                // next letter
                c1 = c2;
                int index = ((count / 8) + 1) % message.length();
                c2 = BitSet.valueOf(HexToBits.convertTo6x8charIn8x8Raster(message.charAt(index)));
            }

            LedColor[][] charLeds = createCharLeds(c1, c2, count % 8);

            createLayout(charLeds)
                .map(Button::midiMessage)
                .doOnNext(m -> receiver.send(m, 0L))
                .blockLast();
                try {
                    Thread.sleep(LETTER_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    private int[] createBuffer(char c1, char c2) {
        byte[] bytes1 = HexToBits.convertTo6x8charIn8x8Raster(c1);
        byte[] bytes2 = HexToBits.convertTo6x8charIn8x8Raster(c2);
        int[] buffer = new int[SIZE_Y];
        for (int i = 0; i < SIZE_Y; i++) {
            int shifted = bytes2[i] << 8;
            buffer[i] = shifted | bytes1[i];
        }
        return buffer;
    }

    private byte[] fromBuffer(int[] buffer) {
        byte[] bytes = new byte[SIZE_Y];
        for (int i = 0; i < SIZE_Y; i++) {
            bytes[i] = (byte) (buffer[i]) ;
        }
        return bytes;
    }

    private void createLetter(Receiver receiver, BitSet bitSet8x8) {
        createLayout(createCharLeds(bitSet8x8))
                //.log()
                .map(Button::midiMessage)
                .doOnNext(m -> receiver.send(m, 0L))
                .subscribe();
    }

    private int[] shiftLeft(int[] buffer) {
        int[] shifted = new int[buffer.length];

        for (int i = 0; i < buffer.length; i++) {
            shifted[i] = buffer[i] << 1;
        }

        return shifted;
    }

    private LedColor[][] createCharLeds(BitSet bits) {

        LedColor[][] ledColors = new LedColor[SIZE_X][SIZE_Y];

        for (int i = 0; i < SIZE_X; i++) {
            for (int j = 0; j < SIZE_Y; j++) {
                ledColors[i][j] = bits.get(i * SIZE_Y + (SIZE_X - j - 1)) ? green : off;
            }
        }
        return ledColors;
    }

    private LedColor[][] createCharLeds(BitSet bits1, BitSet bits2, int offset) {

        LedColor[][] ledColors = new LedColor[SIZE_X][SIZE_Y];

        for (int i = 0; i < SIZE_Y; i++) {
            for (int j = 0; j < SIZE_X; j++) {
                int locationX = j + offset;
                boolean bitX = locationX < 8
                        ? bits1.get(i * SIZE_Y + (SIZE_X - locationX - 1))
                        : bits2.get(i * SIZE_Y + (SIZE_X - (locationX - SIZE_X) - 1));
                ledColors[i][j] =  bitX ? green : off;
            }
        }
        return ledColors;
    }

    private Flux<Button> createLayout(LedColor[][] ledColors) {
        int sizeX = SIZE_X;
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < SIZE_Y; j++) {
                buttons.add(new Button((sizeX * i + j), ledColors[sizeX - 1 - i][j]));
            }
        }
        return Flux.fromIterable(buttons);
    }
    
    private LedColor[][] ledLayout() {
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
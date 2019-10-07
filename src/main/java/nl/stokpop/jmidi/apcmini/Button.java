package nl.stokpop.jmidi.apcmini;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class Button {
    private int ledNumber;
    private LedColor color;

    public Button(int ledNumber, LedColor color) {
        this.ledNumber = ledNumber;
        this.color = color;
    }

    public ShortMessage midiMessage() {
        try {
            return new ShortMessage(ShortMessage.NOTE_ON, 0, ledNumber, color.getColorCode());
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException("Cannot create midi message.", e);
        }
    }

    @Override
    public String toString() {
        return "Button{" + "ledNumber=" + ledNumber +
                ", color=" + color +
                '}';
    }
}

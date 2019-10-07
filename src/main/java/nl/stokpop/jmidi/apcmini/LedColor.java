package nl.stokpop.jmidi.apcmini;

public enum LedColor {
    
    off(0), green(1), red(3), yellow(5), green_blink(2), red_blink(4), yellow_blink(6);

    private int colorCode;

    LedColor(int colorCode) {
        this.colorCode = colorCode;
    }

    public int getColorCode() {
        return colorCode;
    }
}

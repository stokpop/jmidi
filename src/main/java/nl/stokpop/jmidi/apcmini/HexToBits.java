package nl.stokpop.jmidi.apcmini;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class HexToBits {

    public static void main(String[] args) {
        char c = 'T';
        BitSet bitSet = convertTo6x8charIn8x8Raster(c);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(bitSet.get(i * 8 + (8 - j)) ? "X" : ".");
            }
            System.out.println("");
        }
    }

    public static BitSet convertTo6x8charIn8x8Raster(char c) {

        byte[] bytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) MiniFont.console_font_6x8[c * 8 + i];
        }

        return BitSet.valueOf(bytes);
    }


    private static List<String> convert(String input) {
        String bytes = input.contains(":") ? input.split(":")[1] : input;
        List<String> twoChars = new ArrayList<>();
        for (int i = 0; i < bytes.length()/2 ; i++) {
            twoChars.add(bytes.substring(i * 2, i * 2 + 2));
        }

        for (String s : twoChars) {
            Byte hexToByte = hexToByte(s);
            String toBinaryString = Integer.toBinaryString(hexToByte);
            String replace = String.format("%8s", toBinaryString).replace(' ', '0');
            System.out.println(replace);
        }


        return twoChars;
    }

    private static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }
}

package nl.stokpop.jmidi;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum MetaMessageType {

    SEQUENCE_NUMBER(0x00, "sequence_number", MidiBytesToIntList.singleton),
    TEXT(0x01, "text", MidiBytesToString.singleton),
    COPYRIGHT(0x02, "copyright", MidiBytesToString.singleton),
    TRACK_NAME(0x03, "track_name", MidiBytesToString.singleton),
    INTSTRUMENT_NAME(0x04, "instrument_name", MidiBytesToString.singleton),
    LYRICS(0x05, "lyrics", MidiBytesToString.singleton),
    END_OF_TRACK(0x2F, "end_of_track", MidiBytesToIntList.singleton),
    SET_TEMPO(0x51, "set_tempo", MidiBytesToTempo.singleton),
    TIME_SIGNATURE(0x58, "time_signature", MidiBytesToIntList.singleton),
    UNKNOWN(-1, "unknown", MidiBytesToIntList.singleton);

    private int code;
    private String name;
    private MidiBytesConverter converter;

    private static final Map<Integer,MetaMessageType> typeToEnum =
            Arrays.stream(values()).collect(Collectors.toMap(type -> type.getCode(), type -> type));

    MetaMessageType(int code, String name, MidiBytesConverter converter) {
        this.code = code;
        this.name = name;
        this.converter = converter;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String convertBytes(byte[] midiBytes) {
        return converter.convertBytes(midiBytes);
    }

    public static MetaMessageType codeToType(int code) {
        return Optional.ofNullable(typeToEnum.get(code)).orElse(UNKNOWN);
    }

}

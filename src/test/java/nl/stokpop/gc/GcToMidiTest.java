package nl.stokpop.gc;

import org.junit.Assert;

import java.util.regex.Matcher;

public class GcToMidiTest {

    @org.junit.Test
    public void testPattern() {
        String input = "2019-05-20T09:04:55.181-0100: 0.436: [GC pause (G1 Evacuation Pause) (young), 0.0031126 secs]";
        Matcher matcher = GcToMidi.g1PauseTimePattern.matcher(input);
        Assert.assertTrue("should match", matcher.matches());
        Assert.assertEquals("2019-05-20T09:04:55.181-0100", matcher.group("timestamp"));
        Assert.assertEquals("0.436", matcher.group("time"));
        Assert.assertEquals("(G1 Evacuation Pause) (young)", matcher.group("gcname"));
        Assert.assertEquals("0.0031126", matcher.group("durationsecs"));
    }

}
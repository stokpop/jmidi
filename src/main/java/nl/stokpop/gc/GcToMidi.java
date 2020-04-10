package nl.stokpop.gc;

import nl.stokpop.jmidi.MidiController;
import nl.stokpop.midiflux.Channel;
import nl.stokpop.midiflux.Note;
import nl.stokpop.midiflux.TimedNote;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.function.Predicate.not;

public class GcToMidi {

//        CommandLine flags: -XX:InitialHeapSize=134217728 -XX:MaxHeapSize=2147483648 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC
//        2019-05-19T21:13:50.927-0100: 0.571: [GC (Allocation Failure) [PSYoungGen: 33280K->2032K(38400K)] 33280K->2040K(125952K), 0.0078202 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
//        2019-05-19T21:13:51.172-0100: 0.816: [GC (Allocation Failure) [PSYoungGen: 35312K->2368K(71680K)] 35320K->2384K(159232K), 0.0039697 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
//        2019-05-19T21:13:51.582-0100: 1.226: [GC (Allocation Failure) [PSYoungGen: 68928K->3248K(71680K)] 68944K->3272K(159232K), 0.0062535 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
//        2019-05-19T21:13:51.774-0100: 1.418: [GC (Allocation Failure) [PSYoungGen: 69808K->3584K(138240K)] 69832K->3616K(225792K), 0.0056002 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
//        2019-05-19T21:13:52.178-0100: 1.822: [GC (Allocation Failure) [PSYoungGen: 136704K->4496K(138240K)] 136736K->4536K(225792K), 0.0100812 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
//        2019-05-19T21:13:52.644-0100: 2.288: [GC (Allocation Failure) [PSYoungGen: 137616K->5104K(269824K)] 137656K->5944K(357376K), 0.0111873 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
//        2019-05-19T21:13:52.700-0100: 2.344: [GC (Metadata GC Threshold) [PSYoungGen: 41075K->3152K(271360K)] 41915K->6071K(358912K), 0.0083280 secs] [Times: user=0.03 sys=0.00, real=0.00 secs]
//        2019-05-19T21:13:52.709-0100: 2.353: [Full GC (Metadata GC Threshold) [PSYoungGen: 3152K->0K(271360K)] [ParOldGen: 2919K->5660K(50688K)] 6071K->5660K(322048K), [Metaspace: 20512K->20512K(1067008K)], 0.0495993 secs] [Times: user=0.15 sys=0.00, real=0.05 secs]
//        2019-05-19T21:13:54.958-0100: 4.602: [GC (Allocation Failure) [PSYoungGen: 264704K->6112K(442880K)] 270364K->12204K(493568K), 0.0127499 secs] [Times: user=0.03 sys=0.01, real=0.02 secs]
//        2019-05-19T21:13:56.823-0100: 6.467: [GC (Metadata GC Threshold) [PSYoungGen: 330072K->7156K(534528K)] 336165K->21070K(585216K), 0.0263063 secs] [Times: user=0.08 sys=0.01, real=0.03 secs]
//        2019-05-19T21:13:56.850-0100: 6.494: [Full GC (Metadata GC Threshold) [PSYoungGen: 7156K->0K(534528K)] [ParOldGen: 13914K->19780K(86016K)] 21070K->19780K(620544K), [Metaspace: 33676K->33676K(1079296K)], 0.1115246 secs] [Times: user=0.32 sys=0.01, real=0.11 secs]
//        2019-05-19T21:13:59.743-0100: 9.387: [GC (Allocation Failure) [PSYoungGen: 527360K->9556K(606720K)] 547140K->29344K(692736K), 0.0198110 secs] [Times: user=0.07 sys=0.00, real=0.02 secs]
//        2019-05-19T21:14:43.515-0100: 53.160: [GC (Allocation Failure) --[PSYoungGen: 605012K->605012K(606720K)] 624800K->701307K(703488K), 0.2147210 secs] [Times: user=0.29 sys=0.07, real=0.22 secs]
//        2019-05-19T21:14:43.730-0100: 53.374: [Full GC (Ergonomics) [PSYoungGen: 605012K->0K(606720K)] [ParOldGen: 96295K->96758K(180224K)] 701307K->96758K(786944K), [Metaspace: 45630K->45619K(1091584K)], 0.2844998 secs] [Times: user=0.79 sys=0.02, real=0.28 secs

//    // ParGC
//    private final Map<String, Integer> textToNote =
//            Map.of("GC (Allocation Failure)", 55,
//                    "GC (Metadata GC Threshold)", 60,
//                    "Full GC (Metadata GC Threshold)", 65,
//                    "Full GC (Ergonomics)", 70);

    static Pattern g1PauseTimePattern =
            Pattern.compile("(?<timestamp>.*?): (?<time>.*?): \\[(?<gcname>.*?), (?<durationsecs>.*?) secs\\]");

    // G1
    //2019-05-20T09:04:55.181-0100: 0.436: [GC pause (G1 Evacuation Pause) (young), 0.0031126 secs]
    //2019-05-20T09:04:57.530-0100: 2.785: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.0101666 secs]
    //2019-05-20T09:06:31.900-0100: 97.157: [GC pause (G1 Evacuation Pause) (mixed), 0.2931263 secs]
    //2019-05-20T09:06:45.103-0100: 110.360: [GC pause (G1 Humongous Allocation) (young) (initial-mark), 0.1067504 secs]
    //2019-05-20T09:07:07.688-0100: 132.945: [GC concurrent-root-region-scan-end, 0.0228507 secs]
    
    //(.*):(.*): [GC pause (.*), (.*) secs]
    //timestamp:
    private final Map<String, Integer> gcPauseG1ToNote =
            Map.of("GC pause (G1 Evacuation Pause) (young)", 55,
                    "GC pause (Metadata GC Threshold) (young) (initial-mark)", 60,
                    "GC pause (G1 Evacuation Pause) (mixed)", 65,
                    "GC pause (G1 Humongous Allocation) (young) (initial-mark)", 70,
                        "GC concurrent-root-region-scan-end", 75,
                            "GC concurrent-mark-end", 80);

    public static void main(String[] args) throws MidiUnavailableException {

        if (args.length != 1) {
            System.err.println("Please supply a gc log file path.");
            System.exit(1);
        }

        MidiController.printDevices();

        String midiDeviceName = "Gervill";
        MidiDevice midiOutDevice =
                MidiController.openMidiDeviceReceiver(midiDeviceName)
                .orElseThrow(() -> new MidiUnavailableException("Midi receiver not found: " + midiDeviceName));

        Receiver receiver = midiOutDevice.getReceiver();

        String gcFilePath = args[0];
        
        TailCallback callback = new TailCallback() {
            @Override
            public void newLine(String line) {
                emitter.next(line);
            }
        }
        SimpleTail simpleTail = new SimpleTail(file, )

        Flux<String> tailFlux = Flux.create(emitter -> {
            emitter.next(line);
        });

        GcToMidi gcToMidi = new GcToMidi();

        Flux<String> gcEntries = fromPath(Path.of(gcFilePath));

        gcEntries.take(10).subscribe(System.out::println);

        Flux<TimedNote> timedNotes = gcToMidi.transformGcEntriesIntoMidi(gcEntries);

        long startTime = System.currentTimeMillis();
        int speedup = 1000;

        timedNotes.subscribe(new Consumer<>() {
            @Override
            public void accept(TimedNote timedNote) {
                waitToPlayNote(timedNote.getTime());
                playNote(timedNote.getNote());
            }

            private void waitToPlayNote(long time) {
                long waitTime = startTime + time - System.currentTimeMillis();
                try {
                    long wait = waitTime < 0 ? 0 : waitTime;
                    long adjustedWait = wait / speedup;
                    System.out.println("wait " + adjustedWait + " ms");
                    sleep(adjustedWait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            private void playNote(Note note) {
                System.out.println(note);
                MidiController.playNote(receiver, note, Channel.c1);
            }
        });
    }

    private Flux<TimedNote> transformGcEntriesIntoMidi(Flux<String> gcEntries) {
        return gcEntries.map(this::gcLineToTimedNote)
                .filter(not(Optional::isEmpty))
                .map(Optional::get);
    }



    private Optional<TimedNote> gcLineToTimedNote(String gcLine) {
        Matcher matcher = g1PauseTimePattern.matcher(gcLine);
        if (matcher.matches()) {
            String time = matcher.group("time");
            String gcname = matcher.group("gcname");
            String durationsecs = matcher.group("durationsecs");
            return Optional.of(createTimedNote(time, gcname, durationsecs));
        }
        else {
            return Optional.empty();
        }
    }

    private TimedNote createTimedNote(String time, String gcName, String durationsecs) {
        long relativeTimeMs = (long) (Double.parseDouble(time) * 1000);

        int note = gcTypeStringToNote(gcName)
                .orElse(45);
        
        // () -> new RuntimeException("unexpected gctype found: " + gcName)
        int durationMs = 0;
        try {
            durationMs = (int) (Double.parseDouble(durationsecs) * 1000);
        } catch (NumberFormatException e) {
            System.out.println("Cannot parse: " + durationsecs);
            durationMs = 100;
        }
        int velocity = 100;

        return new TimedNote(relativeTimeMs, note, durationMs, velocity);
    }

    private Optional<Integer> gcTypeStringToNote(String gcName) {
        // should a flux/mono be used here instead?
        return gcPauseG1ToNote.entrySet().stream()
                .filter(e -> gcName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
    
    private static Flux<String> fromPath(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }

}

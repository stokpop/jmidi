package nl.stokpop.jmidi;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;

public class StokpopMidi {

    public StokpopMidi() {

    }

    public static void main(String[] args) {

        StokpopMidi stokpopMidi = new StokpopMidi();

        try {

            printDevices();

            String midiFilePath = "/disco.mid";
            int midiDeviceOutNumber = 3;

            InputStream music = StokpopMidi.class.getResourceAsStream(midiFilePath);

            if (music == null) {
                throw new RuntimeException("Cannot find file on classpath: " + midiFilePath);
            }
            Sequence sequence = MidiSystem.getSequence(music);

            Sequence extractedSeq = extractTrack(sequence, 1);

            MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            if (midiDeviceOutNumber >= midiDeviceInfos.length) {
                printDevices();
                throw new RuntimeException(String.format("Cannot select midi device %d, available devices are [0 to %d]", midiDeviceOutNumber, midiDeviceInfos.length - 1));
            }

            MidiDevice midiOutDevice = MidiSystem.getMidiDevice(midiDeviceInfos[midiDeviceOutNumber]);
            midiOutDevice.open();
            stokpopMidi.startExternalPlay(extractedSeq, midiOutDevice);

        } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
            System.err.printf("Error: %s%n", e);
        }

    }

    private static Sequence extractTrack(Sequence sequence, int trackNumber) throws InvalidMidiDataException {
        Sequence extractSequence = new Sequence(sequence.getDivisionType(), sequence.getResolution(), 1);

        Track[] tracks = sequence.getTracks();
        if (trackNumber >= tracks.length) {
                         throw new RuntimeException(String.format("Cannot select track %d, available tracks are [0 to %d]", trackNumber, tracks.length - 1));
        }

        Track trackToCopy = tracks[trackNumber];

        Track newTrack = extractSequence.createTrack();

        System.out.println("Extracting track of size: " + trackToCopy.size());
        for (int i = 0; i < trackToCopy.size(); i++) {
            MidiEvent event = trackToCopy.get(i);
            MidiMessage eventMessage = event.getMessage();
            if (eventMessage instanceof ShortMessage) {
                ShortMessage shortMessage = (ShortMessage) eventMessage;
                int command = shortMessage.getCommand();
                if (command == MidiUtil.NOTE_ON || command == MidiUtil.NOTE_OFF) {
                    int key = shortMessage.getData1();
                    int velocity = shortMessage.getData2();
                    long tick = event.getTick();
                    MidiEvent midiEvent = MidiUtil.makeEvent(command, 1, key, velocity, tick);
                    newTrack.add(midiEvent);
                }
            }
        }

        return extractSequence;
    }

    public static void printDevices() {
        MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

        for (int i = 0, midiDeviceInfosLength = midiDeviceInfos.length; i < midiDeviceInfosLength; i++) {
            MidiDevice.Info info = midiDeviceInfos[i];
            System.out.printf("[%d] %s, %s, %s, %s%n", i, info.getDescription(), info.getName(), info.getVersion(), info.getVendor());
        }
    }

    public void startExternalPlay(Sequence sequence, MidiDevice midiOutDevice) throws MidiUnavailableException, InvalidMidiDataException {
        System.out.println("Start external play on device: " + midiOutDevice.getDeviceInfo());

        midiOutDevice.open();

        Receiver midiOutReceiver = midiOutDevice.getReceiver();
        Sequencer midiOutSequencer = MidiSystem.getSequencer(false);

        midiOutSequencer.getTransmitter().setReceiver(midiOutReceiver);
        midiOutSequencer.open();

        midiOutSequencer.setSequence(sequence);

        midiOutSequencer.start();

        boolean stopped = false;

        while (!stopped) {

            if (!midiOutSequencer.isRunning()) {
                midiOutSequencer.close();
                stopped = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished external play on device: " + midiOutDevice.getDeviceInfo());

    }






}
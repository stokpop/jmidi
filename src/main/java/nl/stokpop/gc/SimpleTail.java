package nl.stokpop.gc;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Found this here: https://coderwall.com/p/0vmcvq/simple-tail-in-java-with-shutdownhook
 * <p>
 * based on http://stackoverflow.com/a/559146/1313040
 * also based on http://stackoverflow.com/a/2922031/1313040
 */
public class SimpleTail implements Runnable {

    private final TailCallback callback;
    private long _updateInterval = 1000;
    private long _filePointer;
    private File _file;
    private static volatile boolean keepRunning = true;

    public static void main(String[] args) {
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            keepRunning = false;
            try {
                mainThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleTail.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));
        File log = new File("/var/log/system.log");
        SimpleTail tail = new SimpleTail(log, System.out::println);
        new Thread(tail).start();

    }

    public SimpleTail(File file, TailCallback callback) {
        this._file = file;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {

            while (keepRunning) {
                Thread.sleep(_updateInterval);
                long len = _file.length();

                if (len < _filePointer) {
                    // Log must have been jibbled or deleted.
                    this.appendMessage("Log file was reset. Restarting logging from start of file.");
                    _filePointer = len;
                } else if (len > _filePointer) {
                    // File must have had something added to it!
                    RandomAccessFile raf = new RandomAccessFile(_file, "r");
                    raf.seek(_filePointer);
                    String line = null;
                    while ((line = raf.readLine()) != null) {
                        this.appendLine(line);
                    }
                    _filePointer = raf.getFilePointer();
                    raf.close();
                }
            }
        } catch (Exception e) {
            this.appendMessage("Fatal error reading log file, log tailing has stopped.");
        }
        // dispose();
    }

    private void appendMessage(String line) {
        System.err.println(line.trim());
    }

    private void appendLine(String line) {
        callback.newLine(line);
    }
}
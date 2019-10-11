package nl.stokpop.tail;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class Tail {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Please provide file on command line.");
            System.exit(1);
        }
        Path file = Path.of(args[0]);

        new Tail().watch(file);

    }

    private void watch(Path file) throws IOException {
        WatchService service = FileSystems.getDefault().newWatchService();

        try {
            file.register(service, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey watchKey = service.take();
            if (watchKey != null) {
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                watchEvents.forEach(System.out::println);
            }
            else {
                System.out.println("No watchkey events.");
            }
        } catch (InterruptedException e) {
            System.out.println("Catched interrupted exception! " +  e.getMessage());
        }
    }
}

package application;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.TextArea;

public class WatchDirectoryService implements Runnable {

	private Path dir;
	private TextArea textArea;
	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	private final boolean recursive = true;
	private boolean isFile;
	private String watchFile;
	private boolean trace = false;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} 
			else 
			{
				if (!dir.equals(prev)) 
				{
					textArea.appendText("Updated : "+prev+" TO "+dir+"\n");
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	WatchDirectoryService(String path, TextArea textArea, boolean isFile) throws IOException {
		this.textArea = textArea;
		this.isFile = isFile;
		if(isFile){
			this.watchFile = path.substring(path.lastIndexOf("\\")+1);
			this.dir = Paths.get(path.substring(0,path.lastIndexOf("\\")));
		}else this.dir = Paths.get(path);
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();

		if (recursive) {
			textArea.clear();
			textArea.appendText("Scanning "+dir+" ...\n");
			registerAll(dir);
			textArea.appendText("Done.\n");
		} else {
			register(dir);
		}

		// enable trace after initial registration
		this.trace = true;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		for (;;) {

			// wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				textArea.appendText("WatchKey not recognized!! \n");
				continue;
			}

			for (WatchEvent<?> event: key.pollEvents())
			{
				WatchEvent.Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path fileName = ev.context();
				Path child = dir.resolve(fileName);

				// print out event
				System.out.format("%s: %s\n", event.kind().name(), child);
				if(isFile){
					if (child.endsWith(watchFile)&&kind.name().equals("ENTRY_MODIFY")) {
						textArea.appendText("Modified: " + child + "\n");
					}
					else if (child.endsWith(watchFile)&&kind.name().equals("ENTRY_DELETE")) {
						textArea.appendText("Filename has been changed or Deleted: " + child + "\n");
					}
				}
				else{

					switch (kind.name()) {
					case "ENTRY_CREATE":
						textArea.appendText("Created: " + child + "\n");
						break;
					case "ENTRY_DELETE":
						textArea.appendText("Deleted: " + child + "\n");
						break;
					/*case "ENTRY_MODIFY":
						textArea.appendText("Modified: " + child + "\n");
						break;*/
					default:
						System.out.println("Wrong event called.\n");
						break;
					}
				}

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (kind == ENTRY_CREATE) {
					try
					{
						if (Files.isDirectory(child, NOFOLLOW_LINKS))
						{
							registerAll(child);
						}
					} catch (IOException x)
					{
						// ignore to keep sample readable
					}
				}

			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
	@Override
	public void run() {
		processEvents();
	}
}
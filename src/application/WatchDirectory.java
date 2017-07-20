package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.scene.control.TextArea;

public class WatchDirectory implements Runnable {

	WatchService watchService;
	Path path;
	WatchKey key;
	TextArea textArea;

	public WatchDirectory(String path, TextArea textArea) {
		this.path = Paths.get(path);
		this.textArea = textArea;
	}

	public void Register(final Path root) throws InterruptedException {

		for (;;) {

			this.key = watchService.take();
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path fileName = ev.context();

				System.out.println(kind.name() + ": " + fileName);
				switch (kind.name()) {
				case "ENTRY_CREATE":
					textArea.appendText("Created: " + kind.name() + ": " + fileName + "\n");
					break;
				/*
				 * case "ENTRY_MODIFY": textArea.appendText("Modified: "
				 * +kind.name() + ": " + fileName+"\n"); break;
				 */
				case "ENTRY_DELETE":
					textArea.appendText("Deleted: " + kind.name() + ": " + fileName + "\n");
					break;
				default:
					textArea.appendText("Wrong event called.\n");
					break;
				}

				boolean valid = key.reset();

				if (!valid) {
					break;
				}
			}
		}
	}

	public void recursiveRegister(Path path) throws IOException, InterruptedException {

		for (;;) {

			this.key = watchService.take();
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path fileName = ev.context();

				System.out.println(kind.name() + ": " + fileName);

				String completeFileName = path.toString() + "\\" + fileName;
				switch (kind.name()) {
				case "ENTRY_CREATE":
					File fileObject = new File(completeFileName);
					textArea.appendText("Created: " + kind.name() + ": " + fileName + "\n");
					if (fileObject.isDirectory()) {
						Path newPath = Paths.get(completeFileName);
						this.watchService = FileSystems.getDefault().newWatchService();
						newPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
								StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
						this.recursiveRegister(newPath);
					}
					break;
					
				case "ENTRY_DELETE":
					textArea.appendText("Deleted: " + kind.name() + ": " + fileName + "\n");
					break;
				default:
					textArea.appendText("Wrong event called.\n");
					break;
				}

				boolean valid = key.reset();

				if (!valid) {
					break;
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
			this.path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			this.recursiveRegister(this.path);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}
}

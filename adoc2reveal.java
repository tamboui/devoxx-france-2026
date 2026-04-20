///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//JAVA_OPTIONS --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED
//JAVA_OPTIONS --enable-native-access=ALL-UNNAMED
//DEPS org.asciidoctor:asciidoctorj:3.0.1
//DEPS org.asciidoctor:asciidoctorj-diagram:3.2.1
//DEPS org.asciidoctor:asciidoctorj-diagram-batik:1.19
//DEPS org.asciidoctor:asciidoctorj-diagram-ditaamini:1.0.3
//DEPS org.asciidoctor:asciidoctorj-diagram-plantuml:1.2026.2
//DEPS org.asciidoctor:asciidoctorj-diagram-jsyntrax:1.38.2
//DEPS org.asciidoctor:asciidoctorj-pdf:2.3.23
//DEPS org.asciidoctor:asciidoctorj-revealjs:5.2.0
//DEPS org.asciidoctor:asciidoctorj-chart:1.0.0
//DEPS info.picocli:picocli:4.6.3
//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import dev.tamboui.style.Color;
import dev.tamboui.toolkit.Toolkit;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static dev.tamboui.toolkit.Toolkit.*;
import static picocli.CommandLine.*;

@Command(name = "adoc2reveal", version = "1.0")
public class adoc2reveal implements Callable<Integer> {

    @Parameters(index = "0", description = ".adoc file to convert/render.", defaultValue = "index.adoc")
    private File file;

    @Option(names = {"-w", "--watch"}, description = "Watch for changes and re-render if .adoc file changes")
    private boolean watch;

    @Option(names = {"--serve"}, description = "Serve the output directory via built-in HTTP server")
    private boolean serve;

    @Option(names = {"--tui"}, description = "Run an interactive TamboUI dashboard (implied by --watch or --serve)")
    private boolean tui;

    @Option(names = {"--open"}, description = "Open the generated presentation in the browser after render")
    private boolean open;

    @Option(names = {"--port"}, defaultValue = "8181", description = "Port for the HTTP server (used with --serve)")
    private int port;

    @Option(names = "--verbose")
    boolean verbose;

    @Option(names = {"--revealjsdir"}, defaultValue = "https://cdn.jsdelivr.net/npm/reveal.js@5.2.0", description = "revealjs directory or base url")
    String revealjsdir;

    @Option(names = {"-o", "--output-dir"}, defaultValue = "_presentation", description = "Directory where generated HTML and assets are written (kept separate from the sources)")
    String outputDirName;

    /** Directories mirrored from the source root into the output dir so relative asset URLs resolve. */
    private static final List<String> MIRRORED_ASSET_DIRS = List.of("images", "css");

    private Asciidoctor asciidoctor;
    private WatchService watchService;
    private HttpServer httpServer;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("adoc2reveal-scheduler", 0).factory());
    private final ConcurrentLinkedQueue<String> statusMessages = new ConcurrentLinkedQueue<>();
    private final LinkedHashSet<String> pendingChangedFiles = new LinkedHashSet<>();
    private final Object stateLock = new Object();

    private volatile ScheduledFuture<?> pendingRender;
    private volatile boolean interactive;
    private volatile boolean running;
    private volatile boolean showHelp = true;
    private volatile boolean watching;
    private volatile boolean serving;
    private volatile int renderCount;
    private volatile long lastRenderNanos;
    private volatile long totalRenderNanos;
    private volatile Instant lastRenderAt;
    private volatile String lastRenderReason = "startup";
    private volatile String lastRenderSummary = "Waiting for first render...";
    private volatile List<String> lastChangedFiles = List.of();
    private volatile Thread watchThread;

    public static void main(String... args) {
        System.exit(new CommandLine(new adoc2reveal()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        if (serve) {
            watch = true; // serving implies watching
        }
        interactive = tui || watch || serve;
        watching = watch;
        serving = serve;
        running = true;

        System.out.println("Booting up Asciidoctor...");
        asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.registerLogHandler(new LogHandler() {
            @Override
            public void log(LogRecord logRecord) {
                enqueueMessage("asciidoctor: " + logRecord.getMessage());
            }
        });
        asciidoctor.requireLibrary("asciidoctor-revealjs");
        asciidoctor.requireLibrary("asciidoctor-diagram");
        asciidoctor.requireLibrary("asciidoctor-chart");

        try {
            doRender("startup");
            if (open && !interactive) {
                openPresentation();
            }

            if (!interactive) {
                return 0;
            }

            if (serving) {
                startServer();
            }
            if (watching) {
                startWatcher();
            }

            new DashboardApp().run();
            return 0;
        } finally {
            shutdown();
        }
    }

    private Path rootDir() {
        return file.getAbsoluteFile().getParentFile().toPath();
    }

    private String outputName() {
        return file.getName().replaceFirst("\\.adoc$", ".html");
    }

    private Path outputDir() {
        Path candidate = Path.of(outputDirName);
        if (!candidate.isAbsolute()) {
            candidate = rootDir().resolve(candidate);
        }
        return candidate.toAbsolutePath().normalize();
    }

    private Path outputFile() {
        return outputDir().resolve(outputName());
    }

    private String currentUrl() {
        return serving ? "http://localhost:" + port + "/" + outputName() : outputFile().toUri().toString();
    }

    private void enqueueMessage(String msg) {
        statusMessages.add(timestamp() + " " + msg);
        while (statusMessages.size() > 12) {
            statusMessages.poll();
        }
    }

    private static String timestamp() {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(Instant.now().atZone(java.time.ZoneId.systemDefault()));
    }

    private void registerRecursive(final Path root) throws IOException {
        final Path outDir = outputDir();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                if (name.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (dir.toAbsolutePath().normalize().equals(outDir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void mirrorDir(Path from, Path to) throws IOException {
        if (!Files.isDirectory(from)) {
            return;
        }
        Files.createDirectories(to);
        try (var walk = Files.walk(from)) {
            walk.forEach(src -> {
                try {
                    Path rel = from.relativize(src);
                    Path dest = to.resolve(rel.toString());
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private synchronized void doRender(String reason) {
        long start = System.nanoTime();
        lastRenderReason = reason;
        List<String> changed = drainChangedFiles();
        lastChangedFiles = changed;

        Path outDir = outputDir();
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            enqueueMessage("failed to create output dir " + outDir + ": " + e.getMessage());
            return;
        }

        // Clear diagram cache so diagrams always re-render
        try {
            Path cacheDir = rootDir().resolve(".asciidoctor/diagram");
            if (Files.isDirectory(cacheDir)) {
                try (var entries = Files.list(cacheDir)) {
                    entries.filter(p -> p.toString().endsWith(".cache")).forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
                }
            }
        } catch (IOException ignored) {}

        // Mirror static asset dirs from source into the output dir so relative URLs resolve
        for (String name : MIRRORED_ASSET_DIRS) {
            Path from = rootDir().resolve(name);
            Path to = outDir.resolve(name);
            try {
                mirrorDir(from, to);
            } catch (IOException e) {
                enqueueMessage("asset sync failed for " + name + ": " + e.getMessage());
            }
        }

        if (interactive) {
            enqueueMessage("rendering because " + reason + (changed.isEmpty() ? "" : " | changed: " + String.join(", ", changed)));
        } else {
            System.out.printf("Start Rendering %s -> %s (%s)%n", file, outDir, reason);
            if (!changed.isEmpty()) {
                System.out.println("Changed: " + String.join(", ", changed));
            }
        }

        asciidoctor.convertFile(file,
                Options.builder()
                        .backend("revealjs")
                        .safe(SafeMode.UNSAFE)
                        .toDir(outDir.toFile())
                        .mkDirs(true)
                        .attributes(
                                Attributes.builder()
                                        .attribute("revealjsdir", revealjsdir)
                                        .attribute("imagesoutdir", outDir.resolve("images").toString())
                                        .build()
                        ).build()
        );

        long took = System.nanoTime() - start;
        renderCount++;
        lastRenderNanos = took;
        totalRenderNanos += took;
        lastRenderAt = Instant.now();
        lastRenderSummary = String.format("Rendered %s in %.2fs", outputName(), took / 1_000_000_000.0);

        if (interactive) {
            enqueueMessage(lastRenderSummary);
        } else {
            System.out.printf("Done Rendering %s in %.2fs%n", file, took / 1_000_000_000.0);
        }
    }

    private List<String> drainChangedFiles() {
        synchronized (stateLock) {
            if (pendingChangedFiles.isEmpty()) {
                return List.of();
            }
            List<String> changed = new ArrayList<>(pendingChangedFiles);
            pendingChangedFiles.clear();
            return changed;
        }
    }

    private void noteChanged(String path) {
        synchronized (stateLock) {
            pendingChangedFiles.add(path);
        }
        scheduleRender("file change");
    }

    private void scheduleRender(String reason) {
        if (pendingRender != null) {
            pendingRender.cancel(false);
        }
        pendingRender = scheduler.schedule(() -> doRender(reason), 180, TimeUnit.MILLISECONDS);
    }

    private void startServer() throws IOException {
        if (httpServer != null) {
            return;
        }
        var address = new InetSocketAddress(port);
        try {
            Path outDir = outputDir();
            Files.createDirectories(outDir);
            httpServer = SimpleFileServer.createFileServer(
                    address,
                    outDir,
                    SimpleFileServer.OutputLevel.NONE);
            httpServer.start();
            enqueueMessage("serving at " + currentUrl());
        } catch (Exception e) {
            httpServer = null;
            throw new IOException(String.format("failed to start server on %s:%d — %s", address.getHostString(), port, e.getMessage()), e);
        }
    }

    private void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
            enqueueMessage("server stopped");
        }
    }

    private void startWatcher() throws IOException {
        if (watchService != null) {
            return;
        }
        watchService = FileSystems.getDefault().newWatchService();
        registerRecursive(rootDir());
        enqueueMessage("watching " + rootDir());
        watchThread = Thread.ofVirtual().name("adoc2reveal-watch", 0).start(() -> {
            try {
                while (running) {
                    WatchKey key = watchService.take();
                    Path watchedDir = (Path) key.watchable();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        String name = changed.getFileName().toString();
                        if (isIgnoredGeneratedOutput(watchedDir, name)) {
                            if (verbose) {
                                enqueueMessage("ignored generated output " + name + " [" + event.kind().name() + "]");
                            }
                            continue;
                        }
                        if (name.endsWith(".adoc") || name.endsWith(".svg") || name.endsWith(".css") || name.endsWith(".html")) {
                            Path absolute = watchedDir.resolve(changed);
                            String full = rootDir().relativize(absolute).toString();
                            enqueueMessage("changed " + full + " [" + event.kind().name() + "]");
                            noteChanged(full);
                        } else if (verbose) {
                            enqueueMessage("ignored " + name + " [" + event.kind().name() + "]");
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                if (running) {
                    enqueueMessage("watch stopped: " + e.getMessage());
                }
            }
        });
    }

    private void stopWatcher() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            watchService = null;
        }
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
    }

    private void openEditor() {
        String editor = System.getenv("VISUAL");
        if (editor == null) {
            editor = System.getenv("EDITOR");
        }
        if (editor == null) {
            enqueueMessage("no editor found — set VISUAL or EDITOR env var");
            return;
        }
        try {
            Path dir = rootDir();
            Path target = dir.resolve(file.getName());
            new ProcessBuilder(editor, ".", target.toString())
                    .directory(dir.toFile())
                    .inheritIO()
                    .start();
            enqueueMessage("opened editor: " + editor + " . " + file.getName());
        } catch (Exception e) {
            enqueueMessage("editor launch failed: " + e.getMessage());
        }
    }

    private void openPresentation() {
        try {
            URI uri = URI.create(currentUrl());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
                enqueueMessage("opened " + uri);
                return;
            }
        } catch (Exception e) {
            enqueueMessage("browser open failed: " + e.getMessage());
        }

        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                new ProcessBuilder("open", currentUrl()).start();
            } else if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", currentUrl()).start();
            } else {
                new ProcessBuilder("xdg-open", currentUrl()).start();
            }
            enqueueMessage("opened " + currentUrl());
        } catch (Exception e) {
            enqueueMessage("could not open browser: " + e.getMessage());
        }
    }

    private void toggleWatch() {
        if (serving) {
            enqueueMessage("watch is locked while serve is on");
            return;
        }
        if (watchService == null) {
            try {
                watching = true;
                watch = true;
                startWatcher();
                requestRender("watch enabled");
            } catch (IOException e) {
                enqueueMessage("watch error: " + e.getMessage());
            }
        } else {
            watching = false;
            watch = false;
            stopWatcher();
        }
    }

    private void toggleServe() {
        if (httpServer == null) {
            serving = true;
            serve = true;
            if (watchService == null) {
                try {
                    watching = true;
                    watch = true;
                    startWatcher();
                } catch (IOException e) {
                    enqueueMessage("watch error: " + e.getMessage());
                }
            }
            try {
                startServer();
                openPresentation();
            } catch (IOException e) {
                serving = false;
                serve = false;
                enqueueMessage(e.getMessage());
            }
        } else {
            serving = false;
            serve = false;
            stopServer();
        }
    }

    private boolean isIgnoredGeneratedOutput(Path watchedDir, String name) {
        // Output lives in outputDir() which is skipped by the watcher, but a stale
        // pre-split .html sitting in the source root should still be ignored.
        return name.endsWith(".html");
    }

    private void requestRender(String reason) {
        scheduleRender(reason);
    }

    private void shutdown() {
        running = false;
        if (pendingRender != null) {
            pendingRender.cancel(false);
        }
        stopWatcher();
        stopServer();
        scheduler.shutdownNow();
    }

    private final class DashboardApp extends ToolkitApp {
        @Override
        protected TuiConfig configure() {
            return TuiConfig.builder()
                    .tickRate(Duration.ofMillis(250))
                    .build();
        }

        @Override
        protected Element render() {
            var root = panel("adoc2reveal",
                    column(
                            row(statusPanel(), shortcutsPanel()),
                            row(changesPanel(), eventsPanel()),
                            footerPanel()
                    ).spacing(1)
            )
                    .rounded()
                    .borderColor(Color.CYAN)
                    .padding(1)
                    .focusable()
                    .onKeyEvent(event -> {
                        if (event.isQuit() || event.isCharIgnoreCase('q')) {
                            quit();
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('r')) {
                            requestRender("manual refresh");
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('w')) {
                            toggleWatch();
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('s')) {
                            toggleServe();
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('o')) {
                            openPresentation();
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('h')) {
                            showHelp = !showHelp;
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('e')) {
                            openEditor();
                            return EventResult.HANDLED;
                        }
                        if (event.isCharIgnoreCase('c')) {
                            statusMessages.clear();
                            return EventResult.HANDLED;
                        }
                        return EventResult.UNHANDLED;
                    });

            return root;
        }
    }

    private Element statusPanel() {
        return panel("Status",
                column(
                        row(text("File").gray(), spacer(), text(file.getName()).cyan().bold()),
                        row(text("Output").gray(), spacer(), text(outputName()).green().bold()),
                        row(text("Out dir").gray(), spacer(), text(rootDir().relativize(outputDir()).toString()).green()),
                        row(text("URL").gray(), spacer(), text(currentUrl()).yellow()),
                        row(text("Mode").gray(), spacer(), text(modeText()).magenta()),
                        row(text("Render").gray(), spacer(), text("#" + renderCount + "  " + formatNanos(lastRenderNanos)).white()),
                        row(text("Total").gray(), spacer(), text(formatNanos(totalRenderNanos)).white())
                )
        ).rounded().borderColor(Color.CYAN).padding(1);
    }

    private Element shortcutsPanel() {
        return panel("Shortcuts",
                column(
                        text("r  render now").cyan(),
                        text("w  toggle watch").cyan(),
                        text("s  toggle serve").cyan(),
                        text("o  open browser").cyan(),
                        text("h  help on/off").cyan(),
                        text("e  open editor").cyan(),
                        text("c  clear log").cyan(),
                        text("q  quit").cyan()
                )
        ).rounded().borderColor(Color.MAGENTA).padding(1);
    }

    private Element changesPanel() {
        List<String> changes = lastChangedFiles;
        List<Element> lines = new ArrayList<>();
        if (changes.isEmpty()) {
            lines.add(text("No file changes yet.").gray());
        } else {
            for (String change : changes) {
                lines.add(text("• " + change).yellow());
            }
        }
        return panel("Last changed files", column(lines.toArray(Element[]::new)))
                .rounded().borderColor(Color.GREEN).padding(1);
    }

    private Element eventsPanel() {
        var snapshot = new ArrayList<>(statusMessages);
        List<Element> lines = new ArrayList<>();
        if (snapshot.isEmpty()) {
            lines.add(text("Waiting for events...").gray());
        } else {
            int start = Math.max(0, snapshot.size() - 8);
            for (int i = start; i < snapshot.size(); i++) {
                lines.add(text(snapshot.get(i)).white());
            }
        }
        return panel("Recent events", column(lines.toArray(Element[]::new)))
                .rounded().borderColor(Color.YELLOW).padding(1);
    }

    private Element footerPanel() {
        var body = column(
                text(lastRenderSummary).green().bold(),
                text("Reason: " + lastRenderReason).gray(),
                text(showHelp ? "Press h to hide this help block." : "Press h to show hotkeys.").gray()
        );
        return panel(body).rounded().borderColor(Color.BLUE).padding(1);
    }

    private String modeText() {
        return (watch ? "[ON]" : "[OFF]") + " watch   " + (serve ? "[ON]" : "[OFF]") + " serve   " + (interactive ? "[ON]" : "[OFF]") + " tui";
    }

    private static String formatNanos(long nanos) {
        if (nanos <= 0) {
            return "—";
        }
        if (nanos < 1_000_000) {
            return String.format("%.0fµs", nanos / 1_000.0);
        }
        if (nanos < 1_000_000_000) {
            return String.format("%.1fms", nanos / 1_000_000.0);
        }
        return String.format("%.2fs", nanos / 1_000_000_000.0);
    }
}

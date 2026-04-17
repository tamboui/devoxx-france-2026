/*
 * Compilable snippet used in slides/higher-level.adoc.
 */
package snippets.tui;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import dev.tamboui.tui.EventHandler;
import dev.tamboui.tui.Renderer;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.error.RenderErrorHandlers;

public final class TuiRunnerConfigDemo {

    private TuiRunnerConfigDemo() {}

    private void pollBackend() {
        // pretend we're polling some external service
    }

    private boolean onEvent(dev.tamboui.tui.event.Event event, TuiRunner runner) {
        return false;
    }

    private void render(dev.tamboui.terminal.Frame frame) {
        // render something
    }

    void configExample() throws Exception {
        // tag::config[]
        var config = TuiConfig.builder()
            .mouseCapture(true)
            .tickRate(Duration.ofMillis(16))        // ~60 fps
            .resizeGracePeriod(Duration.ofMillis(250))
            .bindings(BindingSets.vim())            // hjkl out of the box
            .errorHandler(RenderErrorHandlers.displayAndQuit())
            .build();

        try (var tui = TuiRunner.create(config)) {
            tui.scheduler().scheduleAtFixedRate(
                this::pollBackend, 1, 1, TimeUnit.SECONDS);
            tui.run(this::onEvent, this::render);
        }
        // end::config[]
    }
}

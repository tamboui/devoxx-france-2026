/*
 * Compilable snippet used in slides/tfx.adoc.
 */
package snippets.tfx;

import dev.tamboui.style.Color;
import dev.tamboui.terminal.Frame;
import dev.tamboui.tfx.Fx;
import dev.tamboui.tfx.Interpolation;
import dev.tamboui.tfx.tui.TfxIntegration;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.Event;

public final class TfxTuiDemo {

    private TfxTuiDemo() {}

    private static boolean handleEvent(Event event, TuiRunner runner) {
        return false;
    }

    private static void renderUI(Frame frame) {
        // render widgets
    }

    public static void main(String[] args) throws Exception {
        TuiConfig config = TuiConfig.builder().build();

        // tag::integration[]
        var tfx = new TfxIntegration();
        tfx.addEffect(Fx.fadeFromFg(Color.BLACK, 1000, Interpolation.QuadOut));

        try (TuiRunner tui = TuiRunner.create(config)) {
            tui.run(
                tfx.wrapHandler((event, runner) -> handleEvent(event, runner)),
                tfx.wrapRenderer(frame -> renderUI(frame))
            );
        }
        // end::integration[]
    }
}

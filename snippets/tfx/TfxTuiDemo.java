///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS dev.tamboui:tamboui-core:LATEST
//DEPS dev.tamboui:tamboui-tui:LATEST
//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-widgets:LATEST
//DEPS dev.tamboui:tamboui-annotations:LATEST
//DEPS dev.tamboui:tamboui-css:LATEST
//DEPS dev.tamboui:tamboui-tfx:LATEST
//DEPS dev.tamboui:tamboui-tfx-tui:LATEST
//DEPS dev.tamboui:tamboui-tfx-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST
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
        var config = TuiConfig.builder().build();

        // tag::integration[]
        var tfx = new TfxIntegration();
        tfx.addEffect(Fx.fadeFromFg(Color.BLACK, 1000, Interpolation.QuadOut));

        try (var tui = TuiRunner.create(config)) {
            tui.run(
                tfx.wrapHandler((event, runner) -> handleEvent(event, runner)),
                tfx.wrapRenderer(frame -> renderUI(frame))
            );
        }
        // end::integration[]
    }
}

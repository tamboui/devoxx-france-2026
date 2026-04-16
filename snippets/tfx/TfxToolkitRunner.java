/*
 * Compilable snippet used in slides/tfx.adoc.
 */
package snippets.tfx;

import dev.tamboui.tfx.toolkit.ToolkitEffects;
import dev.tamboui.toolkit.app.ToolkitRunner;
import dev.tamboui.tui.TuiConfig;

import static dev.tamboui.toolkit.Toolkit.*;

public final class TfxToolkitRunner {

    private TfxToolkitRunner() {}

    public static void run() throws Exception {
        var effects = new ToolkitEffects();
        var config = TuiConfig.builder().build();

        // tag::runner[]
        try (var runner = ToolkitRunner.builder()
                .config(config)
                .postRenderProcessor(effects.asPostRenderProcessor())
                .build()) {
            runner.run(() ->
                panel("Header", text("Welcome!")).id("header").rounded()
            );
        }
        // end::runner[]
    }
}

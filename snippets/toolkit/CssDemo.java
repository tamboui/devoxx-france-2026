/*
 * Compilable snippet used in slides/higher-level.adoc.
 */
package snippets.toolkit;

import java.io.IOException;

import dev.tamboui.css.engine.StyleEngine;

public final class CssDemo {

    private CssDemo() {}

    public static void example() throws IOException {
        var engine = StyleEngine.create();

        // tag::css[]
        engine.loadStylesheet("dark", "/themes/dark.tcss");
        engine.loadStylesheet("light", "/themes/light.tcss");
        engine.setActiveStylesheet("dark");   // at runtime, live!
        // end::css[]
    }
}

/*
 * Compilable snippet used in slides/higher-level.adoc.
 */
package snippets.tui;

import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.error.RenderErrorHandlers;

public final class ErrorHandlerDemo {

    private ErrorHandlerDemo() {}

    public static TuiConfig example() {
        // tag::error[]
        return TuiConfig.builder()
            .errorHandler(RenderErrorHandlers.displayAndQuit())
            .build();
        // end::error[]
    }
}

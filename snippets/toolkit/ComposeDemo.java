/*
 * Compilable snippet used in slides/higher-level.adoc.
 */
package snippets.toolkit;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import static dev.tamboui.toolkit.Toolkit.*;

public class ComposeDemo extends ToolkitApp {

    // tag::render[]
    @Override
    protected Element render() {
        return column(
            header(),
            row(sidebar(), mainContent()),
            statusBar()
        ).fill();
    }

    private Element sidebar() {
        return panel(
            list("Inbox", "Sent")
                .highlightSymbol("> ")
                .focusable().id("nav")
        ).title("Folders").rounded().length(30);
    }
    // end::render[]

    private Element header() {
        return panel(text("Header"));
    }

    private Element mainContent() {
        return panel(text("Content")).fill();
    }

    private Element statusBar() {
        return panel(text("Status")).length(1);
    }
}

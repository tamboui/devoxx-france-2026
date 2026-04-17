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
 * Compilable snippet used in slides/lower-level.adoc.
 */
package snippets.lower;

import dev.tamboui.terminal.Backend;
import dev.tamboui.terminal.BackendFactory;

public final class BackendDemo {

    private BackendDemo() {}

    public static void main(String[] args) throws Exception {
        // tag::backend[]
        // All three backends implement the same Backend interface
        try (var backend = BackendFactory.create()) {  // picks best available
            backend.enterAlternateScreen();
            backend.enableRawMode();
            backend.enableMouseCapture();
            // ...
            backend.leaveAlternateScreen();
        }
        // end::backend[]
    }
}

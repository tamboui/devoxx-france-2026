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
        try (Backend backend = BackendFactory.create()) {  // picks best available
            backend.enterAlternateScreen();
            backend.enableRawMode();
            backend.enableMouseCapture();
            // ...
            backend.leaveAlternateScreen();
        }
        // end::backend[]
    }
}

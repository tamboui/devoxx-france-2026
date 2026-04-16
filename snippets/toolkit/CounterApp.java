/*
 * Compilable snippet used in slides/higher-level.adoc.
 */
package snippets.toolkit;

import dev.tamboui.annotations.bindings.OnAction;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.tui.bindings.Actions;
import dev.tamboui.tui.event.Event;

import static dev.tamboui.toolkit.Toolkit.*;

// tag::counter[]
public class CounterApp extends ToolkitApp {
    private int count = 0;

    @Override
    protected Element render() {
        return column(
            panel(text("Count: " + count).bold().cyan())
                .title("Counter").rounded().fill(),
            panel(text(" ↑/↓: change  q: quit ").dim())
                .rounded().length(3)
        );
    }

    @OnAction(Actions.MOVE_UP)   void inc(Event e) { count++; }
    @OnAction(Actions.MOVE_DOWN) void dec(Event e) { count--; }

    public static void main(String[] args) throws Exception {
        new CounterApp().run();
    }
}
// end::counter[]

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

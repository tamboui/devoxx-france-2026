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
package snippets.tui;

import dev.tamboui.text.Text;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.Event;
import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.widgets.paragraph.Paragraph;

public final class CounterRunner {

    private CounterRunner() {}

    public static void main(String[] args) throws Exception {
        // tag::counter[]
        int[] count = {0};
        try (var tui = TuiRunner.create()) {
            tui.run(
                (event, runner) -> switch (event) {
                    case KeyEvent k when k.isQuit() -> { runner.quit(); yield false; }
                    case KeyEvent k when k.isUp()   -> { count[0]++; yield true; }
                    case KeyEvent k when k.isDown() -> { count[0]--; yield true; }
                    default -> false;
                },
                frame -> frame.renderWidget(
                    Paragraph.builder()
                        .text(Text.from("Count: " + count[0]))
                        .centered()
                        .build(),
                    frame.area())
            );
        }
        // end::counter[]
    }
}

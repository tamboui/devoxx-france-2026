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
        try (TuiRunner tui = TuiRunner.create()) {
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

/*
 * Compilable snippet used in slides/lower-level.adoc.
 */
package snippets.lower;

import dev.tamboui.text.Text;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.paragraph.Paragraph;

public final class ImmediateDemo {

    private ImmediateDemo() {}

    public static void main(String[] args) throws Exception {
        // tag::immediate[]
        try (TuiRunner tui = TuiRunner.create()) {
            tui.run(
                (event, runner) -> false,
                frame -> {
                    Paragraph para = Paragraph.builder()
                        .text(Text.from("Hello, TamboUI!"))
                        .block(Block.bordered())
                        .build();
                    frame.renderWidget(para, frame.area());
                }
            );
        }
        // end::immediate[]
    }
}

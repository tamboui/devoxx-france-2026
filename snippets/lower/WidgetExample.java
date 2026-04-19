/*
 * Compilable snippet used in slides/lower-level.adoc.
 */
package snippets.lower;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Alignment;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Overflow;
import dev.tamboui.text.Text;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;

public final class WidgetExample {

    private WidgetExample() {}

    public static void demo(Rect area, Buffer buffer) {
        // tag::widget-example[]
        Paragraph paragraph = Paragraph.builder()
            .text(Text.from("This is a paragraph of text that can wrap across multiple lines."))
            .overflow(Overflow.WRAP_WORD)
            .alignment(Alignment.LEFT)
            .block(Block.builder()
                .title("Description")
                .borders(Borders.ALL)
                .build())
            .build();

        paragraph.render(area, buffer);
        // end::widget-example[]
    }
}

/*
 * Compilable snippet used in slides/lower-level.adoc.
 */
package snippets.lower;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.buffer.Cell;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;

public final class BufferDemo {

    private BufferDemo() {}

    public static void demo() {
        // tag::buffer[]
        // A Buffer is a 2D grid of Cells
        Buffer buffer = Buffer.empty(new Rect(0, 0, 80, 24));

        // A Cell has a symbol and a Style
        buffer.set(0, 0, new Cell("H", Style.EMPTY.fg(Color.RED).bold()));

        // Rect: x, y, width, height, used everywhere for layout
        Rect area = new Rect(10, 5, 60, 14);
        // end::buffer[]

        assert area.width() == 60;
    }
}

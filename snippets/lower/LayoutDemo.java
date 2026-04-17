/*
 * Compilable snippet used in slides/lower-level.adoc.
 */
package snippets.lower;

import java.util.List;

import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Layout;
import dev.tamboui.layout.Rect;
import dev.tamboui.terminal.Frame;

public final class LayoutDemo {

    private LayoutDemo() {}

    public static void split(Frame frame) {
        // tag::layout[]
        var layout = Layout.horizontal().constraints(
            Constraint.percentage(30),   // sidebar
            Constraint.min(20),          // main content, at least 20 cols
            Constraint.length(10)        // fixed-width panel
        );

        var rects = layout.split(frame.area());
        // end::layout[]

        assert rects.size() == 3;
    }
}

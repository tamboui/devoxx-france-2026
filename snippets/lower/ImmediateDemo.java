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

import dev.tamboui.text.Text;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.paragraph.Paragraph;

public final class ImmediateDemo {

    private ImmediateDemo() {}

    public static void main(String[] args) throws Exception {
        // tag::immediate[]
        try (var tui = TuiRunner.create()) {
            tui.run(
                (event, runner) -> false,
                frame -> {
                    var para = Paragraph.builder()
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

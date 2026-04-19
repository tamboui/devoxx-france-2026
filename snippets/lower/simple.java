
import java.util.Map;

import dev.tamboui.layout.Constraint;
import dev.tamboui.text.Emoji;
import dev.tamboui.text.MarkupParser;
import dev.tamboui.toolkit.app.InlineToolkitRunner;

import static dev.tamboui.toolkit.Toolkit.*;


/**
 * Main entry point
 * @throws Exception in case something goes wrong
 */
void main() throws Exception {
    try (var runner = InlineToolkitRunner.create()) {
        runner.println(markupText("This is [red]red[/red] and [bold]bold[/bold]."));
        runner.println(table().header("Name", "Age", "City")
                        .row("Alice", "30", "NYC")
                        .row("Bob", "25", "LA")
                        .row("Charlie", "35", "Chicago")
                        .widths(Constraint.fill(), Constraint.fill(), Constraint.fill()));
        runner.println(text("This is a paragraph"));
    }
}

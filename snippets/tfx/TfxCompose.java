/*
 * Compilable snippet used in slides/tfx.adoc.
 */
package snippets.tfx;

import dev.tamboui.style.Color;
import dev.tamboui.tfx.Effect;
import dev.tamboui.tfx.Fx;
import dev.tamboui.tfx.Interpolation;
import dev.tamboui.tfx.Motion;

public final class TfxCompose {

    private TfxCompose() {}

    public static void example() {
        // tag::compose[]
        // Chain: fade in, then dissolve out
        var intro = Fx.sequence(
            Fx.fadeFromFg(Color.BLACK, 500, Interpolation.QuadOut),
            Fx.dissolve(800, Interpolation.Linear)
        );

        // Or run both at once
        var flash = Fx.parallel(
            Fx.fadeToFg(Color.CYAN, 2000, Interpolation.SineInOut),
            Fx.sweepIn(Motion.LEFT_TO_RIGHT, 10, 0, Color.BLUE,
                       2000, Interpolation.QuadOut)
        );
        // end::compose[]

        // Keep the compiler happy about unused locals.
        assert intro != null && flash != null;
    }
}

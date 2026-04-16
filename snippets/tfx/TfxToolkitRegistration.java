/*
 * Compilable snippet used in slides/tfx.adoc.
 */
package snippets.tfx;

import dev.tamboui.style.Color;
import dev.tamboui.tfx.Fx;
import dev.tamboui.tfx.Interpolation;
import dev.tamboui.tfx.toolkit.ToolkitEffects;

public final class TfxToolkitRegistration {

    private TfxToolkitRegistration() {}

    public static void example() {
        // tag::register[]
        var effects = new ToolkitEffects();

        // By element id
        effects.addEffect("header",
            Fx.fadeFromFg(Color.BLACK, 800, Interpolation.QuadOut));

        // By CSS class — every matching element gets a copy
        effects.addEffectBySelector(".highlight",
            Fx.fadeTo(Color.WHITE, Color.MAGENTA, 1000,
                      Interpolation.SineInOut).pingPong());

        // Pseudo-classes work too
        effects.addEffectBySelector("#nav:focus .item",
            Fx.fadeToFg(Color.CYAN, 400, Interpolation.SineInOut));
        // end::register[]
    }
}

/*
 * Compilable snippet used in slides/tfx.adoc.
 */
package snippets.tfx;

import dev.tamboui.style.Color;
import dev.tamboui.tfx.CellFilter;
import dev.tamboui.tfx.Effect;
import dev.tamboui.tfx.EffectManager;
import dev.tamboui.tfx.Fx;
import dev.tamboui.tfx.Interpolation;
import dev.tamboui.tfx.pattern.RadialPattern;

public final class TfxBasic {

    private TfxBasic() {}

    public static void example() {
        var effectManager = new EffectManager();

        // tag::basic[]
        var fade = Fx.fadeFromFg(Color.CYAN, 2000, Interpolation.SineInOut)
            .withFilter(CellFilter.text())
            .withPattern(RadialPattern.center().withTransitionWidth(10f));

        effectManager.addEffect(fade);
        // end::basic[]
    }
}

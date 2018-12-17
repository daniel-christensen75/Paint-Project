package pain.t;

import java.util.EventListener;
import javafx.scene.paint.Color;

/**
 * A listener to color picking events in a PaintCanvas. Obtains the color from
 * the canvas when using the eye dropper tool.
 *
 */
public interface EyeDropper extends EventListener {

    /**
     * Called when pressing on the canvas with eye dropper tool. 
     * The color that is selected is the parameter and becomes 
     * line color for other tools to use.
     *
     * @param c Color the picked color on the canvas
     */
    void colorPicked(Color c);
}

package pain.t;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Selects a part of the image in a rectangle. The image can then be moved
 * about on the canvas.
 */
public class SelectObject extends MyRectangle {

    //mouse X and Y positions
    private double xPos, yPos;

    //true if the user is dragging the selection
    private boolean grabbed = false;

    //updated when the selection moves
    private boolean moved = false;

    //the content of the selection
    private WritableImage capturedCanvas;

    /**
     * Creates a rectangle for the user to select parts of the canvas with. 
     */
    SelectObject() {
        super();

        //default graphics for the rectangle
        this.setSmooth(false);
        this.setFill(Color.TRANSPARENT);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(1);
        this.getStrokeDashArray().addAll(2d, 2d);

        /**
         * Grabs the selection box.
         */
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xPos = event.getX() - getX();
                yPos = event.getY() - getY();
                grabbed = true;
            }
        });

        /**
         * Rectangle begins to capture canvas as it is being dragged across canvas
         */
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                moved = true;
                //moves the selection with the mouse
                setX(event.getX() - xPos);
                setY(event.getY() - yPos);

                if (capturedCanvas != null) {
                    setFill(new ImagePattern(capturedCanvas, getX(), getY(), getWidth(), getHeight(), false));
                }
            }
        });

        /**
         * On mouse released: end selection dragging.
         */
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //user releases selection
                grabbed = false;
            }
        });
    }

    /**
     * resets the selection rectangle to default values
     * allows for the next selection
     *
     * @param x new x position
     * @param y new y position
     */
    public void reset(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setWidth(0);
        this.setHeight(0);
        this.empty();
        this.setFill(Color.TRANSPARENT);
        moved = false;
    }

    /**
     * Sees if the user is holding the selection
     *
     * @return true if holding the selection, otherwise false 
     */
    public boolean getIsHolding() {
        return grabbed;
    }

    /**
     * Sees if selection has moved
     *
     * @return true if selection has moved, otherwise false
     */
    public boolean hasMoved() {
        return moved;
    }

    /**
     * Takes a snapshot of whatever is in the selection rectangle
     */
    public void select() {

        //coordinates of the selection, without the selection rectangle's borders
        int x = (int) this.getAbsX() + 1;
        int y = (int) this.getAbsY() + 1;
        int w = (int) this.getWidth() - 2;
        int h = (int) this.getHeight() - 2;

        if (w > 0 && h > 0) {
            Node parent = this.getParent();

            //take a snapshot of whatever is within the selection rectangle
            this.capturedCanvas = new WritableImage(parent.snapshot(new SnapshotParameters(), null).getPixelReader(), x, y, w, h);
            this.setFill(new ImagePattern(capturedCanvas, getX(), getY(), getWidth(), getHeight(), false));
        }
    }

    /**
     * Returns whatever was captured in the rectangle from the canvas
     * 
     * @return Shape. The rectangle that contains a part of the canvas or null
     * if nothing has been captured.
     */
    public Rectangle getContent() {

        if (this.capturedCanvas != null) {
            int x = (int) this.getAbsX() + 1;
            int y = (int) this.getAbsY() + 1;

            Rectangle r = new Rectangle(x, y, this.capturedCanvas.getWidth(), this.capturedCanvas.getHeight());
            r.setFill(new ImagePattern(this.capturedCanvas, x, y, this.capturedCanvas.getWidth(), this.capturedCanvas.getHeight(), false));
            r.setStrokeWidth(0);
            return r;
        } else {
            //if no content has been captured, return null
            return null;
        }
    }

    /**
     * Empty the selection rectangle.
     */
    public void empty() {
        this.capturedCanvas = null;
    }
}

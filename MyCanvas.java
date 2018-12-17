package pain.t;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.imageio.ImageIO;

/**
 * 
 * A canvas for displaying images and drawing on. It allows for shapes and Text
 * to be drawn on it. The shapes can be seen changing while dragging the mouse.
 * Colors can also be detected off of it.
 *
 * @author Daniel Christensen
 */
public class MyCanvas extends Pane {

    //default dimension values
    private static final double defaultWidth = 800;
    private static final double defaultHeight = 800;

    //color to apply for drawing
    private Color LineColor;

    //color for the fill of shapes
    private Color FillColor;

    //stroke width
    private double width;

    //font size for text
    private double fontSize;

    //the text to display in text mode
    private String text = MyToolbar.getText();

    //keeps track of changes on canvas
    private boolean edit = false;

    //Shapes for drawing
    private Pen shapeType;

    //Mode for the pen
    private Mode mode;

    //a stack to store the undone actions
    private Stack<Shape> undo = new Stack<Shape>();

    //selection graphics
    private SelectObject selection = new SelectObject();

    //background
    private Rectangle background;

    //listeners for color picking events
    private List<EyeDropper> colorPickedListeners = new ArrayList<EyeDropper>();

    /**
     * Allows for these shape types to be drawn
     */
    public enum Pen {
        FREE_DRAWING, LINE, RECT, SQUARE, CIRCLE, ELLIPSE, TEXT
    }

    /**
     * Allows for shapes to be drawn, erased, portions of the canvas to be selected,
     * or colors to be grabbed from the eye dropper tool.
     */
    public enum Mode {
        DRAW, ERASE, SELECT, EYEDROPPER
    }

    /**
     * Creates a new custom canvas with the default width and height.
     */
    public MyCanvas() {
        this(MyCanvas.defaultWidth, MyCanvas.defaultHeight);
    }

    /**
     * Creates a new canvas with the specified width or height
     * 
     * @param w canvas width
     * @param h canvas height
     */
    public MyCanvas(double w, double h) {
        super();

        //default settings
        this.LineColor = MyToolbar.getLineColor();
        this.FillColor = MyToolbar.getFillColor();
        this.width = 1;
        this.fontSize = 40;

        //reset the canvas with a specified width and height. Background will be white.
        this.reset(w, h);

        /**
         * Event handler for the mouse being pressed on the canvas.
         * Changes outcomes depending on what button the user has selected.
         * Can Draw shapes, make selections from the canvas, or select colors from the canvas
         */
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                //check the current mode
                if (mode == Mode.SELECT) {

                    //check if selecting or clicking on existing selection
                    if (selection.getIsHolding()) {

                        //if starting to move current selection, place empty rectangle instead of selection before selection moves
                        if (!selection.hasMoved()) {
                            Rectangle r = new Rectangle(selection.getAbsX(), selection.getAbsY(), selection.getWidth(), selection.getHeight());
                            r.setFill(Color.WHITE);
                            getChildren().add(getChildren().indexOf(selection), r);
                        }
                    } else {

                        //if in selection mode, print the content of previous selection
                        if (selection.getContent() != null) {
                            getChildren().remove(selection);
                            getChildren().add(selection.getContent());
                        }

                        //start a new selection where the user clicked
                        selection.reset(event.getX(), event.getY());
                    }
                } else if (mode == Mode.EYEDROPPER) {
                    //in picking mode, 
                    //get the pixel's color at the mouse position
                    Color color = snapshot(new SnapshotParameters(), null).getPixelReader().getColor((int) event.getX(), (int) event.getY());

                    //update drawing color
                    LineColor = color;

                    //call the event listeners for color picking events
                    for (EyeDropper listener : colorPickedListeners) {
                        listener.colorPicked(color);
                    }
                } else {
                    //if drawing or erasing, start a new drawing as a new shape we add the pane

                    //check if in text mode and text has not been set yet
                    if (mode == Mode.DRAW && shapeType == Pen.TEXT && text == "") {
                        //Grab text from the Input Box
                        text = MyToolbar.getText();
                        
                    } else {
                        //otherwise, start a new shape according to the current settings
                        Shape newShape;

                        //if in erase mode, use free drawing, otherwise use custom pen
                        Pen p = mode == Mode.ERASE ? Pen.FREE_DRAWING : shapeType;

                        switch (p) {
                            case LINE:
                                newShape = new Line(event.getX(), event.getY(), event.getX(), event.getY());
                                break;
                            case RECT:
                            case SQUARE:
                                newShape = new MyRectangle(event.getX(), event.getY(), 0, 0);
                                break;
                            case CIRCLE:
                                newShape = new Circle(event.getX(), event.getY(), 0);
                                break;
                            case ELLIPSE:
                                newShape = new Ellipse(event.getX(), event.getY(), 0, 0);
                                break;
                            case TEXT:
                                newShape = new Text(event.getX(), event.getY(), MyToolbar.getText());
                                ((Text) newShape).setFont(new Font(fontSize));
                                break;
                            default:
                                newShape = new Path();
                                newShape.setStrokeLineCap(StrokeLineCap.ROUND);
                                newShape.setStrokeLineJoin(StrokeLineJoin.ROUND);
                                ((Path) newShape).getElements().add(new MoveTo(event.getX(), event.getY()));
                                break;
                        }

                        if (mode == Mode.ERASE) {
                            //in erase mode, pen should be a large, white free drawing
                            newShape.setStroke(Color.WHITE);
                            newShape.setStrokeWidth(width * 5);
                        } else {
                            //sets the shapes Line and Fill Colors
                            newShape.setStroke(LineColor);
                            newShape.setFill(FillColor);

                            if (shapeType == Pen.TEXT) {
                                //if adding text to the image, should use a with of 1
                                newShape.setStrokeWidth(1);
                                //Colors for the outline and inside of text
                                newShape.setStroke(LineColor);
                                newShape.setFill(FillColor);
                            } else if (shapeType == Pen.FREE_DRAWING) {
                                newShape.setStrokeWidth(MyToolbar.getLineWidth());
                                newShape.setStroke(LineColor);
                                //Sets the fill color back to transparent
                                newShape.setFill(Color.TRANSPARENT);
                            }else {
                                //for any other type of drawing, use custom width
                                newShape.setStrokeWidth(width);
                                //Sets the fill color for the shape
                                newShape.setFill(FillColor);
                            }
                        }

                        //finalize and add the new shape to the parent
                        getChildren().add(newShape);
                    }
                }

                //In any case, the canvas has changed and smart save should be triggered when necessary
                edit = true;
                undo.clear();
            }
        });

        /**
         * Mouse dragged event listener: drawing preview. In selection or
         * drawing modes, update the shape being drawn as to match the new mouse
         * position. Used to preview the shape or selection until the user
         * releases the mouse.
         */
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                //check the current mode
                if (mode == Mode.SELECT) {

                    //if the user is not moving the selection, update the selection rectangle
                    if (!selection.getIsHolding()) {

                        selection.setRelativeWidth(event.getX() - selection.getX());
                        selection.setRelativeHeight(event.getY() - selection.getY());

                        //if the selection rectangle is not visible yet, add it to the pane's children
                        if (!getChildren().contains(selection)) {
                            getChildren().add(selection);
                        }
                    }
                } else if (mode != Mode.EYEDROPPER) {
                    //if in drawing or erasing mode, update the shape being drawn so as to preview it

                    //retrieve last shape from the canvas' children, it is the one being drawn
                    Shape shape = (Shape) getChildren().get(getChildren().size() - 1);

                    //get mouse position
                    double mouseX = event.getX();
                    double mouseY = event.getY();

                    //if in erase mode, use free drawing, otherwise use custom pen
                    Pen p = mode == Mode.ERASE ? Pen.FREE_DRAWING : shapeType;

                    //otherwise, use the custom pen
                    switch (p) {
                        case LINE:
                            Line l = (Line) shape;
                            l.setEndX(mouseX);
                            l.setEndY(mouseY);
                            break;
                        case RECT:
                            MyRectangle r = (MyRectangle) shape;
                            r.setRelativeWidth(mouseX - r.getX());
                            r.setRelativeHeight(mouseY - r.getY());
                            break;
                        case SQUARE:
                            MyRectangle sq = (MyRectangle) shape;
                            sq.setRelativeHeight(mouseY - sq.getY());

                            //for the square, set the width to be equals to the height qnd orient the shqpe to be on the pointer's side
                            if (mouseX >= sq.getX() && mouseY >= sq.getY() || mouseX < sq.getX() && mouseY < sq.getY()) {
                                sq.setRelativeWidth(mouseY - sq.getY());
                            } else {
                                sq.setRelativeWidth(sq.getY() - mouseY);
                            }
                            break;
                        case CIRCLE:
                            Circle c = (Circle) shape;
                            c.setRadius(sqrt(pow(mouseX - c.getCenterX(), 2) + pow(mouseY - c.getCenterY(), 2)));
                            resize(800, 800);
                            break;
                        case ELLIPSE:
                            Ellipse e = (Ellipse) shape;
                            e.setRadiusX(sqrt(pow(mouseX - e.getCenterX(), 2)));
                            e.setRadiusY(sqrt(pow(mouseY - e.getCenterY(), 2)));
                            resize(800, 800);
                        case TEXT:
                            Text t = (Text) shape;
                            t.setX(mouseX);
                            t.setY(mouseY);
                            break;
                        default:
                            //free drawing or eraser
                            Path path = (Path) shape;
                            path.getElements().add(new LineTo(mouseX, mouseY));
                            break;
                    }
                }
            }
        }
        );

        /**
         * Captures image in box and allows user to grab the image
         */
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                //selects the image in the box upon release of mouse after dragging the box over canvas
                if (mode == Mode.SELECT && selection.getContent() == null) {
                    selection.select();
                }
            }
        });
    }

    /**
     * Erases everything on canvas and sets the canvas to default values. 
     * Also clears the stack for undo and redo.
     */
    public void reset() {
        this.reset(MyCanvas.defaultWidth, MyCanvas.defaultHeight, Color.WHITE);
    }

    /**
     * Erases everything on canvas. The canvas will be created with new width and height
     * values. Also clears the stack for undo and redo.
     *
     * @param w canvas' new width.
     * @param h canvas' new height.
     */
    public void reset(double w, double h) {
        this.reset(w, h, Color.WHITE);
    }

    /**
     * Erases everything on canvas. The canvas will be created with new width and height
     * values. A new background will be painted. This is mostly used for opening images. 
     * Also clears the stack for undo and redo.
     *
     * @param w canvas' new width.
     * @param h canvas' new height.
     * @param background the paint to use as a background to the new canvas.
     */
    public void reset(double w, double h, Paint background) {
        this.getChildren().clear();
        this.selection.empty();
        this.background = new Rectangle(0, 0, w, h);
        this.background.setFill(background);
        this.getChildren().add(this.background);
        this.undo = new Stack();
        this.edit = false;
    }

    /**
     * Sets a new line color. 
     *
     * @param color new line color.
     */
    public void setLineColor(Color color) {
        this.LineColor = color;
    }

    /**
     * Gets the line color that is currently in use.
     *
     * @return Color the currently selected color.
     */
    public Color getLineColor() {
        return this.LineColor;
    }

    /**
     * Sets a new fill color. 
     *
     * @param fillColor new fill color.
     */
    public void SetFillColor(Color fillColor) {
        this.FillColor = fillColor;
    }

    /**
     * Gets the fill color that is currently in use.
     *
     * @return Color the currently selected fill color.
     */
    public Color getFillColor() {
        return this.FillColor;
    }

    /**
     * Sets a new line width size for the line of shapes.
     * 
     * @param width new line width.
     */
    public void setLineWidth(double width) {
        this.width = width;
    }

    /**
     * Gets the line width size currently being used by shapes.
     *
     * @return double the current line width.
     */
    public double getLineWidth() {
        return this.width;
    }

    /**
     * Sets a new font line size for text.
     *
     * @param fontSize the new text size.
     */
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Gets the font line size that is currently in use for the text tool.
     *
     * @return double the font size
     */
    public double getFontSize() {
        return this.fontSize;
    }

    /**
     * Sets the pen to use for future drawing. The pen sets which type of shape
     * to draw (hand drawing, line, rectangle, square, circle, ellipse, text).
     *
     * @param shape MyCanvas.Pen corresponding to the desired shape or object to draw.
     */
    public void setPen(Pen shape) {
        this.shapeType = shape;
    }

    /**
     * Gets the Pen / Shape Type that is currently in use. 
     *
     * @return MyCanvas.Pen the currently used Pen / Shape Type.
     */
    public Pen getPen() {
        return this.shapeType;
    }

    /**
     * Sets the mode that allows the user to interact with the canvas. 
     * Modes: Draw, Erase, Select, EyeDropper.
     * if user is using Select, it puts an empty zone behind where the image was selected
     *
     * @param m MyCanvas.Mode the mode selected by the user.
     */
    public void setMode(Mode m) {
        this.mode = m;

        //hide the selection rectangle
        if (m != Mode.SELECT && this.getChildren().contains(selection)) {

            //if in selection mode, print the content of previous selection
            if (selection.getContent() != null) {
                this.getChildren().add(selection.getContent());
                this.selection.empty();
            }

            this.getChildren().remove(selection);
        }
    }

    /**
     * Gets the Mode that is currently in use. The mode can be either selecting
     * parts of the image, eraser tool, dropper tool or regular drawing.
     *
     * @return MyCanvas.Mode the currently selected mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Adds a listener to allow the EyeDropper the ability to select a color
     * from the canvas.
     *
     * @param listener the listener to register.
     */
    void addListener(EyeDropper color) {
        this.colorPickedListeners.add(color);
    }

    /**
     * Undoes the last shape. Makes a copy of the last shape and puts it in
     * a redo stack for an eventual redo action. 
     */
    public void undo() {

        //check if there are more shapes than only the background
        if (this.undoAvailable()) {

            //the shape to remove is the last one in the pane's children list
            Shape copy = (Shape) this.getChildren().get(this.getChildren().size() - 1);

            //push the undone shape into the undo stack, for a possible redo
            this.undo.push(copy);

            //the shape is removed, it is considered as a change
            this.getChildren().remove(copy);
            this.edit = true;
        }
    }

    /**
     * Redoes the last 'undo' action. Pops shape off of the undo stack.
     */
    public void redo() {

        //check if there is something to redo
        if (this.redoAvailable()) {

            //the shape to redo is the last to have been pushed onto the undo stack
            Shape toAdd = this.undo.pop();

            //the shape is added to the pane's children again, it is considered a change
            this.getChildren().add(toAdd);
            this.edit = true;
        }
    }

    /**
     * Checks to see if there are any shapes on the canvas beside the background.
     *
     * @return true if undo is possible, false otherwise.
     */
    public boolean undoAvailable() {
        //can undo last action only if there is more shapes than only the background
        return this.getChildren().size() > 1;
    }

    /**
     * Checks the undo stack to see if anything can be popped off / redone. 
     *
     * @return true if redo is possible.
     */
    public boolean redoAvailable() {
        //can redo last undone action only if there is something in the stack
        return this.undo.size() > 0;
    }

    /**
     * Checks if there are any changes on the canvas. It resets to false when saving.
     *
     * @return True if changed. False if no changes.
     */
    public boolean hasChanged() {
        return this.edit;
    }

    /**
     * Displays an image from a file on the canvas.
     *
     * @param file the file from which to get the image
     */
    public void openImageFromFile(File file) {
        //gets the image from the file
        Image img = new Image(file.toURI().toString());

        //resets canvas and draws the image
        this.reset(img.getWidth(), img.getHeight(), new ImagePattern(img));
    }

    /**
     * Saves the canvas and anything on it in a file location
     *
     * @param file the file in which to save the image
     */
    public void saveImageAs(File file) {

        if (this.getChildren().contains(selection)) {
            this.getChildren().remove(selection);
        }

        if (file != null) {
            try {
                //gets the file type extension
                String extension = file.getName().split("\\.")[1];

                //creates the image
                BufferedImage snapshot = SwingFXUtils.fromFXImage(this.snapshot(new SnapshotParameters(), null), null);
                BufferedImage img = new BufferedImage(snapshot.getWidth(), snapshot.getHeight(), BufferedImage.TYPE_INT_RGB);

                for (int x = 0; x < snapshot.getWidth(); x++) {
                    for (int y = 0; y < snapshot.getHeight(); y++) {
                        img.setRGB(x, y, snapshot.getRGB(x, y));
                    }
                }

                //writing the file
                ImageIO.write(img, extension, file);
                this.edit = false;
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }
}

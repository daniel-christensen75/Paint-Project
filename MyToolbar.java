package pain.t;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A tool bar for the canvas. It allows the user the ability to select what
 * shapes they wish to draw as well as text. It gives the ability to change 
 * line and fill color for shapes. There are sliders for the line and text size
 * as well as an input box for the user to input text.
 *
 * @author Daniel Christensen
 */
public class MyToolbar extends VBox {

    private static ColorPicker cpLine = new ColorPicker(Color.BLACK);
    private static ColorPicker cpFill = new ColorPicker(Color.TRANSPARENT);
    private static double width;
    private static double LineWidth = 1;
    private static TextArea textLine = new TextArea();

    private MyCanvas canvas;

    /**
     * Create a new tool bar. Contains all of the options for drawing on the
     * canvas.
     *
     * @param c MyCanvas Links the paint canvas to the toolbar.
     */
    MyToolbar(MyCanvas c) {

        this.canvas = c;

        //Ability to change the line color
        cpLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //change the canvas' selected line color
                canvas.setLineColor(cpLine.getValue());
            }
        });

        //Ability to change fill color
        cpFill.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //change the canvas' selected fill color
                canvas.SetFillColor(cpFill.getValue());
            }
        });

        //listener to know when a color is picked from the canvas using the EyeDropper tool
        c.addListener(new EyeDropper() {
            @Override
            public void colorPicked(Color c) {
                cpLine.setValue(c);
            }
        });

        //Allows the user to either draw shapes, erase shapes, select parts of the canvas, or select a color
        ToggleGroup ModeToggle = new ToggleGroup();

        ToggleButton DrawMode = new ToggleButton();
        ToggleButton Eraser = new ToggleButton();
        ToggleButton SelectionTool = new ToggleButton();
        ToggleButton EyeDropper = new ToggleButton(); //the dropper tool

        //Allows user to select the buttons
        ModeToggle.getToggles().addAll(DrawMode, SelectionTool, Eraser, EyeDropper);

        //Shapes that the user can use when DrawMode is selected
        ToggleGroup ShapeToggle = new ToggleGroup();

        ToggleButton Draw = new ToggleButton();
        ToggleButton DrawLine = new ToggleButton();
        ToggleButton Rectangle = new ToggleButton();
        ToggleButton Circle = new ToggleButton();
        ToggleButton Ellipse = new ToggleButton();
        ToggleButton Text = new ToggleButton("Text");

        //Allows user to select shape and text button(s)
        ShapeToggle.getToggles().addAll(Draw, DrawLine, Rectangle, Circle, Ellipse, Text);

        //Text box that allows user to enter in text for the canvas
        textLine.setPromptText("Insert Text Here");
        textLine.setPrefRowCount(1);
        textLine.setPrefColumnCount(1);

        //Labels for toolbar
        Label lineWidth = new Label("Line Width: ");
        Label DrawOptions = new Label("Drawing Tools: ");
        Label lineColorLabel = new Label("Line Color: ");
        Label lineFillLabel = new Label("Fill Color: ");
        Label line_width = new Label("3.0");

        /**
         * *SLIDERS**
         */
        //Slider for Line size
        Slider slider = new Slider(0, 50, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.valueProperty().addListener(e -> {
            LineWidth = slider.getValue();
            canvas.setLineWidth(LineWidth);
        });

        //Slider for Text size
        Slider textSlider = new Slider(0, 150, 50);
        textSlider.setShowTickLabels(true);
        textSlider.valueProperty().addListener(e -> {
            width = textSlider.getValue();
            if (Text.isSelected()) {
                canvas.setLineWidth(1);
                canvas.setFontSize(textSlider.getValue());
                line_width.setText(String.format("%.1", width));
                return;
            }
            line_width.setText(String.format("%.1", width));
            canvas.setLineWidth(width);
        });

        /**
         * *Images and their Changes**
         */
        Image pencilImage = new Image(getClass().getResourceAsStream("Images/pencil.png"));
        Image lineImage = new Image(getClass().getResourceAsStream("Images/line.png"));
        Image circleImage = new Image(getClass().getResourceAsStream("Images/circle.png"));
        Image ellipseImage = new Image(getClass().getResourceAsStream("Images/ellipse.png"));
        Image squareImage = new Image(getClass().getResourceAsStream("Images/square.png"));
        Image scissorImage = new Image(getClass().getResourceAsStream("Images/Scissor.png"));
        Image eraserImage = new Image(getClass().getResourceAsStream("Images/eraser.png"));
        Image dropperImage = new Image(getClass().getResourceAsStream("Images/eyedropper.png"));

        //ImageViews to place on buttons
        ImageView pencilImageView = new ImageView(pencilImage);
        ImageView lineImageView = new ImageView(lineImage);
        ImageView circleImageView = new ImageView(circleImage);
        ImageView ellipseImageView = new ImageView(ellipseImage);
        ImageView rectangleImageView = new ImageView(squareImage);
        ImageView scissorImageView = new ImageView(scissorImage);
        ImageView eraserImageView = new ImageView(eraserImage);
        ImageView dropperImageView = new ImageView(dropperImage);

        //Putting the images on the buttons
        Draw.setGraphic(pencilImageView);
        Rectangle.setGraphic(rectangleImageView);
        Circle.setGraphic(circleImageView);
        Ellipse.setGraphic(ellipseImageView);
        DrawLine.setGraphic(lineImageView);
        SelectionTool.setGraphic(scissorImageView);
        Eraser.setGraphic(eraserImageView);
        EyeDropper.setGraphic(dropperImageView);

        //Editing images to make them fit the size of the button
        pencilImageView.setFitHeight(20);
        pencilImageView.setFitWidth(20);
        circleImageView.setFitHeight(20);
        circleImageView.setFitWidth(20);
        ellipseImageView.setFitHeight(20);
        ellipseImageView.setFitWidth(20);
        rectangleImageView.setFitHeight(20);
        rectangleImageView.setFitWidth(20);
        lineImageView.setFitHeight(20);
        lineImageView.setFitWidth(20);
        scissorImageView.setFitHeight(20);
        scissorImageView.setFitWidth(20);
        eraserImageView.setFitHeight(20);
        eraserImageView.setFitWidth(20);
        dropperImageView.setFitHeight(20);
        dropperImageView.setFitWidth(20);

        /**
         * *Adding functionality to buttons**
         */
        Draw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.FREE_DRAWING);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });
        Eraser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                canvas.setMode(MyCanvas.Mode.ERASE);
            }
        });
        DrawLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.LINE);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });
        Rectangle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.RECT);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });
        Circle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.CIRCLE);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });
        Ellipse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.ELLIPSE);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });
        Text.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setPen(MyCanvas.Pen.TEXT);
                ModeToggle.selectToggle(DrawMode);
                canvas.setMode(MyCanvas.Mode.DRAW);
            }
        });

        SelectionTool.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                canvas.setMode(MyCanvas.Mode.SELECT);
            }
        });

        EyeDropper.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.setMode(MyCanvas.Mode.EYEDROPPER);
            }
        });

        //Align everything along the left side of the VBox
        this.setAlignment(Pos.TOP_LEFT);

        //Add everything to the VBox
        this.getChildren().addAll(
                DrawOptions, Draw, Eraser, DrawLine, Rectangle, Circle,
                Ellipse, SelectionTool, EyeDropper, new Separator(), lineWidth,
                slider, lineColorLabel, cpLine, lineFillLabel, cpFill,
                new Separator(), Text, textLine, textSlider
        );

        //Creating the spacing for the ToolBar
        this.setSpacing(10);
        this.setPadding(new Insets(10));

    }

    /**
     * *END OF MYTOOLBAR()**
     * @return 
     */
    
    /*
     *Allows other classes to get the value from the text slider
     *Returns the width which is a double
     */
    
    public static double getLineWidth() {
        return LineWidth;
    }
    
    /**
     * Gets Text from the input box
     * @return String that user entered in the input box.
     */
    public static String getText() {
        return textLine.getText();
    }

    /**
     * Gets the line color that is currently in use.
     * @return Color Line Color that is currently in use.
     */
    public static Color getLineColor() {
        return cpLine.getValue();
    }

    /**
     * Gets the fill color that is currently in use.
     * @return Color Fill Color that is in use. 
     */
    public static Color getFillColor() {
        return cpFill.getValue();
    }

}

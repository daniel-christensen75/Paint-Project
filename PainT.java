package pain.t;

import java.io.File;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author Daniel Christensen
 */
public class PainT extends Application {

    public static Stage primaryStage;

    //Height and Width for the Main Window
    final private double WindowWidth = 1000;
    final private double WindowHeight = 800;

    //Height and Width for the Canvas
    final private double canvasWidth = WindowWidth - 162;
    final private double canvasHeight = WindowHeight - 33;

    //The path of the currently open file
    private String fileLocation = "";

    //Custom canvas for drawing on
    private MyCanvas canvas = new MyCanvas(canvasWidth, canvasHeight);

    @Override
    public void start(Stage stage) {
        //the main scene for everything to be placed on
        BorderPane borderPane = new BorderPane();

        //contain the custom tool bar
        VBox toolBox = new VBox();

        //Custom tool bar for drawing options
        MyToolbar toolbar = new MyToolbar(canvas);

        //the menu bar and the menus
        MenuBar menuBar = new MenuBar();

        //Menu File to go in the menu bar and its subheadings
        Menu menuFile = new Menu("File");
        MenuItem menuNew = new MenuItem("New");
        MenuItem menuOpen = new MenuItem("Open");
        MenuItem menuSave = new MenuItem("Save");
        MenuItem menuSaveAs = new MenuItem("Save as");
        MenuItem menuClose = new MenuItem("Quit");

        //Menu Edit to go in the menu bar and its subheadings
        Menu menuEdit = new Menu("Edit");
        MenuItem menuUndo = new MenuItem("Undo");
        MenuItem menuRedo = new MenuItem("Redo");

        //Menu Help to go in the menu bar and its subheadings
        Menu menuHelp = new Menu("Help");
        MenuItem help = new MenuItem("I dunno either");

        /**
         * Resets the canvas and asks the user if they would like to save
         * changes
         */
        menuNew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                //check if current file not saved
                if (smartSave()) {
                    canvas.reset();
                    fileLocation = "";
                }
            }
        });

        /**
         * Opens a menu for the user to choose a file from. If the previous file
         * contains changes, it will prompt the user if they would like to save
         * before resetting the canvas and opening the file.
         */
        menuOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open image");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));

                //show file chooser
                File file = fileChooser.showOpenDialog(stage);

                //check file exists
                if (file != null) {

                    //check if current file not saved
                    if (smartSave()) {
                        //reset canvas and load image
                        canvas.openImageFromFile(file);
                        fileLocation = file.getPath();
                    }
                }
            }
        });

        /**
         * Menu Save: saves the image in the currently open file.
         */
        menuSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                save();
            }
        });

        /**
         * Menu Save As: saves the image in a new image file.
         */
        menuSaveAs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveAs();
            }
        });

        /**
         * Menu Close: fires a close request.
         */
        menuClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });

        /**
         * Close request: close the application. Checks if modifications have
         * been made and asks the user to save previous work to prevent loss.
         */
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {

                if (!smartSave()) {
                    we.consume();
                }
            }
        });

        /**
         * Menu Undo: undo last action. Last drawing on the canvas is removed,
         * but able to be redone.
         */
        menuUndo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.undo();
            }
        });

        /**
         * Menu Redo: redo last drawing. Whatever was last undone is added
         * to the canvas again.
         */
        menuRedo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                canvas.redo();
            }
        });

        //Keyboard shortcuts for the menus
        menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        menuUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        menuRedo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        //Adding the menus to the MenuBar
        menuFile.getItems().addAll(menuNew, menuOpen, menuSave, menuSaveAs, menuClose);
        menuEdit.getItems().addAll(menuUndo, menuRedo);
        menuHelp.getItems().add(help);
        menuBar.getMenus().addAll(menuFile, menuEdit, menuHelp);

        //Window layout
        toolBox.getChildren().add(toolbar);
        borderPane.setCenter(canvas);
        borderPane.setTop(menuBar);
        borderPane.setLeft(toolbar);

        //Creating the Window
        Scene scene = new Scene(borderPane, WindowWidth, WindowHeight);
        stage.setTitle("Pain(t)™");
        stage.setScene(scene);
        stage.show();

        this.primaryStage = stage;
    }

    /**
     * Checks if canvas has been edited since last save, and asks user if they
     * want to save or not. If canvas has been changed, a
     * window is prompted asking the user if they want to save their work.
     * If they click 'yes', the save() function is called. If 'no', content is
     * not saved. Returns true if smart saving worked successfully or if changes were not made.
     * Returns false if saving failed.
     *
     * @return  <code>true</code> if save completed or not wanted,
     * <code>false</code> if user clicked <b>Cancel</b>, or saving failed.
     */
    private boolean smartSave() {

        //checks for changes
        if (canvas.hasChanged()) {

            //creates an alert box to allow user to save changes or not
            Optional<ButtonType> response = new AlertBox().showAndWait();

            //if user clicks 'yes', save the canvas and it's contents
            //returns if it saved or not
            if (response.get() == ButtonType.YES) {
                return save();
            }
            //returns true if user clicks 'cancel'
            if (response.get() != ButtonType.CANCEL) {
                return true;
            } else {
                return false;
            }

        } else {
            //return true if no changes to canvas;
            return true;
        }
    }

    /**
     * Saves canvas and it's changes as a JPEG or PNG file. Sets the location of
     * the file to fileLocation. If file location is empty, a window will be
     * displayed for the user to choose the location. Returns true or false
     * depending on if the file was save successfully or not.
     *
     * @return true if save completed, false otherwise
     */
    private boolean save() {

        //check if fileUrl is empty (new file)
        if (fileLocation == "") {
            //call saveAs() to ask user where to save file
            return saveAs();
        } else {
            File file = new File(fileLocation);

            //check if file exists
            if (file != null) {
                //save the image into the file and return true
                canvas.saveImageAs(file);
                return true;
            } else {
                //if problem with the file, return false
                return false;
            }
        }
    }

    /**
     * Saves the canvas and its changes as an image file. A window will be
     * displayed to allow the user to chose the saving location. Returns true or
     * false depending on if the file was saved successfully or not.
     *
     * @return true if save completed, false otherwise
     */
    private boolean saveAs() {
        File file;
        FileChooser fc = new FileChooser();
        fc.setTitle("Save image as");

        //extension filters to restrain to only image files
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));

        //show the file chooser
        file = fc.showSaveDialog(PainT.primaryStage);

        //check if file exists
        if (file != null) {
            //save the image into the file, set the fileLocation to be the new path and return true
            canvas.saveImageAs(file);
            fileLocation = file.getPath();
            return true;
        } else {
            //Return false if there is an error
            return false;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

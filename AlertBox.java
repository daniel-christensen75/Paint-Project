package pain.t;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Alert Box to appear if user needs to save.
 *
**/
public class AlertBox extends Alert {

    /**
     * Creates a new alert box for the user to save their work, or discard it.
     */
    public AlertBox() {
        super(Alert.AlertType.WARNING);
        this.setTitle("Ruh Row");
        this.setHeaderText("Would you like to save your work?");
        this.getButtonTypes().clear();
        this.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    }

}

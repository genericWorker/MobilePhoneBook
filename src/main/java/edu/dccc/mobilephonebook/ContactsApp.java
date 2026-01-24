package edu.dccc.mobilephonebook;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import java.io.IOException;

public class ContactsApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ContactsApp.class.getResource("contacts-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 700);

        // Connect CSS
        String css = this.getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Get the controller instance to call save logic
        ContactsAppController controller = fxmlLoader.getController();

        stage.setTitle("Contacts App");
        stage.setScene(scene);

        // INTERCEPT THE CLOSE BUTTON (X)
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Confirmation");
            alert.setHeaderText("Unsaved Changes");
            alert.setContentText("Would you like to save before exiting?");

            // Define Buttons
            ButtonType buttonSave = new ButtonType("Save and Exit");
            ButtonType buttonExit = new ButtonType("Exit Only");
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonSave, buttonExit, buttonCancel);

            // FIX: Apply CSS specifically to the DialogPane
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            // Optional: Add a class to the dialog pane if you want to target it specifically
            dialogPane.getStyleClass().add("my-alert");

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonSave) {
                    controller.onExitButtonClick();
                } else if (type == buttonExit) {
                    Platform.exit();
                } else {
                    event.consume(); // Keep the app open
                }
            });

        // This helps the dialog inherit the 'white card' look
            dialogPane.getStyleClass().add("main-container");
        });
        stage.show();
    }



    public static void main(String[] args) {
        launch();
    }

}
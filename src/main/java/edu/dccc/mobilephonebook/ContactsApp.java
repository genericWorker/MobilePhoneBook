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
        FXMLLoader fxmlLoader = new FXMLLoader(ContactsApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 700);

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

    // Static Inner Model Class
    public static class Contact implements Comparable<Contact>{
        private String name;
        private String phone;

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() { return name; }
        public String getPhone() { return phone; }

        @Override
        public String toString() {
            return name + " - " + phone;
        }

        @Override
        public int compareTo(Contact other) {
            // This provides the alphabetical sorting for the TreeSet
            return this.name.compareToIgnoreCase(other.getName());
        }
    }
}
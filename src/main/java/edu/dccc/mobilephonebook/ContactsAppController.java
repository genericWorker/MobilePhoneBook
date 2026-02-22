package edu.dccc.mobilephonebook;

import edu.dccc.store.CSVReaderWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.TreeSet;

public class ContactsAppController {

    @FXML private Button addButton; // Added @FXML
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField searchField;
    @FXML private ToggleButton directionToggle;
    @FXML private CheckBox singleSearchCheck;
    @FXML private Label statusLabel;
    @FXML private ListView<Contact> contactListView;
    @FXML private Label resultsLabel; // For Found/Iterations
    private final ArrayList<Contact> bridgeList = new ArrayList<>();
    private final DoublyLinkedList<Contact> storage = new DoublyLinkedList<Contact>();
    private final ObservableList<Contact> displayList = FXCollections.observableArrayList();
    // TODO:  Set what csv file you want to use. Root of project is default
    private final String FILE_NAME = "contacts2.csv";
    // Use the Generic version: CSVReaderWriter<Type>
    private CSVReaderWriter csvReaderWriter;

    @FXML
    public void initialize() {

        // 2. Initialize the Persistence Engine (The Utility)
        //  bridgeList is an ArrayList used for temporary sorting of contacts to/from storage
        //  bridgeList gets passed int the CSVReaderWriter
        csvReaderWriter = new CSVReaderWriter<>(FILE_NAME, bridgeList, Contact.class);

        // 3. Connect the ObservableList to the UI
        contactListView.setItems(displayList);

        // 4. LOAD DATA: CSV -> ArrayList (bridgeList)
        // 'false' means we don't want to append; we want a fresh load.
        csvReaderWriter.loadFromCSV(true);

        // 5. SYNC: ArrayList (bridgeList) -> DoublyLinkedList (storage)
        // Load the storage from th bridgeList.
        storage.clear();
        for (Contact c : bridgeList) {
            if (!storage.contains(c)) { // 1. Prevents the "Hang" (Memory Safety)
                storage.add(c);        // 2. Adds the contact
            }
        }

        // 6. UI EVENT LISTENERS
        // Trigger a search/refresh whenever the toggle or search text changes
        directionToggle.selectedProperty().addListener((obs, old, isNowSelected) -> performSearch(searchField.getText()));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch(newVal));

        // Listeners for Add Contact Validation (Enables/Disables the Add button)
        nameField.textProperty().addListener((obs, old, newVal) -> validateInput());
        phoneField.textProperty().addListener((obs, old, newVal) -> validateInput());

        // Double-click to Edit listener
        contactListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Contact selected = contactListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleEditPopup(selected);
                }
            }
        });

        // Also clear it when they click the list
        contactListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) clearStatus();
        });

        // 7. FINAL STATE: Set initial UI appearance
        validateInput();
        updateUI(); // This populates the UI displayList for the first time
        // Clear the status label when the user starts typing in any field
        resetStatus(); // Sets the initial "Ready" message
        nameField.setOnKeyTyped(e -> clearStatus());
        phoneField.setOnKeyTyped(e -> clearStatus());
        searchField.setOnKeyTyped(e -> clearStatus());
    }

    // Helper method to reset the label
    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    private void resetStatus() {
        // Show a helpful default, like the total count in your DoublyLinkedList
        int count = storage.getSize(); // Assuming your storage has a getSize() method
        statusLabel.setText("System Ready | Total Contacts: " + count);
        statusLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-weight: normal;");
    }

    private void showTimedStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

        // After 3 seconds, fade back to the default status
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(e -> resetStatus());
        pause.play();
    }

    private void validateInput() {
        String name = (nameField.getText() == null) ? "" : nameField.getText().trim();
        String rawPhone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();

        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");

        boolean nameOk = name.length() >= 2;
        boolean phoneOk = digitsOnly.length() >= 10;

        if (nameOk && phoneOk) {
            addButton.setDisable(false);
            addButton.setOpacity(1.0);
            // Button remains blue
            addButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold;");

            // Status: Bright Green for Dark BG
            statusLabel.setText("✔ Ready to add contact.");
            statusLabel.setStyle("-fx-text-fill: #4ADE80; -fx-font-weight: bold;");
        } else {
            addButton.setDisable(true);
            addButton.setOpacity(0.5);
            // Button becomes gray
            addButton.setStyle("-fx-background-color: #94A3B8; -fx-text-fill: white; -fx-font-weight: bold;");

            // Status: Light Blue-Gray for Dark BG
            statusLabel.setText("⚠ Waiting for name and phone..");
            statusLabel.setStyle("-fx-text-fill: #94A3B8;");
        }
    }

    private void handleEditPopup(Contact contact) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Contact");
        dialog.setHeaderText("Updating: " + contact.getName());

        TextField nameEdit = new TextField(contact.getName());
        TextField phoneEdit = new TextField(contact.getPhone());

        VBox content = new VBox(10, new Label("Name:"), nameEdit, new Label("Phone:"), phoneEdit);
        content.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 1. Get a reference to the OK button
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);

        // 2. Validation Logic
        Runnable validate = () -> {
            boolean nameBlank = nameEdit.getText().trim().isEmpty();
            boolean phoneBlank = phoneEdit.getText().trim().isEmpty();
            // You can also add length requirements here
            okButton.setDisable(nameBlank || phoneBlank);
        };

        // 3. Attach listeners so the button toggles as the user types
        nameEdit.textProperty().addListener((obs, old, val) -> validate.run());
        phoneEdit.textProperty().addListener((obs, old, val) -> validate.run());

        // Run once immediately to check current state
        validate.run();

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                contact.setName(nameEdit.getText().trim());
                contact.setPhone(phoneEdit.getText().trim());
                contact.setLastModified(java.time.LocalDateTime.now()); // Update timestamp

                updateUI();
                showTimedStatus("✔ Updated: " + contact.getName(), "#60A5FA");
           //   statusLabel.setStyle("-fx-text-fill: #60A5FA;");
            }
        });
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
        singleSearchCheck.setSelected(false);
        updateUI();
        searchField.requestFocus();
    }

    private void updateUI() {
        performSearch(searchField.getText());
    }

    @FXML
    protected void onAddButtonClick() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String digits = phone.replaceAll("[^0-9]", "");

        // Only auto-format if it's a standard 10-digit US number
        if (!phone.startsWith("+") && digits.length() == 10) {
            phone = digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }

        Contact newContact = new Contact(name, phone);
        storage.add(newContact);
        bridgeList.add(newContact); // This updates the CSV memory buffer
        nameField.clear();
        phoneField.clear();

        //  Adds new contact to list
        updateUI();
        validateInput(); // Correctly disables button after clear

        // Success Flash
        showTimedStatus("Successfully added: " + name, "#60A5FA");
    }

    private void performSearch(String query) {

        // 1. CRITICAL: Clear the selection before doing anything else
        // This prevents "Ghost Selection" from previous results
        contactListView.getSelectionModel().clearSelection();

        // 2. Handle Empty Query WITHOUT calling updateUI()
        if (query == null || query.isEmpty()) {
            // Instead of adding directly to displayList, use a TreeSet to sort them
            TreeSet<Contact> sortedResults = new TreeSet<>();
            for (Contact c : storage) {
                sortedResults.add(c);
            }
            displayList.setAll(sortedResults); // Populates UI in alphabetical order
            resultsLabel.setText("Total Contacts: " + storage.getSize());
            resetStatus();
            return; // EXIT HERE to stop the loop
        }

        Contact currentlySelected = contactListView.getSelectionModel().getSelectedItem();
        displayList.clear();
        int iterations = 0;

        // 2. Capture the ID or specific reference, not just the "content"
        String lowerQuery = (query == null) ? "" : query.toLowerCase().trim();
        String searchDigits = lowerQuery.replaceAll("[^0-9]", "");

        // Uses the 'Natural Ordering' defined in your Contact class
        TreeSet<Contact> results = new TreeSet<>();

        // This is the part that changes the iterations (The Data Structure lesson)
        //  TreeSet refreshed from storage, path means forward or backwards
        //  path is contact list of items from head or tail
        Iterable<Contact> path = directionToggle.isSelected() ? storage.backwards() : storage;

        for (Contact c : path) {
            iterations++;

            String contactDigits = c.getPhone().replaceAll("[^0-9]", "");
            boolean nameMatch = c.getName().toLowerCase().contains(lowerQuery);
            boolean phoneMatch = !searchDigits.isEmpty() && contactDigits.contains(searchDigits);

            if (lowerQuery.isEmpty() || nameMatch || phoneMatch) {
                results.add(c);

                if (singleSearchCheck.isSelected() && !lowerQuery.isEmpty()) {
                    break;
                }
            }
        }

        resultsLabel.setText(String.format("Found: %d | Iterations: %d", results.size(), iterations));
        displayList.setAll(results);

        if (currentlySelected != null)  {
            contactListView.getSelectionModel().select(currentlySelected);
        }
    }

    @FXML
    private void onSearchKeyReleased() {
        performSearch(searchField.getText());
    }

    @FXML
    protected void onDeleteButtonClick() {
        Contact selected = contactListView.getSelectionModel().getSelectedItem();

        // 2. GUARD CLAUSE: Stop immediately if null
        if (selected == null) {
            showTimedStatus("⚠ Please select a contact to delete first.", "#FBBF24");
            return; // This ensures NO code below this line runs
        }


        if (selected != null) {
            String deletedName = selected.getName();
            storage.remove(selected);

            updateUI();

            // Update the System Status Bar
            showTimedStatus("🗑 Deleted: " + deletedName, "#F87171");
        }
        else {
            // Warning if nothing is selected
            showTimedStatus("⚠ Please select a contact to delete first.", "#FBBF24");
        }
    }

    @FXML
    private void onExitButtonClick(ActionEvent event) {
        try {
            // 1. Update the status UI (nice for user feedback)
            statusLabel.setText("Saving contacts...");

            // 2. Delegate to your utility class.
            // This handles the header, the copy-to-list, and the sorting.
            csvReaderWriter.saveToCSVSorted("Name,Phone");

            // 3. Log to console for debugging
            System.out.println("Clean exit: Contacts saved alphabetically.");

            // 4. Close the JavaFX Application
            Platform.exit();

        } catch (Exception e) {
            // Just in case something goes wrong with the file system
            statusLabel.setText("Error during save!");
            e.printStackTrace();
        }
    }

    // Add an overloaded version for the Main App to call easily
    public void onExitButtonClick() {
        onExitButtonClick(null);
    }

    @FXML
    private void onSingleSearchToggled() {
        performSearch(searchField.getText());
    }
}
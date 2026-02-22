package edu.dccc.mobilephonebook;

import edu.dccc.utils.CSVReaderWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final String FILE_NAME = "contacts2.csv";
    // Use the Generic version: CSVReaderWriter<Type>
    CSVReaderWriter csvReaderWriter;




    @FXML
    public void initialize() {

        // 2. Initialize the Persistence Engine (The Utility)
        csvReaderWriter = new CSVReaderWriter<>(FILE_NAME, bridgeList, Contact.class);

        // 3. Connect the ObservableList to the UI
        contactListView.setItems(displayList);

        // 4. LOAD DATA: CSV -> ArrayList (bridgeList)
        // 'false' means we don't want to append; we want a fresh load.
        csvReaderWriter.loadFromCSV(false);

        // 5. SYNC: ArrayList (bridgeList) -> DoublyLinkedList (storage)
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

        // 7. FINAL STATE: Set initial UI appearance
        validateInput();
        updateUI(); // This populates the displayList for the first time
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
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Contact");
        dialog.setHeaderText("Updating: " + contact.getName());

        // Set up the input fields
        TextField nameEdit = new TextField(contact.getName());
        TextField phoneEdit = new TextField(contact.getPhone());

        VBox content = new VBox(10, new Label("Name:"), nameEdit, new Label("Phone:"), phoneEdit);
        content.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(content);

        // Add Save and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Process the result
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                contact.setName(nameEdit.getText());
                contact.setPhone(phoneEdit.getText());

                updateUI(); // Refresh list and resultsLabel
                statusLabel.setText("✔ Updated: " + contact.getName());
                statusLabel.setStyle("-fx-text-fill: #60A5FA;");
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

        updateUI();
        validateInput(); // Correctly disables button after clear

        // Success Flash
        statusLabel.setText("Successfully added: " + name);
        statusLabel.setStyle("-fx-text-fill: #60A5FA;"); // Light blue success color
    }

    private void performSearch(String query) {
        Contact currentlySelected = contactListView.getSelectionModel().getSelectedItem();
        displayList.clear();
        int iterations = 0;

        // 1. Capture the ID or specific reference, not just the "content"
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
        if (selected != null) {
            String deletedName = selected.getName();
            storage.remove(selected);

            updateUI();

            // Update the System Status Bar
            statusLabel.setText("🗑 Deleted: " + deletedName);
            statusLabel.setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;"); // Soft red for dark BG
        }
        else {
            // Warning if nothing is selected
            statusLabel.setText("⚠ Please select a contact to delete first.");
            statusLabel.setStyle("-fx-text-fill: #FBBF24;"); // Amber for warning
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
package edu.dccc.mobilephonebook;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ContactsAppController {

    @FXML private Button addButton; // Added @FXML
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField searchField;
    @FXML private ToggleButton directionToggle;
    @FXML private CheckBox singleSearchCheck;
    @FXML private Label statusLabel;
    @FXML private ListView<ContactsApp.Contact> contactListView;

    private final DoublyLinkedList<ContactsApp.Contact> storage = new DoublyLinkedList<>();
    private final ObservableList<ContactsApp.Contact> displayList = FXCollections.observableArrayList();
    private final String FILE_NAME = "contacts.csv";
    private CSVReaderWriter csvReaderWriter = new CSVReaderWriter(FILE_NAME, storage);


    @FXML
    public void initialize() {
        contactListView.setItems(displayList);
        csvReaderWriter.loadFromCSV(true);

        // Listeners for Search
        directionToggle.selectedProperty().addListener((obs, old, isNowSelected) -> performSearch(searchField.getText()));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch(newVal));

        // Listeners for Add Contact Validation
        nameField.textProperty().addListener((obs, old, newVal) -> validateInput());
        phoneField.textProperty().addListener((obs, old, newVal) -> validateInput());

        // Initial UI State
        validateInput();
        updateUI();
    }

    private void validateInput() {
        String name = (nameField.getText() == null) ? "" : nameField.getText().trim();
        String rawPhone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();

        // Strip everything but numbers to check actual digit count
        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");

        // Rules: Name 2+ chars, Phone 10+ digits
        boolean nameOk = name.length() >= 2;
        boolean phoneOk = digitsOnly.length() >= 10;

        if (nameOk && phoneOk) {
            addButton.setDisable(false);
            addButton.setOpacity(1.0);
            addButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
            statusLabel.setText("Ready to add contact.");
            statusLabel.setStyle("-fx-text-fill: #16A34A;");
        } else {
            addButton.setDisable(true);
            addButton.setOpacity(0.5);
            addButton.setStyle("-fx-background-color: #94A3B8; -fx-text-fill: white;");
            statusLabel.setText("Waiting for valid name and 10-digit phone...");
            statusLabel.setStyle("-fx-text-fill: #64748B;");
        }
    }

    private void performSearch(String query) {
        ContactsApp.Contact currentlySelected = contactListView.getSelectionModel().getSelectedItem();
        displayList.clear();
        ArrayList<ContactsApp.Contact> results = new ArrayList<>();
        int iterations = 0;

        String lowerQuery = (query == null) ? "" : query.toLowerCase().trim();
        String searchDigits = lowerQuery.replaceAll("[^0-9]", "");

        Iterable<ContactsApp.Contact> path = directionToggle.isSelected() ? storage.backwards() : storage;

        for (ContactsApp.Contact c : path) {
            iterations++;

            String contactDigits = c.getPhone().replaceAll("[^0-9]", "");
            boolean nameMatch = c.getName().toLowerCase().contains(lowerQuery);
            boolean phoneMatch = !searchDigits.isEmpty() && contactDigits.contains(searchDigits);

            if (lowerQuery.isEmpty() || nameMatch || phoneMatch) {
                results.add(c);
                if (singleSearchCheck.isSelected() && !lowerQuery.isEmpty()) break;
            }
        }

        // Sorting
        Comparator<ContactsApp.Contact> comp = Comparator.comparing(ContactsApp.Contact::getName, String.CASE_INSENSITIVE_ORDER);
        if (directionToggle.isSelected()) results.sort(comp.reversed());
        else results.sort(comp);

        displayList.setAll(results);

        if (currentlySelected != null && displayList.contains(currentlySelected)) {
            contactListView.getSelectionModel().select(currentlySelected);
        }

        statusLabel.setText("Found: " + results.size() + " | Iterations: " + iterations);
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

        storage.add(new ContactsApp.Contact(name, phone));

        nameField.clear();
        phoneField.clear();
        validateInput(); // Correctly disables button after clear
        updateUI();
    }

    @FXML
    private void onSearchKeyReleased() {
        performSearch(searchField.getText());
    }

    @FXML
    protected void onDeleteButtonClick() {
        ContactsApp.Contact selected = contactListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            storage.remove(selected);
            updateUI();
        }
    }

    @FXML
    protected void onExitButtonClick() {
        csvReaderWriter.saveToCSV();
        Platform.exit();
    }

    @FXML
    private void onSingleSearchToggled() {
        performSearch(searchField.getText());
    }


}
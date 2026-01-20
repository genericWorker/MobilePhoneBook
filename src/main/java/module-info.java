module edu.dccc.mobilephonebook {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.dccc.mobilephonebook to javafx.fxml;
    exports edu.dccc.mobilephonebook;
}
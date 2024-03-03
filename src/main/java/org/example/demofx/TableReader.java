package org.example.demofx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class TableReader {
    private TableWriter tableWriter = new TableWriter();
    private AlertUtility alertUtility = new AlertUtility();
    ObservableList<String> getAllTableNames() {
        ObservableList<String> tableNames = FXCollections.observableArrayList();
        try (Connection con = DatabaseConnection.connect()) {
            PreparedStatement preparedStatement = con.prepareStatement("SHOW TABLES");
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                tableNames.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching table names");
        }
        return tableNames;
    }


    TableView createTableViewForTable(String tableName) {
        TableView tableview = new TableView();
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        try (Connection con = DatabaseConnection.connect()) {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM " + tableName);
            ResultSet rs = preparedStatement.executeQuery();

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                tableview.getColumns().addAll(col);
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            // Print all rows for the current table
            System.out.println("*******************************");
            System.out.println("Table: " + tableName);
            for (ObservableList<String> row : data) {
                System.out.println(row);
            }

            tableview.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error building data for table: " + tableName);
        }
        return tableview;
    }


    void showAddRecordDialog(String tableName, TableView tableView) {
        // Create a dialog for user input
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Add Record");
        dialog.setHeaderText("Enter data for each column:");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create input fields and ComboBoxes for each column
        List<Node> inputFields = new ArrayList<>();
        for (Object column : tableView.getColumns()) {
            if (column instanceof TableColumn) {
                TableColumn tableColumn = (TableColumn) column;
                if (isForeignKeyColumn(tableName, tableColumn.getText())) {
                    // Create ComboBox for foreign key column
                    ComboBox<String> comboBox = createComboBoxForForeignKey(tableColumn.getText());
                    inputFields.add(comboBox);
                } else {
                    // Create TextField for other columns
                    TextField textField = new TextField();
                    textField.setPromptText("Enter " + tableColumn.getText());
                    inputFields.add(textField);
                }
            }

        }

        // Add input fields to the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < inputFields.size(); i++) {
            if (inputFields.get(i) != null) {
                grid.add(new Label(getColumnNameFromNode(inputFields.get(i))), 0, i);
                grid.add(inputFields.get(i), 1, i);
            }
        }

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable Add button depending on whether a TextField is empty
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Add listener to input fields for validation
        for (Node inputField : inputFields) {
            if (inputField instanceof TextField) {
                TextField textField = (TextField) inputField;
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    addButton.setDisable(inputFields.stream()
                            .filter(node -> node instanceof TextField)
                            .map(tf -> (TextField) tf)
                            .anyMatch(tf -> tf.getText().isEmpty()));
                });
            } else if (inputField instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) inputField;
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    addButton.setDisable(inputFields.stream()
                            .filter(node -> node instanceof ComboBox)
                            .map(cb -> (ComboBox) cb)
                            .anyMatch(cb -> cb.getValue() == null || cb.getValue().toString().isEmpty()));
                });
            }
        }

        // Set result converter to convert the result to a List<String>
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return inputFields.stream()
                        .map(node -> node instanceof TextField ? ((TextField) node).getText() : ((ComboBox<String>) node).getValue())
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            // Insert the new record into the table
            tableWriter.insertRecord(tableName, tableView, data);
        });
    }

    void showDeleteRecordDialog(String tableName, TableView tableView) {
        // Retrieve the selected row
        ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            // No row selected, show an alert
            alertUtility.showAlert("Error", "Please select a record to delete.");
            return;
        }

        // Ask for confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Confirm Deletion");
        confirmation.setContentText("Are you sure you want to delete this record?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed, delete the record
            tableWriter.deleteRecord(tableName, tableView, selectedRow);
        }
    }

    void showUpdateRecordDialog(String tableName, TableView tableView) {
        ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            alertUtility.showAlert("Error", "Please select a row to update.");
            return;
        }

        // Create a dialog for user input
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Update Record");
        dialog.setHeaderText("Update data for each column:");

        // Set the button types
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Create input fields and ComboBoxes for each column
        List<Node> inputFields = new ArrayList<>();
        for (int i = 0; i < selectedRow.size(); i++) {
            TableColumn column = (TableColumn) tableView.getColumns().get(i);
            String columnName = column.getText();

            if (isForeignKeyColumn(tableName, columnName)) {
                // Create ComboBox for foreign key column
                ComboBox<String> comboBox = createComboBoxForForeignKey(columnName);
                comboBox.setValue(selectedRow.get(i));
                inputFields.add(comboBox);
            } else {
                // Create TextField for other columns
                TextField textField = new TextField();
                textField.setPromptText("Enter " + columnName);
                textField.setText(selectedRow.get(i));
                inputFields.add(textField);
            }
        }

        // Add input fields to the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < inputFields.size(); i++) {
            if (inputFields.get(i) != null) {
                grid.add(new Label(getColumnNameFromNode(inputFields.get(i))), 0, i);
                grid.add(inputFields.get(i), 1, i);
            }
        }

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable Update button depending on whether a TextField is empty
        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(true);

        // Add listener to input fields for validation
        for (Node inputField : inputFields) {
            if (inputField instanceof TextField) {
                TextField textField = (TextField) inputField;
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    updateButton.setDisable(inputFields.stream()
                            .filter(node -> node instanceof TextField)
                            .map(tf -> (TextField) tf)
                            .anyMatch(tf -> tf.getText().isEmpty()));
                });
            } else if (inputField instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) inputField;
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    updateButton.setDisable(inputFields.stream()
                            .filter(node -> node instanceof ComboBox)
                            .map(cb -> (ComboBox) cb)
                            .anyMatch(cb -> cb.getValue() == null || cb.getValue().toString().isEmpty()));
                });
            }
        }

        // Set result converter to convert the result to a List<String>
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return inputFields.stream()
                        .map(node -> node instanceof TextField ? ((TextField) node).getText() : ((ComboBox<String>) node).getValue())
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            // Update the record in the table
            tableWriter.updateRecord(tableName, tableView, selectedRow, data);
        });
    }



    private boolean isForeignKeyColumn(String tableName, String columnName) {


        return (tableName.equals("car") && columnName.equals("made"))
                || (tableName.equals("device") && columnName.equals("made"))
                || (tableName.equals("customer") && columnName.equals("address"))
                || (tableName.equals("car_part") && columnName.equals("part") || columnName.equals("car"))
                || ((tableName.equals("orders") && (columnName.equals("customer")) ||(tableName.equals("order") && columnName.equals("car") )));
    }

private ComboBox<String> createComboBoxForForeignKey(String foreignKeyColumn) {
    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setPromptText("Select " + foreignKeyColumn);

    try (Connection con = DatabaseConnection.connect()) {
        // Define a map to store relationships between foreign key columns and their corresponding tables and columns
        Map<String, TableColumnMapping> columnMappings = new HashMap<>();
        columnMappings.put("made", new TableColumnMapping("manufacture", "name"));
        columnMappings.put("address", new TableColumnMapping("address", "id"));
        columnMappings.put("part", new TableColumnMapping("device", "no"));
        columnMappings.put("car", new TableColumnMapping("car", "name"));
        columnMappings.put("customer", new TableColumnMapping("customer", "id"));


        // Check if the provided foreignKeyColumn is in the map
        if (columnMappings.containsKey(foreignKeyColumn)) {
            // Get the corresponding table and column information
            TableColumnMapping mapping = columnMappings.get(foreignKeyColumn);

            // Construct the SQL query dynamically
            String sql = "SELECT " + mapping.getColumnName() + " FROM " + mapping.getTableName();
            try (PreparedStatement preparedStatement = con.prepareStatement(sql);
                 ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    comboBox.getItems().add(rs.getString(mapping.getColumnName()));
                }
            }
        } else {
            System.out.println("Foreign key column mapping not found: " + foreignKeyColumn);
        }

    } catch (SQLException | ClassNotFoundException e) {
        e.printStackTrace();
        System.out.println("Error fetching foreign key values for column: " + foreignKeyColumn);
    }

    return comboBox;
}


    private String getColumnNameFromNode(Node node) {
        if (node instanceof TextField) {
            return ((TextField) node).getPromptText();
        } else if (node instanceof ComboBox) {
            return ((ComboBox) node).getPromptText();
        }
        return "";
    }

    //  Helper class to represent the mapping between table and column
    private static class TableColumnMapping {
        private final String tableName;
        private final String columnName;

        public TableColumnMapping(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getColumnName() {
            return columnName;
        }
    }
}


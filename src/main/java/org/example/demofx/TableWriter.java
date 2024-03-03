package org.example.demofx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TableWriter {
    AlertUtility alertUtility = new AlertUtility();

    void insertRecord(String tableName, TableView tableView, List<String> data) {
        boolean cheeker = true;
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        Connection con = null;

        try {
            // Data type validations
            for (int i = 0; i < tableView.getColumns().size(); i++) {
                TableColumn column = (TableColumn) tableView.getColumns().get(i);
                String columnName = column.getText();
                String value = data.get(i);

                if ("year".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid year.");
                    return;
                }
                if (("price".equals(columnName) || "weight".equals(columnName)) && !isNumeric(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid Price and Weight.");
                    return;
                }
                if ("no".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid no.");
                    return;
                }
                if ("id".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid id.");
                    return;
                }
                if ("buidling".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid buidling.");
                    return;
                }
                if ("country".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid country.");
                    return;
                }
                if ("city".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid city.");
                    return;
                }
                if ("model".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid model.");
                    return;
                }
                if (("f_name".equals(columnName) || "l_name".equals(columnName)) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid name.");
                    return;
                }
                if ("job".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid job.");
                    return;
                }
                if ("name".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid name.");
                    return;
                }
                if ("type".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid type.");
                    return;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            con = DatabaseConnection.connect();

            // Build the INSERT query
            queryBuilder.append(tableName).append(" VALUES (");

            for (int i = 0; i < data.size(); i++) {
                queryBuilder.append("'").append(data.get(i)).append("'");
                if (i < data.size() - 1) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(")");


            // Execute the INSERT query
            try (PreparedStatement preparedStatement = con.prepareStatement(queryBuilder.toString())) {
                preparedStatement.executeUpdate();

                // Refresh the TableView to display the new record
                tableView.getItems().add(FXCollections.observableArrayList(data));

                // Show success alert
                alertUtility.showAlert(Alert.AlertType.INFORMATION, "Success", "New record added successfully.");
            } catch (SQLException e) {
                cheeker = false;
                // Check for duplicate entry error
                if (e.getErrorCode() == 1062) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Duplicate entry! Please try again.");
                } else {
                    e.printStackTrace();
                    System.out.println("Error inserting record into table: " + tableName);
                    System.out.println("SQL State: " + e.getSQLState());
                    System.out.println("Error Code: " + e.getErrorCode());
                    System.out.println("Error Message: " + e.getMessage());
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("General error inserting record into table: " + tableName);
        } finally {
            DatabaseConnection.close(con);
            if (cheeker) {
                // Print the INSERT query
                System.out.println("*******************************");
                System.out.println("Executing Insert Query: " + queryBuilder.toString());
            }
        }
    }


    void updateRecord(String tableName, TableView tableView, ObservableList<String> selectedRow, List<String> newData) {
        try {
            // Data type validations
            for (int i = 0; i < tableView.getColumns().size(); i++) {
                TableColumn column = (TableColumn) tableView.getColumns().get(i);
                String columnName = column.getText();
                String value = newData.get(i);

                if ("year".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid year.");
                    return;
                }
                if (("price".equals(columnName) || "weight".equals(columnName)) && !isNumeric(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid Price and Weight.");
                    return;
                }
                if ("no".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid no.");
                    return;
                }
                if ("id".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid id.");
                    return;
                }
                if ("buidling".equals(columnName) && !isInteger(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid buidling.");
                    return;
                }
                if ("country".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid country.");
                    return;
                }
                if ("city".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid city.");
                    return;
                }
                if ("model".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid model.");
                    return;
                }
                if (("f_name".equals(columnName) || "l_name".equals(columnName)) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid name.");
                    return;
                }
                if ("job".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid job.");
                    return;
                }
                if ("name".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid name.");
                    return;
                }
                if ("type".equals(columnName) && !isString(value)) {
                    alertUtility.showAlert(Alert.AlertType.ERROR, "Error", "Enter valid type.");
                    return;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Connection con = null;

        try{
            con = DatabaseConnection.connect();
            // Build the UPDATE query
            StringBuilder queryBuilder = new StringBuilder("UPDATE ");
            queryBuilder.append(tableName).append(" SET ");

            // Print the values of the updated record
            System.out.println("*******************************");
            System.out.println("Updating record in table: " + tableName);
            System.out.println("Original Record Values: " + selectedRow);
            System.out.println("New Record Values: " + newData);

            for (int i = 0; i < newData.size(); i++) {
                TableColumn column = (TableColumn) tableView.getColumns().get(i);
                String columnName = column.getText();
                String newValue = newData.get(i);

                queryBuilder.append(columnName).append(" = '").append(newValue).append("'");

                if (i < newData.size() - 1) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(" WHERE ");

            for (int i = 0; i < selectedRow.size(); i++) {
                TableColumn column = (TableColumn) tableView.getColumns().get(i);
                String columnName = column.getText();
                String cellValue = selectedRow.get(i);

                queryBuilder.append(columnName).append(" = '").append(cellValue).append("'");

                if (i < selectedRow.size() - 1) {
                    queryBuilder.append(" AND ");
                }
            }

            // Execute the UPDATE query
            try (PreparedStatement preparedStatement = con.prepareStatement(queryBuilder.toString())) {
                preparedStatement.executeUpdate();

                // Refresh the TableView to reflect the update
                int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
                tableView.getItems().set(selectedIndex, FXCollections.observableArrayList(newData));

                // Show a success alert
                alertUtility.showAlert("Success", "Record updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error updating record in table: " + tableName);

                // Show an error alert
                alertUtility.showAlert("Error", "Error updating record.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database.");
        }finally {
            DatabaseConnection.close(con);
        }
    }



    void deleteRecord(String tableName, TableView tableView, ObservableList<String> selectedRow) {
        Connection con = null;

        try {
            // Build the DELETE query
            StringBuilder queryBuilder = new StringBuilder("DELETE FROM ");
            queryBuilder.append(tableName).append(" WHERE ");

            System.out.println("*******************************");
            System.out.println("Deleting record from table: " + tableName);
            System.out.println("Deleted Record Values: " + selectedRow);

            for (int i = 0; i < selectedRow.size(); i++) {
                TableColumn column = (TableColumn) tableView.getColumns().get(i);
                String columnName = column.getText();
                String cellValue = (String) column.getCellData(tableView.getSelectionModel().getSelectedIndex());

                queryBuilder.append(columnName).append(" = '").append(cellValue).append("'");

                if (i < selectedRow.size() - 1) {
                    queryBuilder.append(" AND ");
                }
            }

            // Execute the DELETE query
            con = DatabaseConnection.connect();
            try (PreparedStatement preparedStatement = con.prepareStatement(queryBuilder.toString())) {
                preparedStatement.executeUpdate();

                // Refresh the TableView to reflect the deletion
                tableView.getItems().remove(selectedRow);

                // Show a success alert
                alertUtility.showAlert("Success", "Record deleted successfully.");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error deleting record from table: " + tableName);

                // Show an error alert
                alertUtility.showAlert("Error", "Error deleting record.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            DatabaseConnection.close(con);
        }
    }

    private boolean isString(String value) {
        // Regular expression to check if the string does not contain any digits
        return value != null && !value.matches(".*\\d.*");
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}


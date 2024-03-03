package org.example.demofx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;

public class SearchUtility {


    void filterTable(TableView tableView, TableColumn column, String value) {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        // Iterate through all rows in the original data
        for (Object row : tableView.getItems()) {
            // Check if any column value contains the filter value
            boolean containsFilter = false;
            for (int i = 0; i < tableView.getColumns().size(); i++) {
                TableColumn currentColumn = (TableColumn) tableView.getColumns().get(i);
                String cellValue = ((ObservableList<String>) row).get(i);

                // Check if the cell value contains the filter value
                if (cellValue.toLowerCase().contains(value.toLowerCase())) {
                    containsFilter = true;
                    break;
                }
            }

            // If any column value contains the filter value, add the row to the filtered data
            if (containsFilter) {
                data.add((ObservableList<String>) row);
            }
        }

        // Set the filtered data to the TableView
        tableView.setItems(data);
    }

    void searchTable(TableView tableView, HBox searchBox) {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        // Retrieve the associated Tab from the properties of the TableView
        Tab tab = (Tab) tableView.getProperties().get("associatedTab");

        // Build the SQL query based on user input
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(tab.getText()).append(" WHERE ");

        for (Node node : searchBox.getChildren()) {
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
                String columnName = textField.getPromptText().substring("Search ".length());

                // Append the condition for each text field
                queryBuilder.append(columnName).append(" LIKE ? AND ");
            }
        }

        // Remove the trailing " AND " from the query
        queryBuilder.delete(queryBuilder.length() - 5, queryBuilder.length());

        try (Connection con = DatabaseConnection.connect()) {
            // Prepare the statement with the built query
            try (PreparedStatement preparedStatement = con.prepareStatement(queryBuilder.toString())) {
                // Set the parameters for each text field
                int parameterIndex = 1;
                for (Node node : searchBox.getChildren()) {
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                        preparedStatement.setString(parameterIndex++, "%" + textField.getText() + "%");
                    }
                }

                // Execute the query
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    // Populate the data with the search results
                    while (rs.next()) {
                        ObservableList<String> row = FXCollections.observableArrayList();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            row.add(rs.getString(i));
                        }
                        data.add(row);
                    }

                    // Clear existing data and set the new data to the TableView
                    tableView.getItems().clear();
                    tableView.setItems(data);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error executing search query");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database.");
        }

        // Clear all text fields in the search box
        for (Node node : searchBox.getChildren()) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            }
        }
    }

}



package org.example.demofx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    TableReader tableReader = new TableReader();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Database Viewer");
        SearchUtility searchUtility = new SearchUtility();
        TabPane tabPane = new TabPane();


        ObservableList<String> tableNames = tableReader.getAllTableNames();
        for (String tableName : tableNames) {
            Tab tab = new Tab(tableName);
            TableView tableView = tableReader.createTableViewForTable(tableName);
            HBox searchBox = createSearchBox(tableView);

            VBox searchAndAddVBox = getvBox(tableName, tableView, searchBox);
            tab.setContent(searchAndAddVBox);

            VBox vBox = new VBox(searchAndAddVBox, tableView);
            tab.setContent(vBox);

            tableView.getProperties().put("associatedTab", tab);

            tabPane.getTabs().add(tab);
        }

        BorderPane root = new BorderPane();

        root.setCenter(tabPane);
        Label footerLabel = new Label("Developed By Husam Ramoni\nFor SWER351 - Dr. Anas Samara\nBethlehem University\n" +
                "JavaFX application for managing a database using tabs and tables. It allows users to view, " +
                "add, delete, and update records in various database tables.\n");
        
        footerLabel.setStyle("-fx-padding: 10; -fx-alignment: bottom-left;");
        root.setBottom(footerLabel);

//        Image backgroundImage;
//        try {
//            backgroundImage = new Image(getClass().getResourceAsStream("/b1.jpeg"));
//        } catch (Exception e) {
//            System.out.println("Background image not found. Check your path and resource folder.");
//            e.printStackTrace(); // This will print more details about the error
//            backgroundImage = null;
//        }
//
//        if (backgroundImage != null) {
//            // Create a BackgroundImage object
//            BackgroundImage background = new BackgroundImage(backgroundImage,
//                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                    BackgroundPosition.CENTER,
//                    BackgroundSize.DEFAULT);
//
//            // Set the background to the BorderPane
//            root.setBackground(new Background(background));
//        }


        Scene scene = new Scene(root, 1010, 700);

        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
   private HBox createSearchBox(TableView tableView) {
        SearchUtility searchUtility = new SearchUtility();
        HBox searchBox = new HBox(5);
        ObservableList<TableColumn> columns = tableView.getColumns();

        Button searchButton = new Button("Refresh");
        searchButton.setStyle("-fx-background-color: #373739; -fx-text-fill: white;");
        searchButton.setOnAction(event -> searchUtility.searchTable(tableView, searchBox));
        // Add the search button to the HBox first
        searchBox.getChildren().add(searchButton);

        // Loop through columns to create text fields
        for (TableColumn column : columns) {
            TextField textField = new TextField();
            textField.setPromptText("Search " + column.getText());
            textField.textProperty().addListener((observable, oldValue, newValue) ->
                    searchUtility.filterTable(tableView, column, newValue));
            // Add each text field to the HBox
            searchBox.getChildren().add(textField);
        }

        return searchBox;
    }

    private VBox getvBox(String tableName, TableView tableView, HBox searchBox) {
        Button addRecordButton = new Button("Add Record");
        addRecordButton.setStyle("-fx-background-color: #373739; -fx-text-fill: white;");
        addRecordButton.setOnAction(event -> tableReader.showAddRecordDialog(tableName, tableView));

        HBox hB1 = gethBox(tableName, tableView, addRecordButton);


        VBox searchAndAddVBox = new VBox(searchBox, hB1);
        searchAndAddVBox.setPadding(new Insets(20, 10, 10, 10));
        searchAndAddVBox.setSpacing(20);
        return searchAndAddVBox;
    }

    private HBox gethBox(String tableName, TableView tableView, Button addRecordButton) {
        Button deleteRecordButton = new Button("Delete Record");
        deleteRecordButton.setStyle("-fx-background-color: #373739; -fx-text-fill: white;");
        deleteRecordButton.setOnAction(event -> tableReader.showDeleteRecordDialog(tableName, tableView));

        Button updateRecordButton = new Button("Update Record");
        updateRecordButton.setStyle("-fx-background-color: #373739; -fx-text-fill: white;");
        updateRecordButton.setOnAction(event -> tableReader.showUpdateRecordDialog(tableName, tableView));

        HBox hB1 = new HBox(addRecordButton, deleteRecordButton, updateRecordButton);
        hB1.setSpacing(10);
        return hB1;
    }
}

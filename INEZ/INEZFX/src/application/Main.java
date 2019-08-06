package application;
	
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.*;
import java.util.*;

public class Main extends Application {
	private BrainAPI brain = new Brain();
    private final ObservableList<Product> products
            = FXCollections.observableArrayList();
    @Override
    public void start(Stage stage) throws IOException {
    	TextField textField = new TextField();
        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                products.add(new Product(textField.getText()));
                products.addAll(brain.calculateSuggestions(textField.getText()));
            }
        });
        final VBox vbox = new VBox();
        vbox.getChildren().addAll(createList(300, 600),addButton);
        vbox.getChildren().add(textField);

        Scene scene = new Scene(new Group(), 400, 600);
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        stage.setScene(scene);
        stage.show();
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        brain.startBrain();
    }

    public static void main(String[] args) {
        launch();
    }


    private GridPane createList(int minWith, int minHeight) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.minWidth(minWith);
        grid.minHeight(minHeight);

        TableView table = new TableView();
        table.setEditable(true);

        TableColumn productCol = new TableColumn("Einkaufsliste");
        productCol.setCellValueFactory(new PropertyValueFactory<Product,String>("listName"));

        table.getColumns().addAll(productCol);
        productCol.setMinWidth(minWith);
        table.setItems(products);
        GridPane.setConstraints(table, 0,0);

        grid.getChildren().addAll(table);

        return grid;

    }
    

    private static void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
    }
}

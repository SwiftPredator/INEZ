package application;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

public class Controller implements Initializable{
	/* Flags */
	private Boolean showSuggestions = true;
	private Boolean autoAddAmount = false;
	/* Model and own Datastructers */
	private BrainAPI brain = new Brain();
    private final ObservableList<Product> products
            = FXCollections.observableArrayList();
    private final ObservableList<Product> suggProducts
    		= FXCollections.observableArrayList();	
    /* Scene Objects */
    @FXML public Button submitButton;
    @FXML public Button editButton;
	@FXML public TextField listInput;
	@FXML public CheckBox showSuggsCheck;
	@FXML public CheckBox autoAddAmountCheck;
	
	@FXML public TableView<Product> tableView;
	@FXML public TableColumn<Product, String> listProductCol;
	@FXML public TableColumn<Product, String> listOptions;
	
	@FXML public TableView<Product> suggTableView;
	@FXML public TableColumn<Product, String> suggProductCol;
		
	public void addOnList() {
		System.out.println("clicked");
		if(showSuggestions) {
			suggProducts.clear();
			new Thread(() -> {
				submitButton.setDisable(true);
				suggProducts.addAll(brain.calculateSuggestions(listInput.getText()));
				submitButton.setDisable(false);
			}).start();
		}
		Product newProduct = new Product(listInput.getText());
		if(autoAddAmount) {
			new Thread(() -> {
				ArrayList<Product> temp_suggs = brain.calculateSuggestions(listInput.getText());
				newProduct.setListName(temp_suggs.get(0).getAmount() + " " + newProduct.getName());
				products.add(newProduct);
			}).start();
		} else {
			products.add(newProduct);
		}
	}
	
	public void initialize(URL url, ResourceBundle rb) {
		System.out.println("init");
		//Init Model
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
	    brain.startBrain();
	    //Init View
        tableView.setEditable(true);
        listProductCol.setCellValueFactory(new PropertyValueFactory<Product,String>("listName"));
        Callback<TableColumn<Product, String>, TableCell<Product, String>> cellFactory
        = //
        new Callback<TableColumn<Product, String>, TableCell<Product, String>>() {
        	@Override
        	public TableCell call(final TableColumn<Product, String> param) {
        		final TableCell<Product, String> cell = new TableCell<Product, String>() {
        			
        			Button btn = new Button();
        			
        			
        			@Override
        			public void updateItem(String item, boolean empty) {
        				super.updateItem(item, empty);
        				if (empty) {
        					setGraphic(null);
        					setText(null);
        				} else {
        					btn.setOnAction(event -> {
        						Product p = getTableView().getItems().get(getIndex());
        						products.remove(p);
        					});
        					Image image = new Image(getClass().getResourceAsStream("/deleteIcon.png"));
        					btn.setGraphic(new ImageView(image));
  
        					setGraphic(btn);
        					setText(null);
        				}
        			}
        		};
        		return cell;
        	}
        };
        listOptions.setCellFactory(cellFactory);
        listOptions.setVisible(false);
        tableView.setItems(products);
        suggTableView.setEditable(true);
        suggProductCol.setCellValueFactory(new PropertyValueFactory<Product,String>("listName"));
        suggTableView.setItems(suggProducts);
        showSuggsCheck.setOnAction(e -> switchCheckBox(1));
        autoAddAmountCheck.setOnAction(e -> switchCheckBox(2));
        editButton.setOnAction(e -> toogleOptionsForList(1));
    }
	
	public void switchCheckBox(int id) {
		switch(id) {
			case 1:
				if(showSuggestions) {
					showSuggestions = false;
				} else {
					showSuggestions = true;
				}
				break;
			case 2:
				if(autoAddAmount) {
					autoAddAmount = false;
				} else {
					autoAddAmount = true;
				}
				break;
			default:
				break;
		}
	}

	public void toogleOptionsForList(int id) {
		switch(id) {
			case 1:
				listOptions.setVisible(true);
				editButton.setText("Bearbeiten beenden");
				editButton.setOnAction(e -> toogleOptionsForList(2));
				break;
			case 2:
				listOptions.setVisible(false);
				editButton.setText("Einkaufsliste bearbeiten");
				editButton.setOnAction(e -> toogleOptionsForList(1));
				break;
			default:
				break;
		}
	}
	
	@FXML
	public void clickItem(MouseEvent event) {
		System.out.println("clicked");
		products.remove(products.size() - 1);
		products.add(suggTableView.getSelectionModel().getSelectedItem());
	}
}

package application;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ImageIcon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.util.Callback;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;

import org.controlsfx.control.textfield.TextFields;


/**
 * Controller for the View. Matches the MVC-Pattern.
 * Controlls all Objects in the MainView
 * @author Paul Heinemeyer
 *
 */
public class Controller implements Initializable{
	/* Flags */
	private Boolean showSuggestions = true;
	private Boolean autoAddAmount = false;
	private Boolean autoCorrectur = false;
	
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
	@FXML public CheckBox autoCorrectCheck;
	@FXML public ProgressIndicator progressIndi;
	
	@FXML public TableView<Product> tableView;
	@FXML public TableColumn<Product, String> listProductCol;
	@FXML public TableColumn<Product, String> listOptions;
	
	@FXML public TableView<Product> suggTableView;
	@FXML public TableColumn<Product, String> suggProductCol;
	
	@FXML
	/**
	 * Adds Item on the user-List 
	 * Looks at the flags and calculates suggestion and auto-writes the amount for a given product, if flags are set.
	 */
	public void addOnList() {
		if(!brain.syntaxCheckInput(listInput.getText())) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information zur Eingabe");
			alert.setHeaderText("Inkorrektes Format der Eingabe");
			alert.setContentText("Bitte überprüfen Sie ob Ihre Eingabe eines der folgenden Formate hat: \n '1x 1l Milch' \n '1-Liter Milch' \n '1x Milch' \n 'Milch'");
			alert.showAndWait();
			return;
		}
		@SuppressWarnings("unchecked")
		Product newProduct = brain.calculateProductFromString(listInput.getText());
		sortHelper prefixHelper = brain.calculatePrefixAmount(newProduct, new ArrayList<Product>(tableView.getItems()));
		System.out.println("PREFIX: " + prefixHelper.value + prefixHelper.index);
		if(prefixHelper.index != -1) {
			products.remove(prefixHelper.index);
		}
		newProduct.setPrefixAmount(prefixHelper.value);
		if(showSuggestions) {
			suggProducts.clear();
			new Thread(() -> {
				progressIndi.setVisible(true);
				ArrayList<Product> temp_suggs = brain.calculateSuggestions(newProduct.getName(), 5);
				submitButton.setDisable(true);
				suggProducts.addAll(temp_suggs);
				submitButton.setDisable(false);
				if(autoAddAmount) {
					newProduct.setListName(newProduct.getPrefixAmount() + "x" + " " + brain.calculateBestFittingAmount(temp_suggs) + " " + newProduct.getName());
					products.add(newProduct);
				}
				progressIndi.setVisible(false);
			}).start();
		}
		if(autoAddAmount) {
			if(showSuggestions) {
				return;
			}
			new Thread(() -> {
				progressIndi.setVisible(true);
				ArrayList<Product> temp_suggs = brain.calculateSuggestions(newProduct.getName(), 5);
				newProduct.setListName(newProduct.getPrefixAmount() + "x" + " " + brain.calculateBestFittingAmount(temp_suggs) + " " + newProduct.getName());
				products.add(newProduct);
				progressIndi.setVisible(false);
			}).start();
		} else {
			newProduct.setListName(newProduct.getPrefixAmount() + "x" + " " + newProduct.getAmount() + " "+ newProduct.getName());
			products.add(newProduct);
		}
	}
	
	/**
	 * Inits the MainView and all its objects. 
	 * Fetches the saved user-List
	 */
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
        			
        			Button btn_delete = new Button();
        			
        			@Override
        			public void updateItem(String item, boolean empty) {
        				super.updateItem(item, empty);
        				if (empty) {
        					setGraphic(null);
        					setText(null);
        				} else {
        					btn_delete.setOnAction(event -> {
        						Product p = getTableView().getItems().get(getIndex());
        						products.remove(p);
        					});
        					Image image = new Image(getClass().getResourceAsStream("/deleteIcon.png"));
        					btn_delete.setGraphic(new ImageView(image));
  
        					setGraphic(btn_delete);
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
        autoCorrectCheck.setOnAction(e -> switchCheckBox(3));
        editButton.setOnAction(e -> toogleOptionsForList(1));
        progressIndi.setVisible(false);
        
        
     
        Set<String> autoCompletions = new HashSet<>(Arrays.asList(""));
        SuggestionProvider<String> provider = SuggestionProvider.create(autoCompletions);
        new AutoCompletionTextFieldBinding<>(listInput, provider);
        listInput.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
            	if(autoCorrectur) {
            		provider.clearSuggestions();
            		provider.addPossibleSuggestions(brain.spellCheck(newValue));
            	}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
        });
        
        //fetch products
        try {
			this.products.addAll(brain.fetchSavedProducts(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/listProducts.txt"));
		} catch (Exception e1) {
			System.out.println("no list products found");
		}
    }
	/**
	 * Saves the current list on a file in json format, before application closes
	 */
	public void saveUserList() {
		final Gson gson = new Gson();
		String s = gson.toJson(products);
		try {
			brain.saveProducts(s, FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/listProducts.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Switches the Flags for options. ID = 1: show suggestions for products; ID = 2: add amounts automatically;
	 * @param id
	 */
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
			case 3:
				if(autoCorrectur) {
					autoCorrectur = false;
				} else {
					autoCorrectur = true;
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Switches the text and action for the edit-List Button. ID = 1: mode = edit; ID = 2: mode = no edit;
	 * @param id
	 */
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
	/**
	 * adds clicked suggestion to user list
	 * @param event
	 */
	public void clickItem(MouseEvent event) {
		System.out.println("clicked");
		Product newProduct = new Product(suggTableView.getSelectionModel().getSelectedItem().getName());
		newProduct.setListName(suggTableView.getSelectionModel().getSelectedItem().getListName());
		newProduct.setAmount(suggTableView.getSelectionModel().getSelectedItem().getAmount());
		newProduct.setPrefixAmount(products.get(products.size()-1).getPrefixAmount());
		newProduct.setListName(newProduct.getPrefixAmount() + "x" + " " + newProduct.getListName());
		products.remove(products.size() - 1);
		products.add(newProduct);
	}
	
	@FXML
	
	/**
	 * @deprecated
	 * Opens up a TextInput Dialog to edit the selected item in the user-List. Currently not avaible.
	 * @param event
	 */
	public void editListItem(MouseEvent event) {
		/*
        if (event.getClickCount() >= 2) {
        	TextInputDialog td = new TextInputDialog(tableView.getSelectionModel().getSelectedItem().getListName()); 
            td.setHeaderText("Geben Sie Ihr neues Produkt ein");
            final Optional<String> result = td.showAndWait();
            if (result.isPresent()) {
            	System.out.println("present");
            	try {
            		final String newProdName = result.get();
            		Product p = tableView.getSelectionModel().getSelectedItem();
            		p.setListName(newProdName);
            		tableView.refresh();
            	} catch(NoSuchElementException e) {
            		System.out.println("Cancel edit");
            	}
    			
            }
            
        }
        */
    }
}

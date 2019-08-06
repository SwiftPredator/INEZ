package application;
import javafx.beans.property.SimpleStringProperty;

public class Product {
	private String name;
	private String amount;
	private String label;
	private String listName;
	
    public Product(String name) {
        this.name = name;
        this.listName = name;
    }
    public Product(String name, String label, String amount) {
    	this.name = name;
    	this.label = label;
    	this.amount = amount;
    	this.listName = amount + " " + label + " " + name;
    }
    public String getName() {
        return name;
    }
    
    public void setAmount(String amount) {
    	this.amount = amount;
    }
    
    public String getAmount() {
    	return amount;
    }
    
    public void setLabek(String label) {
    	this.label = label;
    }
    
    public String getLabel() {
    	return label;
    }
    
    public String getListName() {
    	return listName;
    }
}

package application;
import javafx.beans.property.SimpleStringProperty;

/**
 * Product Model class, used to save all important data for a product
 * @author ironmonkeyapps
 *
 */
public class Product {
	private String name = "";
	private String amount = "";
	private String label = "";
	private String listName = "";
	private int prefixAmount;
	
    public Product(String name) {
        this.name = name;
    }
  
    public Product(String name, String label, String amount) {
    	this.name = name;
    	this.label = label;
    	this.amount = amount;
    	this.listName = amount + " " + label + " " + name;
    }
    
    public void setName(String name) {
    	this.name = name;
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
    
    public void setLabel(String label) {
    	this.label = label;
    }
    
    public String getLabel() {
    	return label;
    }
    
    public String getListName() {
    	return listName;
    }
    
    public void setListName(String name) {
    	this.listName = name;
    }
    
    public void setPrefixAmount(int amount) {
    	this.prefixAmount = amount;
    }
    
    public int getPrefixAmount() {
    	return this.prefixAmount;
    }
    
   @Override
   public boolean equals(Object obj) {
   	Product s = (Product) obj;
   	if(s.getName().contentEquals(this.name) && s.getAmount().contentEquals(this.amount) && s.getPrefixAmount() == this.prefixAmount) {
   		return true;
   	}
   	
   	return false;
   }
}

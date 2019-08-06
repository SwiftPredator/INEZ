package application;

import java.util.ArrayList;

public interface BrainAPI {
	public void startBrain(); 
	public ArrayList<Product> calculateSuggestions(String input);
	public Product[] fetchShoppingList();
	
}

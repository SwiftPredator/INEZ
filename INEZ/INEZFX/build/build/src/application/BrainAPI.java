package application;

import java.util.ArrayList;
/**
 * Interface for the Brain-Model
 * Access to key-fuctions of the model
 * startBrain() have to be called before any other method works
 * @author Paul Heinemeyer
 *
 */
public interface BrainAPI {
	public void startBrain(); 
	public ArrayList<Product> calculateSuggestions(String input, int retAmount);
	public String calculateBestFittingAmount(ArrayList<Product> input);
	public sortHelper calculatePrefixAmount(Product input, ArrayList<Product> checkList);
	public Product calculateProductFromString(String input);
	public ArrayList<Product> fetchSavedProducts(String sFileLoc) throws Exception;
	public void saveProducts(String data, String sFileLoc) throws Exception;
	public boolean syntaxCheckInput(String input);
	public String[] spellCheck(String word) throws Exception;
}

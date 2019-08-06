package application;
/**
 * @author Paul Heinemeyer
 * @version 1.0.0
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileSystems;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Brain implements BrainAPI {
	WebClient webClient;
	ArrayList<Product> edeka_products = new ArrayList<Product>();
	private static Gson gson = new Gson();
	
	@Override
	/**
	 * Starts the Controller of the MVC and looks for the reference products, if there is no file containing them
	 * the controller will start fetching them from the *Edeka* website.
	 * @param void
	 * @return void
	 */
	public void startBrain() {
		String pathToReferenceList = FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/products.txt";
		File file = new File(pathToReferenceList);
		if(file.exists()) {
			try {
				edeka_products = this.fetchSavedProducts(pathToReferenceList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setUpWebClient();
			try {
				System.setErr(new PrintStream("/dev/null"));
				ArrayList<DomAttr> linkList = this.getLinks();
				edeka_products = this.scratchProductInfo(linkList);
				String s = gson.toJson(edeka_products);
				this.saveProducts(s, pathToReferenceList);
			} catch (Exception e) {
				System.out.println("BrainError: "+e);
			}
			System.setErr(new PrintStream(System.err));
		}
		System.out.println(edeka_products.size());
	}
	
	@Override
	//o(n^3) better solution?
	/**
	 * Calculates the Levenstein-Distance beetween the input String splitted by ' ' 
	 * and all reference Product names splitted by ' '. With the distance the best fitting products are calculated
	 * @param String input: the word which gets checked against the reference Products 
	 * @return ArrayList that contains the best fitting products for the input String
	 */
	public ArrayList<Product> calculateSuggestions(String input) {
		String[] sourceWords = input.split(" ");
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for(Product product : edeka_products) {
			String[] targetWords = product.getName().split(" ");
			int minScore = 99999;
			for(String s_Word : sourceWords) {
				for(String t_Word : targetWords) {
					int leveScore = evaluateLevenshteinDistance(s_Word, t_Word);
					if(leveScore < minScore) {
						minScore = leveScore;
					}
				}
			scores.add(minScore);
			}
			
		}
		ArrayList<Product> suggestions = new ArrayList<Product>();
		/* Get best fitting products */
		for(int i = 0; i < 6; i++) {
			int curr_minScore = Collections.min(scores);
			int curr_index = scores.indexOf(curr_minScore);
			suggestions.add(edeka_products.get(curr_index + i));
			scores.remove(curr_index);
			
		}
		//System.out.println(Collections.min(scores));
		//System.out.println(scores.indexOf(2));
		//System.out.println(edeka_products.get(1380).getName());
		//System.out.println(sorted.toArray());
		System.out.println(suggestions.get(0).getName());
		//System.out.println(edeka_products.get(0));
		return suggestions;
	}
	
    @Override
    public Product[] fetchShoppingList() {
    	return null;
    }
    /**
     * setups the webclient(HtmlUnit) for scratching data
     * @param void
     * @return void
     */
    private void setUpWebClient() {
    	//SETUP WEBCLIENT
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setTimeout(10000);
        webClient.getOptions().setUseInsecureSSL(true);
    }
	/**
	 * Method goes to the edeka-product website and scratches all the links to the product-info sites for later use.
	 * It iterates throught all pages and fetches the href fields with xPath @see edeka_XPATH_CONS.java
	 * @param void 
	 * @return ArrayList containing the DomAttributes of the href´s from the *edeka* website.
	 * @throws IOException
	 */
	private ArrayList<DomAttr> getLinks() throws IOException {
		//CATCH LINKS TO PRODUCTS
        final HtmlPage page = webClient.getPage("http://www.edeka.de/unsere-marken/suche-nach-edeka-produkten/suche-nach-edeka-produkten/sortimentkategorien.jsp");
        ArrayList<DomAttr> links = new ArrayList<DomAttr>();
        HtmlAnchor getAllProducts_Button = page.getFirstByXPath(edeka_XPATH_CONS.SHOWALLPRODSBUTTON.toString());
        System.out.println(getAllProducts_Button.asXml());
        try {
     	   HtmlPage sub = getAllProducts_Button.click();
     	   webClient.waitForBackgroundJavaScript(1000);
     	   DomText numProducts = sub.getFirstByXPath(edeka_XPATH_CONS.NUMPRODUCTS.toString());
     	   System.out.println(numProducts);
     	   HtmlAnchor nextButton = sub.getFirstByXPath(edeka_XPATH_CONS.NEXTBUTTON.toString());
     	   String[] split = nextButton.getAttribute("style").split(":");
    
     	   while(!split[1].contentEquals("hidden")) {
     		   log("fetch product cycle");
     		   links.addAll(sub.getByXPath("//a[@class='title']/@href"));
     		   sub = nextButton.click();
     		   webClient.waitForBackgroundJavaScript(1000);
     		   nextButton = sub.getFirstByXPath(edeka_XPATH_CONS.NEXTBUTTON.toString());
     		   split = nextButton.getAttribute("style").split(" ");
     		   split[1] = split[1].replace(" ", "").replace(";", "");
     		   System.out.println(links.size());
     		   
     	   }
     	   System.out.println(links);
     	   return links;
        } catch(IOException e) {
     	   throw e;
        }
        	
        
	}
	/**
	 * Scratches the product-info sites from the Link-List and gets the product name, label and amount. 
	 * It creates a new Product @see Product and saves it in a ArrayList.
	 * @param links to the product-info sites scratched from @see getLinks()
	 * @return ArrayList with the scratched Products
	 * @throws Exception
	 */
	private ArrayList<Product> scratchProductInfo(ArrayList<DomAttr> links) throws Exception {
		//FETCH PRODUCTS (O(n)) + fetch time 
 	   HtmlPage productPage;
 	   ArrayList<Product> products = new ArrayList<Product>();
 	   try {
 		   int i = 0;
	 	   for(DomAttr link : links) {
	 		   System.out.print(i+"\n"); i++;
	 		   String temp_link = link.getValue();
	 		   productPage = webClient.getPage("http://www.edeka.de"+temp_link);
	 		   DomText name = productPage.getFirstByXPath("//span/span[@itemprop='name']/text()");
	 		   String strName = "";
	 		   if(name != null) {
	 			  strName = name.toString();
	 		   }
	 		   DomText label = productPage.getFirstByXPath("//span/span[@itemprop='brand']/text()");
	 		   String strLabel = "";
	 		   if(label != null) {
	 			   strLabel = label.toString();
	 		   }
	 		   String amount = productPage.getFirstByXPath("//p/b/text()").toString();
	 		   System.out.println(name);
	 		   log(strLabel);
	 		   log(amount);
	 		   products.add(new Product(strName, strLabel, amount));
	 	   }
	 	   return products;
 	   } catch(Exception e) {
 		   throw e;
 	   }
	}
	/**
	 * 
	 * @param data
	 * @param sFileLoc
	 * @throws Exception
	 */
	private void saveProducts(String data, String sFileLoc) throws Exception {
		File file = new File(sFileLoc);
		//System.out.println("Data" + data);
		if (!file.exists()) {
			try {
				File directory = new File(file.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
				file.createNewFile();
			} catch (IOException e) {
				throw e;
			}
		}
 
		try {
			// Convenience class for writing character files
			FileWriter crunchifyWriter;
			crunchifyWriter = new FileWriter(file.getAbsoluteFile(), false);
 
			// Writes text to a character-output stream
			BufferedWriter bufferWriter = new BufferedWriter(crunchifyWriter);
			bufferWriter.write(data);
			bufferWriter.close();
 
			log("Company data saved at file location: " + sFileLoc + " Data: " + "\n");
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 
	 * @param sFileLoc
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Product> fetchSavedProducts(String sFileLoc) throws Exception {
		File file = new File(sFileLoc);
		if (!file.exists())
			log("File doesn't exist");
 
		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
 
			JsonReader myReader = new JsonReader(isReader);
			ArrayList<Product> productList= (ArrayList<Product>) gson.fromJson(myReader,
                    new TypeToken<ArrayList<Product>>() {}.getType());
			log("\nProduct Data loaded successfully from file " + sFileLoc);
			return productList;
 
		} catch (Exception e) {
			throw e;
		}
 
		
 
	}
	/**
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private int evaluateLevenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	                                                                                    
	                                                         
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	                                    
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	                                     
	                                                                                    
	                                    
	    for (int j = 1; j < len1; j++) {                                                
	                                    
	        newcost[0] = j;                                                             
	                                                                                    
	                                        
	        for(int i = 1; i < len0; i++) {                                             
	                                       
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             
	                                                                                    
	                                           
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	                                                              
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	    return cost[len0 - 1];                                                          
	}
 
	private static void log(String string) {
		System.out.println(string);
	}
	
	

}
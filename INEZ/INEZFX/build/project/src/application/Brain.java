package application;
/**
 * @author Paul Heinemeyer
 * @version 1.0.0
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.spell.JaroWinklerDistance;



/**
 * Model for the Controller and the View. 
 * Access over the Brain API Interface. 
 * @author Paul Heinemeyer
 *
 */
public class Brain implements BrainAPI {
	WebClient webClient;
	ArrayList<Product> edeka_products = new ArrayList<Product>();
	private static Gson gson = new Gson();
	
	@Override
	/**
	 * Starts the Controller of the MVC and looks for the reference products, if there is no file containing them
	 * the model will start fetching them from the *Edeka* website. This can take some time.
	 * @param void
	 * @return void
	 */
	public void startBrain() {
		String pathToReferenceList = FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/products.txt";
		File file = new File(pathToReferenceList);
		if(file.exists()) {
			try {
				//tag("Fettarme H-Milch");
				edeka_products = this.fetchSavedProducts(pathToReferenceList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setUpWebClient();
			try {
				System.setErr(new PrintStream("/dev/null"));
				ArrayList<DomAttr> linkList = this.getLinks();
				edeka_products = this.scratchProductInfo(linkList);
				System.out.println(linkList.size()+" : "+edeka_products.size());
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
	/**
	 * Calculates the Levenshtein-Distance beetween the input String splitted by ' ' and all reference Product names splitted by ' '. 
	 * It also uses a machine learning model to determine nouns in product names. It looks which is the 'main' word in a Product name. 
	 * With this info the function will evaluate a score for each product in the reference list and returns the products with the lowest score.
	 * @param String input: the word which gets checked against the reference Products 
	 * @param int retAmount: the amount of items which get returned in the ArrayList
	 * @return ArrayList that contains the best fitting products for the input String
	 */
	public ArrayList<Product> calculateSuggestions(String input, int retAmount) {
		ArrayList<String> sourceWordList = new ArrayList<String>(Arrays.asList(input.split(" ")));
		//If more than one word in Term then filter the nouns and only check them
		if(sourceWordList.size() > 1) {
			for(int i = 0; i < sourceWordList.size(); i++) {
				try {
					String[] tags = tag(sourceWordList.get(i));
					for(int j = 0; j < tags.length; j++) {
						if(!tags[j].contentEquals("NN")) {
							sourceWordList.remove(i);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		ArrayList<sortHelper> scores = new ArrayList<sortHelper>();
		int index = 0;
		//evaluate levenshtein scores
		for(Product product : edeka_products) {
			String[] targetWords = product.getName().split(" ");
			int minScore = 99999;
			for(String s_Word : sourceWordList) {
				for(String t_Word : targetWords) {
					int leveScore = 999999;
					leveScore = evaluateLevenshteinDistance(s_Word, t_Word);
					if(t_Word.contains(s_Word)) {
						leveScore -= 1;
					} 
					if(leveScore < minScore) {
						minScore = leveScore;
					}
				}
			}
			scores.add(new sortHelper(index, minScore));
			index++;
			
		}
		
		ArrayList<Product> suggestions = new ArrayList<Product>();
		Collections.sort(scores);
		
		/* Look how many nouns are in a product name and adjust score */
		ArrayList<sortHelper> newScores = new ArrayList<sortHelper>();
		for(int i = 0; i < scores.size(); i++) {
			if(scores.get(i).value < 2) {
				try {
					String[] tags = tag(edeka_products.get(scores.get(i).index).getName());
					for(int j = 0; j < tags.length; j++) {
						/* NN = Noun APPR = Preposition */
						/* every noun increases the score, because as example if someone searches for 'Milch' the product 'VollMilch' should be more important than ' Cremedusche Honig & Milch' */
						if(tags[j].contentEquals("NN")  /*|| tags[j].contentEquals("NE")*/) {
							log("score rised");
							scores.get(i).value += 3;
						}
						/* if APPR, one noun is like a verb and describes another noun. So it doesent affect the score*/
						else if(tags[j].contentEquals("APPR")) {
							log("score lowered");
							scores.get(i).value -= 3;
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				newScores.add(new sortHelper(scores.get(i).index, scores.get(i).value));
			} 
		}
		System.out.println(newScores.size());
		Collections.sort(newScores);
		//Gets the best retamount products from the list
		for(int i = 0; i < newScores.size(); i++) {
			if(i == retAmount - 1) {break;}
			System.out.print(newScores.get(i).value + " : ");
			Product temp_prod = edeka_products.get(newScores.get(i).index);
			System.out.println(temp_prod.getName()); 
			suggestions.add(temp_prod);
		}
		
		return suggestions;
	}
	
	@Override
	/**
	 * Checks the param input Product-List for equal amounts and returns the most common. 
	 * If all are the same rarity it returns the first amount from the param input list
	 * @param ArrayList<Product> input 
	 * @return String: the calculated amount for the given product-list
	 */
	public String calculateBestFittingAmount(ArrayList<Product> input) {
		ArrayList<sortHelper> helper = new ArrayList<sortHelper>();
		for(int i = 0; i < input.size(); i++ ) {
			boolean foundInHelper = false;
			String temp_input_amount = input.get(i).getAmount();
			for(int j = 0; j < helper.size(); j++) {
				String temp_helper_amount = input.get(helper.get(j).index).getAmount();
				if(temp_input_amount.contentEquals(temp_helper_amount)) {
					helper.get(j).value++;
					foundInHelper = true;
					break;
				}
			}
			//Check if we doesent found a equal one
			if(!foundInHelper) {
				helper.add(new sortHelper(i, 1));
			}
		}
		//Sort List and look if we got a highest match
		Collections.sort(helper, Collections.reverseOrder());
		if(helper.get(0).value != 1) {
			return input.get(helper.get(0).index).getAmount();
		}
		return input.get(0).getAmount();
	}
	
	@Override
	/**
	 * calculates the prefix amount z.B '1x' or '2x' if a product with the same name exists in the user list
	 * pattern to work is like '1x 100l Milch'. The Prefix amount has to be on the front.
	 * the method returns a tuple of two values. The index determines if we found an equal product in the list [-1 = no, >=0 = index of the item in the list] 
	 * and the value is the new prefix amount
	 * @param input
	 * @param checkList
	 * @return
	 */
	public sortHelper calculatePrefixAmount(Product input, ArrayList<Product> checkList) {
		sortHelper result = new sortHelper(-1, input.getPrefixAmount());
		
		//check if same product is on the list 
		for(Product p : checkList) {
			if(input.getName().contentEquals(p.getName())) {
				result.index = checkList.indexOf(p);
				result.value += p.getPrefixAmount();	
				break;
			}
		}
		
		return result;
	}
	
	@Override
	/**
	 * Extracts prefix amount and amount from input String and returns a product object
	 * @param 
	 * @return Product
	 */
	public Product calculateProductFromString(String input) {
		Product result = new Product("");
		String temp_input = input;
		result.setPrefixAmount(1);
		//Check for prefix Amount 
		int x_index = temp_input.indexOf("x");
		if(x_index != -1) {
			String pre_amount = temp_input.substring(0, x_index);
			log(pre_amount);
			//check if string before x is a number
			if(pre_amount.matches("([1-9]\\d*)")) {
				result.setPrefixAmount(Integer.parseInt(pre_amount));
				log("REPLACE");
				temp_input = temp_input.replace(pre_amount + "x" ,"");
			}
		}
		
		//check for amount defined by number 
		Matcher matcher = Pattern.compile("\\d+(\\p{Punct}\\d*)?(\\p{Blank}|\\p{Punct})?([a-z]|[A-Z])*").matcher(temp_input);
		if(matcher.find()) {
			String amount = matcher.group();
			temp_input = temp_input.replace(amount , "");
			System.out.println("Amount: " + amount);
			System.out.println(result.getPrefixAmount());
			result.setAmount(amount);
		}
		temp_input = temp_input.trim();
		System.out.println("Name: " + temp_input);
		result.setName(temp_input);
		
		
		return result;
	}
	
	@Override
	/**
	 * Checks if the input Product matches the required pattern. 
	 * Valid Patterns are: '1x 1l Milch', '1x Milch', 'Milch', 'Voll Milch'
	 * So its like [prefix-amount | amount | name], where prefix-amount and amount are optional
	 *@param String input
	 *@return 
	 */
	public boolean syntaxCheckInput(String input) {
		if(input.matches("(\\d+x)?(\\s)?(\\d+(\\p{Punct}\\d*)?(\\p{Blank}|\\p{Punct})?([a-z]|[A-Z])*)?\\D+")) {
			return true;
		}
		return false;
	}
	
	@Override
	/**
	 * checks a given word against a big list of german words for auto correctur
	 * @param String word: the input 
	 * @return String[]: the suggestions
	 */
	public String[] spellCheck(String word) throws Exception {
		try {
		Directory spellIndexDir = FSDirectory.open(Paths.get(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/spellchecker"));
		SpellChecker spellchecker = new SpellChecker(spellIndexDir);
		  // To index a file containing words:
		IndexWriterConfig config = new IndexWriterConfig(null);
		spellchecker.indexDictionary(new PlainTextDictionary(Paths.get(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()+"/German_de_DE.dic")), config, false);
		spellchecker.setStringDistance(new JaroWinklerDistance());
		String[] suggestions = spellchecker.suggestSimilar(word, 7);
		//Remove strange tags from the word-dic
		for(int i = 0; i < suggestions.length; i++) {
			String s = suggestions[i];
			int x_index = s.indexOf("/");
			if(x_index != -1) {
				String dirErrorSymbols = s.substring(x_index, s.length());
				s = s.replace(dirErrorSymbols, "");
				suggestions[i] = s;
			}
		}
		return suggestions;
		} catch(Exception e) {
			log(e.getMessage());
			throw e;
		}
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
     	   webClient.waitForBackgroundJavaScript(4000);
     	   DomText numProducts = sub.getFirstByXPath(edeka_XPATH_CONS.NUMPRODUCTS.toString());
     	   System.out.println(numProducts);
     	   HtmlAnchor nextButton = sub.getFirstByXPath(edeka_XPATH_CONS.NEXTBUTTON.toString());
     	   String[] split = nextButton.getAttribute("style").split(":");
    
     	   while(!split[1].contentEquals("hidden")) {
     		   log("fetch product cycle");
     		   links.addAll(sub.getByXPath("//a[@class='title']/@href"));
     		   sub = nextButton.click();
     		   webClient.waitForBackgroundJavaScript(4000);
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
 		   ArrayList<String> finishedLinks = new ArrayList<String>();
	 	   for(DomAttr link : links) {
	 		   if(finishedLinks.contains(link.getValue())) {log("LINK REMOVED"); continue;}
	 		   finishedLinks.add(link.getValue());
	 		   System.out.print(i+"\n"); i++;
	 		   String temp_link = link.getValue();
	 		   productPage = webClient.getPage("http://www.edeka.de"+temp_link);
	 		   //while(webClient.waitForBackgroundJavaScript(500) != 0) {}
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
	 		   DomText amount = productPage.getFirstByXPath("//p/b/text()");
	 		   String strAmount = "";
	 		   if(amount != null) {
	 			   strAmount = amount.toString();
	 		   }
	 		   System.out.println(name);
	 		   log(strLabel);
	 		   log(strAmount);
	 		   products.add(new Product(strName, strLabel, strAmount));
	 	   }
	 	   return products;
 	   } catch(Exception e) {
 		   throw e;
 	   }
	}
	
	@Override
	/**
	 * Can save a json string of data. In this use-case a list of products to a txt-file. 
	 * It saves the products to the given file-name or creates a new file.
	 * @param data
	 * @param sFileLoc
	 * @throws Exception
	 */
	public void saveProducts(String data, String sFileLoc) throws Exception {
		File file = new File(sFileLoc);
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
			FileWriter crunchifyWriter;
			crunchifyWriter = new FileWriter(file.getAbsoluteFile(), false);
			
			BufferedWriter bufferWriter = new BufferedWriter(crunchifyWriter);
			bufferWriter.write(data);
			bufferWriter.close();
 
			log("Data saved at file location: " + sFileLoc + " Data: " + "\n");
		} catch (IOException e) {
			throw e;
		}
	}
	
	@Override
	/**
	 * Gets products from a given file. The file has to contain the products in json-format.
	 * @param sFileLoc
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Product> fetchSavedProducts(String sFileLoc) throws Exception {
		File file = new File(sFileLoc);
		if (!file.exists()) {
			log("File doesn't exist");
			return null;
		}
 
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
	 * Evaluates the levenshtein distance between to given Strings
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private int evaluateLevenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
		int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
        int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                                                  
	    for (int i = 0; i < len0; i++) {
	    	cost[i] = i;                                     
	    }
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
 
	
	
	POSTaggerME tagger = null;
    POSModel model = null;
    
	/**
	 * initialize the machine learning model for grammatical meaning interpretation
	 * @param lexiconFileName : file path to the model
	 */
	private void initialize(String lexiconFileName) {
        try {
            InputStream modelStream =  getClass().getResourceAsStream(lexiconFileName);
            model = new POSModel(modelStream);
            tagger = new POSTaggerME(model);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
	
	/**
	 * evaluates a given input string and determines its grammatical meaning using the perceptron model
	 * @param text
	 */
	private String[] tag(String text) throws Exception{
        initialize("/de-pos-perceptron.bin");
        try {
            if (model != null) {
                POSTaggerME tagger = new POSTaggerME(model);
                if (tagger != null) {
                    ArrayList<String> temp_whitespaceTokenizerLine = new ArrayList<String>(Arrays.asList(WhitespaceTokenizer.INSTANCE
                            .tokenize(text)));
                    /* for(int i = 0; i < temp_whitespaceTokenizerLine.size(); i++) {
                    	 String token = temp_whitespaceTokenizerLine.get(i);
                    	 if(checkIfOnlyUppercase(token)) {
                    		 temp_whitespaceTokenizerLine.remove(token);
                    	 }
                     }  */
                    String whitespaceTokenizerLine[] = new String[temp_whitespaceTokenizerLine.size()];
                    whitespaceTokenizerLine = temp_whitespaceTokenizerLine.toArray(whitespaceTokenizerLine);
                    String[] tags = tagger.tag(whitespaceTokenizerLine);
                    for (int i = 0; i < whitespaceTokenizerLine.length; i++) {
                        String word = whitespaceTokenizerLine[i].trim();
                        String tag = tags[i].trim();
                        System.out.print(tag + ":" + word + "  ");
                    }
                    return tags;
                }
            }
            return null;
        } catch (Exception e) {
           throw e;
        }
    }
	/**
	 * checks if a given String has only upper-cased letters
	 * @param str
	 * @return
	 */
	private boolean checkIfOnlyUppercase(String str) {
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c >= 97 && c <= 122) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper Method to print Strings 
	 * @param string
	 */
	private static void log(String string) {
		System.out.println(string);
	}

}

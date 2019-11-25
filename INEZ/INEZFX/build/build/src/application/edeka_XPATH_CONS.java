package application;

/**
 * xPath Constants for the product fetching from the 'edeka' product site
 * @author Paul Heinemeyer
 *
 */
public enum edeka_XPATH_CONS {
	SHOWALLPRODSBUTTON {
		 public String toString() {
	          return "//a[@class='button']";
	      }
	},
	NUMPRODUCTS {
		public String toString() {
	          return "//div[@class='numResults']/span/text()";
	      }
	},
	NEXTBUTTON {
		public String toString() {
	          return "//a[@class='nextpage']";
	      }
	};
	
}

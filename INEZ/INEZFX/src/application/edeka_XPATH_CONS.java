package application;

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

package application;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrainTest {
	BrainAPI tester = new Brain();
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	
	@Test
	void testCalculateBestFittingAmount() {
		ArrayList<Product> testList = new ArrayList<Product>();
		testList.add(new Product("a", "b", "2L"));
		testList.add(new Product("a", "c", "1L"));
		testList.add(new Product("a", "b", "1L"));
		assertEquals("1L", tester.calculateBestFittingAmount(testList));
		testList.remove(testList.size() - 1);
		assertEquals("2L", tester.calculateBestFittingAmount(testList));
		
	}
	
	@Test
	void testCalculatePrefixAmount() {
		ArrayList<Product> testList = new ArrayList<Product>();
		testList.add(new Product("a", "b", "2L"));
		testList.add(new Product("c", "c", "1L"));
		testList.add(new Product("b", "b", "1L"));
		testList.get(0).setPrefixAmount(2);
		testList.get(1).setPrefixAmount(1);
		testList.get(2).setPrefixAmount(1);
		Product testerP = new Product("b");
		testerP.setPrefixAmount(2);
		assertEquals(new sortHelper(2, 3), tester.calculatePrefixAmount(testerP, testList));
		testerP.setName("a");
		assertEquals(new sortHelper(0, 4), tester.calculatePrefixAmount(testerP, testList));
		testerP.setName("d");
		assertEquals(new sortHelper(-1, 2), tester.calculatePrefixAmount(testerP, testList));
	}
	
	@Test 
	void testCalculateProductFromString() {
		Product p = new Product("a", "b", "2L");
		p.setPrefixAmount(2);
		assertEquals(p, tester.calculateProductFromString("2x 2L a"));
		Product p2 = new Product("a");
		p2.setPrefixAmount(1);
		assertEquals(p2, tester.calculateProductFromString("a"));
		p2.setAmount("3L");
		assertEquals(p2, tester.calculateProductFromString("3L a"));
	}
	
	@Test
	void testSyntaxCheckInput() {
		assertEquals(true, tester.syntaxCheckInput("Milch"));
		assertEquals(true, tester.syntaxCheckInput("2x 100l Milch"));
		assertEquals(true, tester.syntaxCheckInput("2x Milch"));
		assertEquals(false, tester.syntaxCheckInput("3823293 39238 Milch"));
		assertEquals(true, tester.syntaxCheckInput("3823293x 39238 Milxch"));
		assertEquals(true, tester.syntaxCheckInput("38x Voll Milch"));
		assertEquals(true, tester.syntaxCheckInput("38x 100l Voll Milch"));
		assertEquals(false, tester.syntaxCheckInput("3823293"));
		assertEquals(false, tester.syntaxCheckInput("3823293x 39238 Milxch 445050"));
		
		try {
			assertEquals("Milch", tester.spellCheck("Milsch"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	@Test
	void testSaveProducts() {
		fail("Not testable");
	}

	@Test
	void testFetchSavedProducts() {
		fail("Not testable");
	}
	
	@Test
	void testStartBrain() {
		fail("Not testable");
	}

	@Test
	void testCalculateSuggestions() {
		fail("Not testable");
	}

}

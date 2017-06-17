package CarReviewer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class API {

	static int maxOutput = 5; //default is 5
	static HashMap<String, Double> carScores = new HashMap<String, Double>(); // Hold scores for overall category Sentiment
	static List<String> chosenCategories = new ArrayList<String>(); // List of selected categories from parameters
	static ArrayList<List<String>> arrayCategories = new ArrayList<List<String>>(); // List of all (sub)categories
	static List<String> keys = new ArrayList<String>(); // List of car names

	//TODO: Make extended parameter to show all categories
	//TODO: If car has less than x features, do not score
	//TODO: Use pre-annotated data
	public static void main(String[] args) {
		List<String> cars = new ArrayList<String>();
		// Get categories
		arrayCategories = Categories.makeCategories();

		//get keys
		keys = (List<String>) SimpleRunner.getKeys();

		// Loop over arguments to find parameters of cars, categories, and maximum output
		for(String option : args) {
			for (List<String> category : arrayCategories) { 
				for(String subcategory : category)
					if(subcategory.equals(option))
						chosenCategories.add(option);
			}
			if(keys.contains(option.toLowerCase()) || SimpleRunner.trimKeys(keys).contains(option))
				cars.add(option);
			if(isNumeric(option)) {
				if(Integer.parseInt(option) <= 10 && Integer.parseInt(option) >= 0)
					maxOutput = Integer.parseInt(option);
			}
		}
		
		Boolean noOptionSelected = true; // If neither gui or api is specified
		for(String option : args) {
			switch(option.toLowerCase()) {
			case "gui":
				noOptionSelected = false;
				new GUI(chosenCategories);
				break;
			case "api":
				noOptionSelected = false;
				new API(chosenCategories, cars);
				break;
			}
		}
		
		// Run gui by default
		if(noOptionSelected) {
			new GUI(chosenCategories);
		}
	}

	public API(List<String> chosenCategories, List<String> cars) {
		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		HashMap<String, List<Pattern>> allFeatures = new HashMap<String, List<Pattern>>();
		SimpleRunner runner = new SimpleRunner();

		// Sort cars A-Z
		java.util.Collections.sort(keys);

		System.out.println("Categories interested in: " + chosenCategories);
		System.out.println("Cars interested in : " + cars);

		// Prevent CoreNLP from outputting red text with annotated stuff
		PrintStream err = System.err;
		// now make all writes to the System.err stream silent 
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));

		// Setup CoreNLP pipeline
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// If no cars in arguments
		if (cars.size() == 0){
			for (int i=0; i<keys.size(); i++) {
				patterns = (ArrayList<Pattern>) runner
						.run(SimpleRunner.unTrimKey((String) keys.get(i)), pipeline);
				// If patterns have been found, add to the list of all features
				if (patterns.size() != 0) {
					allFeatures.put(keys.get(i), patterns);
				}
			}
		}

		// If cars specified
		else {
			for(String car : cars) {
				patterns = (ArrayList<Pattern>) runner
						.run(SimpleRunner.unTrimKey(car), pipeline);
				allFeatures.put(car, patterns);
			}
		}
		
		// reset error output
		System.setErr(err);
		
		// Show relevant features
		show(allFeatures);
	}

	private static void calculateCarScore(HashMap<String, List<Pattern>> allFeatures) {
		Iterator<Map.Entry<String, List<Pattern>>> it = allFeatures.entrySet().iterator();
		
		// Loop over the list of all cars
		while(it.hasNext()) {
			Map.Entry<String, List<Pattern>> pair = (Map.Entry<String, List<Pattern>>)it.next();
			
			// Group similar categories together
			ArrayList<List<String>> arrayCategories = Categories.groupCategories(pair.getValue());
			double overallCategorySentiment = 0;
			
			// Calculate overall sentiment
			for (List<String> category : arrayCategories) {
				double categorySentiment = 0;
				for(int t=1;t<category.size();t++) { // First element in the list is category name
					categorySentiment += (Double.parseDouble(category.get(t)));
				}
				overallCategorySentiment += categorySentiment/(category.size()-1);
			}
			
			// Put overall sentiment in a HashMap with car name as key
			carScores.put(pair.getKey(), overallCategorySentiment/arrayCategories.size());

		}
	}

	private void show(HashMap<String, List<Pattern>> allFeatures) {
		arrayCategories = Categories.makeCategories(); // Get list of all categories
		
		// Assign categories to features
		Iterator<Map.Entry<String, List<Pattern>>> featureIterator = allFeatures.entrySet().iterator();
		while(featureIterator.hasNext()) {
			new Categories(featureIterator.next().getValue(), arrayCategories);
		}

		// Refine features based on chosen categories
		if(chosenCategories.size() != 0) {
			//1. Make list of custom categories based on arguments of categories with a capital letters
			//For example Performance, Design, Feeling, but not performance, design, feeling, since these are subcategories
			ArrayList<List<String>> customCategories = Categories.makeCategories();
			Iterator<List<String>> customIterator = customCategories.iterator();
			while(customIterator.hasNext()) {
				List<String> customCategory = customIterator.next();
				if(!chosenCategories.contains(customCategory.get(0))) 
					customIterator.remove();
			}
			
			//2. Check if pattern categories are in the list of chosen categories
			Iterator<Map.Entry<String, List<Pattern>>> it = allFeatures.entrySet().iterator();
			
			// Loop over all cars with their features
			while(it.hasNext()) {
				List<Pattern> patterns = it.next().getValue();
				Iterator<Pattern> patternIterator = patterns.iterator();
				while(patternIterator.hasNext()) {
					Pattern pattern = patternIterator.next();
					Boolean found = false;

					for(String chosenCategory : chosenCategories) {
						// Check if selected category is a top-level category
						if(chosenCategory.substring(0, 1).equals(chosenCategory.substring(0, 1).toUpperCase()))
							// Loop over its subcategories and seek for match with pattern
							for(List<String>customCategory : customCategories) {
								if(customCategory.contains(pattern.category)) { 
									found=true;
								}
							}
						// Selected category is a subcategory
						else {
							if(chosenCategory.equals(pattern.head)|| chosenCategory.equals(pattern.modifier))
								found=true;
						}
					}
					if(!found) // If pattern category does not meet selected category, remove from the list
						patternIterator.remove();
				}
				
				// If the car has no features left, remove the car from the results
				if(patterns.size() == 0) {
					it.remove();
				}
			}
		}

		calculateCarScore(allFeatures);
		
		// Sort the HashMap of scores on highest values
		LinkedHashMap<String, Double> allResults = (LinkedHashMap<String, Double>) SimpleRunner.sortByValue(carScores);

		// Limit results to the maximum output (5 if not specified)
		Iterator<Map.Entry<String, Double>> iterator = allResults.entrySet().iterator();
		int count = 0;
		LinkedHashMap<String, Double> results = new LinkedHashMap<String, Double>();
		while(iterator.hasNext() && count<maxOutput) {
			Map.Entry<String, Double> pair = (Map.Entry<String, Double>) iterator.next();
			results.put(pair.getKey(), pair.getValue());
			count++;
		}

		// Print final results for each car
		for (String car : results.keySet()) {
			List<Pattern> patterns = allFeatures.get(car);
			
			// Print pretty car name
			System.out.println(SimpleRunner.trimKey(car));
			
			// Print pretty car name to GUI if it exists
			GUI.APIReturn(SimpleRunner.trimKey(car));
			
			// Print category results
			printResults(patterns);
			
			// Print average score
			System.out.println("Average: " + results.get(car) + "\n");
			
			// Print average score to GUI if it exists
			GUI.APIReturn("Average: " + results.get(car) + "\n");
		}
	}

	private void printResults(List<Pattern> patterns) {
		ArrayList<List<String>> arrayCategories = Categories.groupCategories(patterns);

		for (List<String> category : arrayCategories) {
			double categorySentiment = 0;
			for(int i=1;i<category.size();i++) {
				categorySentiment += (Double.parseDouble(category.get(i)));
			}
			
			System.out.println(category.get(0) + ": " + new DecimalFormat("##.##").format(categorySentiment/(category.size()-1)));
			GUI.APIReturn(category.get(0) + ": " + new DecimalFormat("##.##").format(categorySentiment/(category.size()-1)));
		}

	}

	// Method to check if argument is a number
	private static boolean isNumeric(String str)  {  
		try  {  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  {  
			return false;  
		}  
		return true;  
	}
}

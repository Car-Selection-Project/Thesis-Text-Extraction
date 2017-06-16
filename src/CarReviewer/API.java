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
	static List<String> chosenCategories = new ArrayList<String>();
	static ArrayList<List<String>> arrayCategories = new ArrayList<List<String>>();
	static List<String> keys = new ArrayList<String>();

	//TODO: Make extended parameter to show all categories
	//TODO: If car has less than x features, do not score
	//TODO: Use pre-annotated data
	public static void main(String[] args) {
		List<String> cars = new ArrayList<String>();
		// Get categories
		arrayCategories = Categories.makeCategories();

		//get keys
		keys = (List<String>) SimpleRunner.getKeys();

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
		Boolean noOptionSelected = true;
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
		if(noOptionSelected) {
			new GUI(chosenCategories);
		}
	}

	public API(List<String> chosenCategories, List<String> cars) {
		// Get keys
		java.util.Collections.sort(keys);

		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		System.out.println("Categories interested in: " + chosenCategories);
		System.out.println("Cars interested in : " + cars);

		HashMap<String, List<Pattern>> allFeatures = new HashMap<String, List<Pattern>>();

		PrintStream err = System.err;
		// now make all writes to the System.err stream silent 
		System.setErr(new PrintStream(new OutputStream() {
		    public void write(int b) {
		    }
		}));
		
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		SimpleRunner runner = new SimpleRunner();
		 printTime("Start");
		// If no cars in arguments
		if (cars.size() == 0){
			for (int i=0; i<keys.size(); i++) {
				patterns = (ArrayList<Pattern>) runner
						.run(SimpleRunner.unTrimKey((String) keys.get(i)), pipeline);

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
		System.setErr(err);
		System.out.println("Time to get patterns:");
		printTime("Stop");
		printTime("Start");
		show(allFeatures);
		System.out.println("Time to do other stuff:");
		printTime("Stop");
	}

	private static void calculateCarScore(HashMap<String, List<Pattern>> allFeatures) {
		Iterator<Map.Entry<String, List<Pattern>>> it = allFeatures.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, List<Pattern>> pair = (Map.Entry<String, List<Pattern>>)it.next();
			ArrayList<List<String>> arrayCategories = Categories.groupCategories(pair.getValue());
			//new Categories(pair.getValue(), arrayCategories);
			double overallCategorySentiment = 0;
			for (List<String> category : arrayCategories) {
				double categorySentiment = 0;
				for(int t=1;t<category.size();t++) {
					categorySentiment += (Double.parseDouble(category.get(t)));
				}
				overallCategorySentiment += categorySentiment/(category.size()-1);
				//System.out.println(overallCategorySentiment);
			}
			carScores.put(pair.getKey(), overallCategorySentiment/arrayCategories.size());
			//System.out.println("Car scores:  " + carScores);

		}
	}

	private void show(HashMap<String, List<Pattern>> allFeatures) {
		arrayCategories = Categories.makeCategories();
		//System.out.println(arrayCategories);
		// Assign categories
		Iterator<Map.Entry<String, List<Pattern>>> featureIterator = allFeatures.entrySet().iterator();
		while(featureIterator.hasNext()) {
			new Categories(featureIterator.next().getValue(), arrayCategories);
		}
		
		// Refine features based on chosen categories
		if(chosenCategories.size() != 0) {
			//1. Make custom categories
			ArrayList<List<String>> customCategories = Categories.makeCategories();
			Iterator<List<String>> customIterator = customCategories.iterator();
			while(customIterator.hasNext()) {
				List<String> customCategory = customIterator.next();
				//System.out.println(customCategory.get(0));
				if(!chosenCategories.contains(customCategory.get(0))) 
					customIterator.remove();
			}
			//2. Check if pattern categories are in the list of chosen categories
			//System.out.println(customCategories);
			Iterator<Map.Entry<String, List<Pattern>>> it = allFeatures.entrySet().iterator();
			while(it.hasNext()) {
				List<Pattern> patterns = it.next().getValue();
				//System.out.println(arrayCategories);
				Iterator<Pattern> patternIterator = patterns.iterator();
				while(patternIterator.hasNext()) {
					Pattern pattern = patternIterator.next();
					//System.out.println(pattern.category);
					Boolean found = false;

					for(String chosenCategory : chosenCategories) {
						if(chosenCategory.substring(0, 1).equals(chosenCategory.substring(0, 1).toUpperCase()))
							for(List<String>customCategory : customCategories) {
								//System.out.println(pattern.head + " " + pattern.modifier);
								if(customCategory.contains(pattern.category)) { 
									found=true;
								}
							}
						else {
							if(chosenCategory.equals(pattern.head)|| chosenCategory.equals(pattern.modifier))
								found=true;
						}
					}
					if(!found)
						patternIterator.remove();
				}
				if(patterns.size() == 0) {
					it.remove();
				}
			}
		}
		//System.out.println(allFeatures);

		calculateCarScore(allFeatures);
		LinkedHashMap<String, Double> allResults = (LinkedHashMap<String, Double>) SimpleRunner.sortByValue(carScores);

		Iterator<Map.Entry<String, Double>> iterator = allResults.entrySet().iterator();
		int count = 0;
		LinkedHashMap<String, Double> results = new LinkedHashMap<String, Double>();
		while(iterator.hasNext() && count<maxOutput) {
			Map.Entry<String, Double> pair = (Map.Entry<String, Double>) iterator.next();
			results.put(pair.getKey(), pair.getValue());
			count++;
		}

		// Print final results
		for (String car : results.keySet()) {
			List<Pattern> patterns = allFeatures.get(car);
			System.out.println(SimpleRunner.trimKey(car));
			GUI.APIReturn(SimpleRunner.trimKey(car));
			printResults(patterns);
			System.out.println("Average: " + results.get(car) + "\n");
			GUI.APIReturn("Average: " + results.get(car) + "\n");
		}
		//System.out.println(allFeatures);

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
		//double overallSentiment = SimpleRunner.calculateOverallSentiment(patterns);
		//System.out.println(Overall" + "		" + overallSentiment + "\n\n");

	}

	private static boolean isNumeric(String str)  
	{  
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}
	static long startTime = 0;
	static long stopTime = 0;
	private static void printTime(String cmd) {
		if(cmd.equals("Start"))
			startTime = System.currentTimeMillis();
		if(cmd.equals("Stop"))
			stopTime = System.currentTimeMillis();
		if(startTime!=0&&stopTime!=0) {
			System.out.println(stopTime - startTime);
			startTime = 0;
			stopTime = 0;
		}
	}

}

package CarReviewer;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class SimpleRunner {

	private static Map<String, List<String>> map;
	private Extract extract;
	public double overallSentiment = 0;

	// Read JSON and create HashMap with car as key and reviews as values
	public static List<String> getKeys() {
		JSONParser parser = new JSONParser();
		try {

			// Read Reviews
			Object obj = parser.parse(new FileReader(
					"Reviews - full.json"));

			// Create JSON object
			JSONObject jsonObject = (JSONObject) obj;

			// Create a new map
			map = new HashMap<String, List<String>>();

			// Loop through the JSON and place reviews from the same car under one key
			for(Iterator<?> iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = (String) jsonObject.get(key);

				if (!map.containsKey(key)) {
					map.put(key, new ArrayList<String>());
				}
				
				map.get(key).add(value);
			}
			return new ArrayList<String>(map.keySet());
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>(map.keySet());
		}
	}

	// Extract patterns
	public List<Pattern> run(String key, StanfordCoreNLP pipeline) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		extract = new Extract();

		// Extract patterns
		patterns = extract.run(map.get(key).toString(), pipeline);
		overallSentiment = extract.getOverallSentiment();
			
		return patterns;

	}

	// Sort HashMap on highest values
	// From: https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java by Carter Page
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue, 
						(e1, e2) -> e1, 
						LinkedHashMap::new
						));
	}

	// Make all keys pretty
	public static List<String> trimKeys(List<String> keys) {
		List<String> newKeys = new ArrayList<String>();
		for (String key : keys) {
			key = key.replace("_", " ");
			key = key.substring(0, 1).toUpperCase() + key.substring(1);
			newKeys.add(key);
		}
		if (newKeys.size() == keys.size())
			return newKeys;
		else
			throw new Error();
	}

	// Make a single key pretty
	public static String trimKey(String key) {
		key = key.replace("_", " ");
		key = key.substring(0, 1).toUpperCase() + key.substring(1);
		return key;
	}

	// Reverse trimming to match with database
	public static String unTrimKey(String key) {
		key = key.replace(" ", "_");
		key = key.substring(0, 1).toLowerCase() + key.substring(1);
		return key;
	}
}

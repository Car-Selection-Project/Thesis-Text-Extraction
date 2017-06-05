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

public class SimpleRunner {

	static Map<String, List<String>> map;
	Extract extract;
	public double overallSentiment = 0;

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

				//System.out.println(key + " : " + map.get(key));
			}
			return new ArrayList<String>(map.keySet());
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>(map.keySet());
		}
	}

	public List<Pattern> run(String key) {
		List<String> patternlist = new ArrayList<String>();
		List<Pattern> patterns = new ArrayList<Pattern>();
		extract = new Extract();
		// Loop through the map, extract pattern for each car, and write to file
		//Map<String,Integer> wordMap = new HashMap<String, Integer>();
		Iterator<Map.Entry<String, List<String>>> it = map.entrySet().iterator();

		// If a single car is selected
		if(!key.equals("all")) {
			// Extract patterns
			patterns = extract.run(map.get(key).toString());
			overallSentiment = extract.getOverallSentiment();
		}

		else {
			// For all cars
			try{
				while (it.hasNext()) {
					Map.Entry<String, List<String>> pair = it.next();
					// TODO: Still print out all patterns for each car to files?
					// Prepare to write to file
					/*System.out.println(pair.getKey() + " = " + pair.getValue());
					File file = new File("features/" + pair.getKey().toString() + ".txt");
					file.createNewFile();
					FileWriter fw = new FileWriter("features/" + pair.getKey().toString() + ".txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw);
					*/

					// Extract patterns
					List<Pattern> patternIterate = extract.run(pair.getValue().toString());
					patterns.addAll(patternIterate);
					if (patternIterate.size() != 0) {
						for (Pattern pattern : patternIterate) {
							String patternToAdd = pattern.toAspect();
							if(patternToAdd != "") {	
								patternlist.add(patternToAdd);
								//System.out.println(patternToAdd);
							//	out.println(patternToAdd);
							//	out.println("Sentiment: " + pattern.getSentiment());
							}
						}
					}
					else {
					//	file.delete();
						continue;
					}
					//out.close();
				}
				//System.out.println(patternlist);
				/*wordMap = countEachWord(patternlist);
				Iterator<Entry<String,Integer>> worditerator = wordMap.entrySet().iterator();
				File file = new File("features/featurecount.txt");
				file.createNewFile();
				FileWriter fw = new FileWriter("features/featurecount.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				while (worditerator.hasNext()) {
					// the key/value pair is stored here in pairs
					Map.Entry<String, Integer> pairs = worditerator.next();

					// since you only want the value, we only care about pairs.getValue(), which is written to out
					out.println(pairs.getKey() + ": " + pairs.getValue());
				}
				out.close();*/
			}
			// it.remove(); // avoids a ConcurrentModificationException
			catch(Exception e) {
				e.printStackTrace();
			} 
		}
		return patterns;

	}

	// Count each word function
	public static Map<String, Integer> countEachWord(List<String> list) {

		HashMap<String, Integer> map = new HashMap<>();

		for(int i = 0; i<list.size(); i++) {
			String word = list.get(i);
			word = word.toLowerCase();

			if(word.isEmpty()) {
				continue;
			}
			if(map.containsKey(word)) {
				map.put(word, map.get(word)+1);
			}
			else {
				map.put(word, 1);
			}


		}

		map = (HashMap<String, Integer>)sortByValue(map);

		return map;
	}
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
	public static double calculateOverallSentiment(List<Pattern> patterns) {
		double sentiment = 0;
		int count = 0;
		for (Pattern pattern : patterns) {
			sentiment += pattern.getSentiment();
			count++;
		}
		sentiment = sentiment / count;
		return sentiment;
	}
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

	// Trim a single key
	public static String trimKey(String key) {
		key = key.replace("_", " ");
		key = key.substring(0, 1).toUpperCase() + key.substring(1);
		return key;
	}

	public static String unTrimKey(String key) {
		key = key.replace(" ", "_");
		key = key.substring(0, 1).toLowerCase() + key.substring(1);
		return key;
	}
}

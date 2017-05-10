import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SimpleRunner {

	// Your workspace folder
	final static String basePath = "C:/Users/koenn/workspace/Thesis/src/readJSON/";
			 
    public static void main(String[] args) {
        Extract extract = new Extract();
		 JSONParser parser = new JSONParser();

	        try {
	        	
	        	// Read Reviews
	            Object obj = parser.parse(new FileReader(
	                    basePath + "/Reviews - medium.json"));
	            
	            // Create JSON object
	            JSONObject jsonObject = (JSONObject) obj;
	            
	            // Create a new map
	            Map<String, List<String>> map = new HashMap<String, List<String>>();
	            
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
	            
	            // Loop through the map, extract pattern for each car, and write to file
	            Map<String,Integer> wordMap = new HashMap<String, Integer>();
	            Iterator<?> it = map.entrySet().iterator();
	            List<String> patternlist = new ArrayList();
	            
	            while (it.hasNext()) {
	                Map.Entry pair = (Map.Entry)it.next();
	                System.out.println(pair.getKey() + " = " + pair.getValue());
	                String value = (String) pair.getValue().toString();
	                
	                File file = new File(basePath + "features/" + pair.getKey().toString() + ".txt");
	                file.createNewFile();
	                FileWriter fw = new FileWriter(basePath + "features/" + pair.getKey().toString() + ".txt", true);
	                BufferedWriter bw = new BufferedWriter(fw);
	                PrintWriter out = new PrintWriter(bw);
	                
	                // Extract patterns
	                List<Pattern> patterns = extract.run(pair.getValue().toString());
	                if (patterns.size() != 0) {
	        	        for (Pattern pattern : patterns) {
	        	        	String patternToAdd = pattern.toAspect();
		        	        if(patternToAdd != "") {	
	        	        		patternlist.add(patternToAdd);
		        	        	System.out.println(patternToAdd);
		        	        	out.println(patternToAdd);
		        	        	out.println("Sentiment: " + pattern.getSentiment());
		        	        }
	        	        }
	                }
	                else {
	                	file.delete();
	                	continue;
	                }
        	        out.close();
	            }
	            System.out.println(patternlist);
	            wordMap = countEachWord(patternlist);
	            Iterator<Entry<String,Integer>> worditerator = wordMap.entrySet().iterator();
	            File file = new File(basePath + "features/featurecount.txt");
                file.createNewFile();
                FileWriter fw = new FileWriter(basePath + "features/featurecount.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                while (worditerator.hasNext()) {
                    // the key/value pair is stored here in pairs
                    Map.Entry pairs = (Map.Entry)worditerator.next();
                    
                    // since you only want the value, we only care about pairs.getValue(), which is written to out
                    out.println(pairs.getKey() + ": " + pairs.getValue());
                }
	            out.close();
	        }
	               // it.remove(); // avoids a ConcurrentModificationException
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
    }
    
 // Count each word function
 	public static Map countEachWord(List list) {
 		
 		HashMap<String, Integer> map = new HashMap<>();

 		for(int i = 0; i<list.size(); i++) {
 			String word = (String)list.get(i);
 			word = word.toLowerCase();
 			System.out.println(word);
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
 		
 		for(Map.Entry<String, Integer> entry : map.entrySet()) {
 		    System.out.println(entry.getKey() + ": " + entry.getValue());
 		}
 		
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
 	/*public static String checkRequirements(String pattern, List<String>featureList) {
 		try {
            featureList = LoadrequirementWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
 		
 		String[] patternSplit = pattern.split("\\s+");
 		for(String word : patternSplit) {
	 		if(featureList.contains(word.toLowerCase())) {
	 			System.out.println(pattern);
	 			return pattern;
	 		}
 		}
 		return "";
 	}*/
 	
}

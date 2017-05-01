/**
 * 
 */
package readJSON;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 * @author Koen
 *
 */
public class ReadFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 JSONParser parser = new JSONParser();
		 
		 // Your workspace folder
		 String basePath = "C:/Users/koenn/workspace/Thesis/src/readJSON/";
	        try {
	        	
	        	// Read Reviews
	            Object obj = parser.parse(new FileReader(
	                    basePath + "/Reviews.json"));
	            
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
	            
	            // Loop through the map and count how many times a word has been counted within the value of a key
	            Map<String,Integer> wordMap = new HashMap<String, Integer>();
	            Iterator<?> it = map.entrySet().iterator();

	            while (it.hasNext()) {
	                Map.Entry pair = (Map.Entry)it.next();
	                System.out.println(pair.getKey() + " = " + pair.getValue());
	                String value = (String) pair.getValue().toString();
	                
	                wordMap = countEachWord(pair.getKey().toString(), value);
	                
	                // create your iterator for your map
	                Iterator<Entry<String,Integer>> worditerator = wordMap.entrySet().iterator();

	                File file = new File(basePath + "features/" + pair.getKey().toString() + ".txt");
	                file.createNewFile();
	                FileWriter fw = new FileWriter(basePath + "features/" + pair.getKey().toString() + ".txt", true);
	                BufferedWriter bw = new BufferedWriter(fw);
	                PrintWriter out = new PrintWriter(bw);
	                		
	                while (worditerator.hasNext()) {

	                    // the key/value pair is stored here in pairs
	                    Map.Entry pairs = (Map.Entry)worditerator.next();
	                    
	                    // since you only want the value, we only care about pairs.getValue(), which is written to out
	                    out.println(pairs.getKey() + ": " + pairs.getValue());
	                }
	                out.close();
	                
	               // it.remove(); // avoids a ConcurrentModificationException
	            }
	            
	            System.out.println(countWords(wordMap.entrySet().toString()));
	            String jsonString = jsonObject.toString();
		        System.out.println(countWords(jsonString));
		        
		      //  Path file = Paths.get("test.txt");
		      //  Files.write(file, wordMap, Charset.forName("UTF-8"));
		        
	        }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }

	}
	
	// Count total number of words function
	public static int countWords(String original){

        String[] s1 = original.split("\\s+");

        for(int i = 0; i < s1.length; i++){
            s1[i] = s1[i].replaceAll("^\\W]", "");
        }


        int count = 0;
        for(int i = 0; i < s1.length; i++){
            String str = s1[i];
            int len = 0;
            for(int x = 0; x < str.length(); x++){
                char c = str.charAt(x);

                if(Character.isLetter(c) == true){
                    len ++;
                }
            }
            count ++;
        }

        return count;
    }
	
	// Count each word function
	public static Map countEachWord(String key, String list) {
		
		HashMap<String, Integer> map = new HashMap<>();
		
		List<String> featureList = new ArrayList<String>();
		featureList.addAll(Arrays.asList(
				"seat",
				"ride",
				"interior",
				"accelaration",
				"handle",
				"visibility",
				"look",
				"technology",
				"performance",
				"design",
				"engine",
				"power",
				"transmission",
				"speed",
				"vehicle",
				"model",
				"value",
				"deal",
				"price",
				"feature",
				"time",
				"year",
				"issue",
				"quality",
				"experience",
				"wheel",
				"drive",
				"noise",
				"camera",
				"screen",
				"system",
				"space",
				"room",
				"seats",
				"control",
				"sound",
				"fuel",
				"safety",
				"mileage",
				"gas"
				));
		
		for(String word : list.split("\\W")) {
		    if(word.isEmpty()) {
		        continue;
		    }
			if(featureList.contains(word.toLowerCase())) {
		    	if(map.containsKey(word)) {
			        map.put(word, map.get(word)+1);
			    }
			    else {
			        map.put(word, 1);
			    }
			}
			else {
				continue;
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
}

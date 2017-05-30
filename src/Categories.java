import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Categories {

	List<Pattern> patterns;
	ArrayList<List<String>> arrayCategories = new ArrayList<List<String>>();

	public Categories(List<Pattern> patterns) {
		this.patterns = patterns;
		makeCategories();
		sortCategories();
	}

	private void makeCategories() {
		try {
			File fXmlFile = new File("requirementCategories.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("Category");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					List<String> category = new ArrayList<String>();
					category.add(eElement.getAttribute("name"));

					for (int i=0; eElement.getElementsByTagName("feature").getLength() > i; i++) {
						category.add(eElement.getElementsByTagName("feature").item(i).getTextContent());
					};

					arrayCategories.add(category);
				}
			}
			System.out.println(arrayCategories);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sortCategories() {
		for(Pattern pattern : patterns) {	
			for(List<String> category : arrayCategories) {
				for(String feature : category) {
					if (pattern.toAspect().contains(feature)) {
						pattern.category = category.get(0);
					}
				}
			}
			if(pattern.category == null) 
				pattern.category = "Other";
		}
	}

	public static ArrayList<List<String>> groupCategories(List<Pattern> patterns) {
		ArrayList<List<String>> categoriesArray = new ArrayList<List<String>>();
		// Clear category lists

		int preventException = 0;
		// Create category lists from patterns
		for (Pattern pattern : patterns) {
			int i = 0;
			boolean found = false;
			do {
				if (preventException==0) { // first pattern to get added
					List<String> category = new ArrayList<String>();
					category.add(pattern.category);	
					categoriesArray.add(category);
					category.add(Double.toString(pattern.getSentiment()));
					found=true;
					break;
				}
				else { // category already added
					List<String> match = categoriesArray.get(i);
					if(match.get(0) == pattern.category) {
						match.add(Double.toString(pattern.getSentiment()));
						found=true;
						break;
					}
				}
				i++;
			}
			while (i < categoriesArray.size());
			if (!found) { // add new category
				List<String> category = new ArrayList<String>();
				category.add(pattern.category);
				category.add(Double.toString(pattern.getSentiment()));
				categoriesArray.add(category);
			}
			preventException++;
		}
		//System.out.println(categoriesArray);
		return categoriesArray;
	}
}

import java.util.ArrayList;
import java.util.HashMap;

public class WordToWikiLinkGraph {
	public static void main(String args[]) {
		HashMap<String, Triple> labelToURI = new HashMap<String, Triple>();

		HashMap<String, Triple> rootNodesAdjNodesList = new HashMap<String, Triple>();
		// String[] test = new String[] { "fernsehserie", "filmografie",
		// "gastrolle", "rolle", "serie" };
		String[] test = new String[] { "zeit", "teil", "hauptartikel", "ende", "entwicklung" };
		String languageTag = "de";

		String surfaceFormsFile = "/data/r.usbeck/dbpedia_" + languageTag + "/labels_" + languageTag + ".nt";
		LabelURLIndex labelIndex = new LabelURLIndex(surfaceFormsFile, "label/", LabelURLIndex.N_TRIPLES, "http://dbpedia.org");

		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("/data/r.usbeck/dbpedia_" + languageTag + "/page_links_" + languageTag + ".nt");
		SubjectPredicateObjectIndex tripleIndex = new SubjectPredicateObjectIndex(tmp, "triple/", "http://dbpedia.org", SubjectPredicateObjectIndex.N_TRIPLES);

		for (String t : test) {
			labelToURI.put(t, labelIndex.searchInLabels(t, false).get(0));
		}

		// Breadth First Search
		for (String root : labelToURI.keySet()) {
			if (labelToURI.get(root) != null) {

			}
		}
		// durchschnittliche Abstand aller Knoten

		// in-degree jeder Knoten
	}
}

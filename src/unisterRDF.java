import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class unisterRDF {

    private static HashMap<String, Integer> nodeNames;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open("unister-owl.rdf");

        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        // read the RDF/XML file
        model.read(in, null);
        nodeNames = new HashMap<String, Integer>();
        ResIterator iter = model.listSubjects();
        int node = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("unister-owl.tgf"));
        while (iter.hasNext()) {
            RDFNode N = iter.next();
            if (N.isResource() && N.asResource().getLocalName() != null) {
                String localName = getAlternativeRessourceName(N);
                nodeNames.put(localName, node);
                bw.write(node + " " + localName);
                bw.newLine();
                node++;
            }
        }
        bw.write(node + " " + "outerNode");
        bw.newLine();
        bw.write("#");
        bw.newLine();
        // Edges
        String[] edgeTypes = new String[] { "subClassOf", "sameAs", "domain", "subPropertyOf", "disjointWith" };
        iter = model.listSubjects();
        while (iter.hasNext()) {
            RDFNode N = iter.next();
            Resource R = N.asResource();
            StmtIterator it = R.listProperties();
            while (it.hasNext()) {
                Statement S = it.next();
                for (String type : edgeTypes) {
                    if (S.getPredicate().getLocalName().matches(type)) {
                        writeEdge(bw, S, node);
                    }
                }
            }
        }
        bw.close();
    }

    private static void writeEdge(BufferedWriter bw, Statement S, int node)
            throws IOException {
        Resource object = S.getObject().asResource();
        Resource subject = S.getSubject();
        // TODO: what to do with blank nodes?
        if (object.getLocalName() != null && subject.getLocalName() != null) {
            String localNameObject = getAlternativeRessourceName(object);
            String localNameSubject = getAlternativeRessourceName(subject);
            Integer objectId = nodeNames.get(localNameObject);
            Integer subjectId = nodeNames.get(localNameSubject);
            if (objectId != null && subjectId != null) {
                bw.write(objectId + " " + subjectId + " " + S.getPredicate().getLocalName());
                bw.newLine();
            }
            else {
                // link edges linking to outer nodes to a special node outer
                bw.write(subjectId + " " + node + " " + S.getPredicate().getLocalName());
                bw.newLine();
            }
        }
    }

    private static String getAlternativeRessourceName(RDFNode N) {
        // bug: # isn't accepted by yed
        return (N.asResource().getNameSpace() + "" + N.asResource().getLocalName()).replaceAll("#", "_");
    }
}

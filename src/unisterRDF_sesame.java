import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParser;

public class unisterRDF_sesame {

    private static HashMap<String, Integer> nodeNames;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");

        RDFParser parser = new RDFXMLParser();
        StatementCollector statementCollector = new StatementCollector();
        parser.setRDFHandler(statementCollector);
        parser.setVerifyData(true);
        parser.setStopAtFirstError(false);

        parser.parse(new FileInputStream(new File("unister-owl.rdf")), "http://ontology.unister.de/ontology");

        Collection<Statement> statements = statementCollector.getStatements();

        // create an empty model
        // read the RDF/XML file
        // GraphWriter graphWriter = new TGFWriter(new BufferedWriter(new FileWriter("unister-owl.tgf")));
        GraphWriter graphWriter = new GraphMLWriter(new BufferedWriter(new FileWriter("unister-owl.graphml")));
        nodeNames = new HashMap<String, Integer>();
        int node = 0;
        Iterator<Statement> iter = statements.iterator();
        String[] edgeTypes = new String[] { "subClassOf", "sameAs", "domain", "range", "subPropertyOf", "disjointWith" };
        while (iter.hasNext()) {
            Statement N = iter.next();
            for (String edge : edgeTypes) {
                if (N.getPredicate().getLocalName().matches(edge)) {
                    node = graphWriter.writeNode(node, N, nodeNames);
                }
            }
        }
        graphWriter.write(node + " " + "outerNode");
        graphWriter.newLine();
        graphWriter.write("#");
        graphWriter.newLine();
        // Edges
        iter = statements.iterator();
        while (iter.hasNext()) {
            Statement N = iter.next();
            for (String edge : edgeTypes) {
                if (N.getPredicate().getLocalName().matches(edge)) {
                    Value object = N.getObject();
                    Resource subject = N.getSubject();
                    // TODO: what to do with blank nodes?
                    if (object.stringValue() != null && subject.stringValue() != null) {
                        graphWriter.writeEdge(N, node, nodeNames);
                    }
                }
            }
        }
        graphWriter.close();
    }
}

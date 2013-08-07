import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.activation.UnknownObjectException;
import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class GraphMLWriter implements GraphWriter {

    BufferedWriter bw;
    private String markupEnd;
    private String markupStart;
    private int edgeCount = 0;

    public GraphMLWriter(BufferedWriter bw) throws IOException
    {
        this.bw = bw;
        markupStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xmlns:y=\"http://www.yworks.com/xml/graphml\"\n"
                + "xmlns:yed=\"http://www.yworks.com/xml/yed/3\"\n"
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">\n"
                + "<key for=\"node\" id=\"key1\" yfiles.type=\"nodegraphics\"/>\n"
                + "<key for=\"edge\" id=\"key0\" yfiles.type=\"edgegraphics\"/>\n"
                + "<key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>\n"
                + "<key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d6\"/>\n"
                + "<key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>"
                + "<graph id=\"G\" edgedefault=\"directed\"> ";
        markupEnd = "</graph>\n" +
                " <data key=\"d0\">" +
                "<y:Resources/>" +
                " </data>" + "</graphml>";
        bw.write(markupStart);
        bw.newLine();
    }

    @Override
    public int writeNode(int node, Statement N, HashMap<String, Integer> nodeNames) throws IOException {
        String localName = N.getSubject().stringValue();
        if (!nodeNames.containsKey(localName)) {
            nodeNames.put(localName, node);
            bw.write("<node id=\"" + node + "\"> \n" +
                    "<data key=\"d5\"/>\n" +
                    "<data key=\"key1\">\n" +
                    "<y:ShapeNode>\n" +
                    "<y:Fill color=\"#FFFFFF\" transparent=\"false\"/>" +
                    "<y:NodeLabel>" + localName + "</y:NodeLabel>\n" +
                    "</y:ShapeNode>\n" +
                    "</data>\n" +
                    "</node>");
            bw.newLine();
            node++;
        }
        localName = N.getObject().stringValue();
        if (!nodeNames.containsKey(localName)) {
            nodeNames.put(localName, node);
            bw.write("<node id=\"" + node + "\"> \n" +
                    "<data key=\"d5\"/>\n" +
                    "<data key=\"key1\">\n" +
                    "<y:ShapeNode>\n" +
                    "<y:Fill color=\"#FFFFFF\" transparent=\"false\"/>" +
                    "<y:NodeLabel>" + localName + "</y:NodeLabel>\n" +
                    "</y:ShapeNode>\n" +
                    "</data>\n" +
                    "</node>");
            bw.newLine();
            node++;
        }
        return node;
    }

    @Override
    public void writeEdge(Statement N, int node, HashMap<String, Integer> nodeNames) throws IOException,
            UnknownObjectException {
        Value object = N.getObject();
        Resource subject = N.getSubject();
        String predicate = N.getPredicate().getLocalName();
        bw.write("<edge id=\"" + edgeCount++ + "\" source=\"" +
                nodeNames.get(subject.stringValue()) + "\" target=\"" +
                nodeNames.get(object.stringValue()) + "\">\n" +
                "<data key=\"d6\"/>\n" +
                "<data key=\"key0\">\n" +
                "<y:PolyLineEdge>\n" +
                "<y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n" +
                "<y:Arrows source=\"none\" target=\"standard\"/>\n" +
                "<y:EdgeLabel>" + predicate + "</y:EdgeLabel>\n" +
                "</y:PolyLineEdge>\n" +
                "</data>\n" +
                "</edge>");
        bw.newLine();

    }

    @Override
    public String getAlternativeName4Object(Object resource) throws UnknownObjectException {
        // bug: # isn't accepted by yed
        if (resource instanceof Resource) {
            return ((Resource) resource).stringValue();
        }
        else if (resource instanceof Value) {
            return ((Value) resource).stringValue();
        }
        else {
            throw new UnknownObjectException("Couldn't get the class cast right");
        }
    }

    @Override
    public void write(String string) {
        // don't write anything plain in an well formed xml file

    }

    @Override
    public void newLine() throws IOException {
        bw.newLine();

    }

    @Override
    public void close() throws IOException {
        bw.write(markupEnd);
        bw.newLine();
        bw.close();

    }

}

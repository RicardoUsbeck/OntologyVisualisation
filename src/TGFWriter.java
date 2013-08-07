import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.activation.UnknownObjectException;
import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class TGFWriter implements GraphWriter {
    BufferedWriter bw;

    public TGFWriter(BufferedWriter bw)
    {
        this.bw = bw;
    }

    @Override
    public int writeNode(int node, Statement N, HashMap<String, Integer> nodeNames)
            throws IOException, UnknownObjectException {
        String localName = getAlternativeName4Object(N.getSubject());
        if (!nodeNames.containsKey(localName)) {
            nodeNames.put(localName, node);
            bw.write(node + " " + localName);
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
        String localNameObject = getAlternativeName4Object(object);
        String localNameSubject = getAlternativeName4Object(subject);
        String predicate = N.getPredicate().getLocalName();
        Integer objectId = nodeNames.get(localNameObject);
        Integer subjectId = nodeNames.get(localNameSubject);
        if (objectId != null && subjectId != null) {
            bw.write(objectId + " " + subjectId + " " + predicate);
        } else {
            // link edges linking to outer nodes to a special node outer
            bw.write(subjectId + " " + node + " " + predicate);
        }
        bw.newLine();
    }

    @Override
    public String getAlternativeName4Object(Object resource) throws UnknownObjectException {
        // bug: # isn't accepted by yed
        if (resource instanceof Resource) {
            return (((Resource) resource).stringValue()).replaceAll("#", "_");
        }
        else if (resource instanceof Value) {
            return (((Value) resource).stringValue()).replaceAll("#", "_");
        }
        else {
            throw new UnknownObjectException("Couldn't get the class cast right");
        }
    }

    @Override
    public void write(String string) throws IOException {
        bw.write(string);

    }

    @Override
    public void newLine() throws IOException {
        bw.newLine();

    }

    @Override
    public void close() throws IOException {
        bw.close();

    }
}

import java.io.IOException;
import java.rmi.activation.UnknownObjectException;
import java.util.HashMap;

import org.openrdf.model.Statement;

public interface GraphWriter {

    public abstract int writeNode(int node, Statement N, HashMap<String, Integer> nodeNames)
            throws IOException, UnknownObjectException;

    public abstract void writeEdge(Statement N, int node,
            HashMap<String, Integer> nodeNames)
            throws IOException, UnknownObjectException;

    public abstract String getAlternativeName4Object(Object resource) throws UnknownObjectException;

    public abstract void write(String string) throws IOException, Exception;

    public abstract void newLine() throws IOException;

    public abstract void close() throws IOException;

}
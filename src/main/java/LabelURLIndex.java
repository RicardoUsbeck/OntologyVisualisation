import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.LoggerFactory;

public class LabelURLIndex {
	public static final String TSV = "TSV";
	public static final String N_TRIPLES = "NTriples";
	public static final String TTL = "TTL";
	private org.slf4j.Logger log = LoggerFactory.getLogger(LabelURLIndex.class);
	private String FIELD_NAME_URL = "url";
	private String FIELD_NAME_LABEL = "label";
	private Directory directory;
	private Analyzer analyzer;
	private IndexSearcher isearcher;
	private QueryParser parser;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private HashMap<String, List<Triple>> cache;
	private HashMap<String, List<Triple>> cacheSearch;

	public LabelURLIndex(String file, String idxDirectory, String type, String baseURI) {
		try {
			analyzer = new KeywordAnalyzer();
			File indexDirectory = new File(idxDirectory);

			if (indexDirectory.exists() && indexDirectory.isDirectory() && indexDirectory.listFiles().length > 0) {
				// directory = new RAMDirectory(new
				// SimpleFSDirectory(indexDirectory), IOContext.DEFAULT);
				directory = new MMapDirectory(indexDirectory);
			} else {
				indexDirectory.mkdir();
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
				iwriter = new IndexWriter(directory, config);
				if (type.equals(TTL))
					indexTTLFile(file, baseURI);
				if (type.equals(N_TRIPLES))
					indexNTriplesFile(file, baseURI);
				if (type.equals(TSV))
					indexTSVFile(file);
				iwriter.close();
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			cache = new HashMap<String, List<Triple>>();
			cacheSearch = new HashMap<String, List<Triple>>();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	public LabelURLIndex(String idxDirectory) {
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			File indexDirectory = new File(idxDirectory);
			directory = new MMapDirectory(indexDirectory);
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			cache = new HashMap<String, List<Triple>>();
			cacheSearch = new HashMap<String, List<Triple>>();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private void indexTSVFile(String surfaceFormsTSV) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(surfaceFormsTSV));
			while (br.ready()) {
				String[] line = br.readLine().split("\t");
				String subject = line[0];
				for (int i = 1; i < line.length; ++i) {
					String object = line[i];
					Document doc = new Document();
					doc.add(new StringField(FIELD_NAME_URL, subject, Store.YES));
					doc.add(new TextField(FIELD_NAME_LABEL, object, Store.YES));
					iwriter.addDocument(doc);
				}
			}
			br.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private void indexTTLFile(String file, String baseURI) {
		try {
			log.info("Start parsing: " + file);
			RDFParser parser = new TurtleParser();
			OnlineStatementHandler osh = new OnlineStatementHandler();
			parser.setRDFHandler(osh);
			parser.setStopAtFirstError(false);
			parser.parse(new FileReader(file), baseURI);
			log.info("Finished parsing: " + file);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		} catch (RDFParseException e) {
			log.error(e.getLocalizedMessage());
		} catch (RDFHandlerException e) {
			log.error(e.getLocalizedMessage());
		}

	}

	private void indexNTriplesFile(String file, String baseUri) {
		try {
			log.info("Start parsing: " + file);
			RDFParser parser = new NTriplesParser();
			OnlineStatementHandler osh = new OnlineStatementHandler();
			parser.setRDFHandler(osh);
			parser.setStopAtFirstError(false);
			log.info("Finished parsing: " + file);
			parser.parse(new FileReader(file), baseUri);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		} catch (RDFParseException e) {
			log.error(e.getLocalizedMessage());
		} catch (RDFHandlerException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private void addDocumentToIndex(String subject, String predicate, String object) {
		try {
			Document doc = new Document();
			doc.add(new StringField(FIELD_NAME_URL, subject, Store.YES));
			doc.add(new TextField(FIELD_NAME_LABEL, object.toLowerCase(), Store.YES));
			iwriter.addDocument(doc);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}


	public List<Triple> searchInLabels(String object, boolean replaceObject) {
		if (cacheSearch.containsKey(object)) {
			return cacheSearch.get(object);
		}
		List<Triple> triples = new ArrayList<Triple>();
		try {
			parser = new QueryParser(Version.LUCENE_40, FIELD_NAME_LABEL, new KeywordAnalyzer());
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = parser.parse(object);
			ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String subject = java.net.URLDecoder.decode(hitDoc.get(FIELD_NAME_URL), "UTF-8");
				String predicate = "http://www.w3.org/2000/01/rdf-schema#label";
				if (replaceObject)
					object = hitDoc.get(FIELD_NAME_LABEL);
				triples.add(new Triple(subject, predicate, object));
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + object);
		}
		cache.put(object, triples);
		return triples;
	}

	public List<Triple> getLabelForURI(String subject) {
		if (cache.containsKey(subject)) {
			return cache.get(subject);
		}
		parser = new QueryParser(Version.LUCENE_40, FIELD_NAME_URL, analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		analyzer = new KeywordAnalyzer();
		List<Triple> triples = new ArrayList<Triple>();
		try {
			log.debug("\t start asking index...");
			TermQuery tq = new TermQuery(new Term(FIELD_NAME_URL, subject));
			BooleanQuery bq = new BooleanQuery();
			bq.add(tq, BooleanClause.Occur.SHOULD);
			TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
			isearcher.search(bq, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String object = java.net.URLDecoder.decode(hitDoc.get(FIELD_NAME_LABEL), "UTF-8");
				String predicate = "rdfs:label";
				triples.add(new Triple(subject, predicate, object));
			}
			log.debug("\t finished asking index...");
		} catch (Exception e) {
			log.warn(e.getLocalizedMessage() + " -> " + subject);
		}
		cache.put(subject, triples);
		return triples;
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class OnlineStatementHandler extends RDFHandlerBase {
		@Override
		public void handleStatement(Statement st) {
			String subject = st.getSubject().stringValue();
			String predicate = st.getPredicate().stringValue();
			String object = st.getObject().stringValue();
			addDocumentToIndex(subject, predicate, object);
		}
	}
}

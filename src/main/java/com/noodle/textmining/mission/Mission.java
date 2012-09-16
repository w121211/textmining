package com.noodle.textmining.mission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.noodle.textmining.core.*;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public abstract class Mission {

	protected enum Code {
		CHINESE_NLP, CRAWL_WEB, HUB_PAGERANK, PAGERANK, REMOVE_DB, TFIDF,
	}

	protected static String crawlDomain;
	protected static Pattern crawlFilters;
	protected static String crawlStorageFolder;
	protected static ODatabaseDocumentTx db;
	protected static Map<String, String> dbNameMap;
	protected static String exportDirectory;
	protected MissionCrawler crawler;
	protected String[] crawlURLSeeds;
	protected int maxPagerankTermNumber;
	protected int minDependencyDocsNumber;
	protected String filterAnyTermsPath;
	protected String filterExactTermsPath;
	protected String hubsPath;
	protected String missionName;
	protected String terminalsPath;
	protected String tfidfPath;
	

	public Mission(String missionName, String dbDirectory) {
		this.missionName = missionName;
		db = new ODatabaseDocumentTx(dbDirectory);
		if (db.exists()) {
			System.out.println("db exists, connecting to the db");
			db.open("admin", "admin");
		} else {
			System.out.println("db dose not exist, creating a new db");
			db.create();
		}

		crawlFilters = Pattern
				.compile(".*(\\.(css|js|bmp|gif|jpe?g"
						+ "|png|tiff?|mid|mp2|mp3|mp4"
						+ "|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
		crawlStorageFolder = "crawl/" + missionName + "/";
		exportDirectory = "export/";
		maxPagerankTermNumber = -1; // negative numbers refer to no limits

		dbNameMap = new HashMap<String, String>();
		dbNameMap.put("crawlClass", "Crawl");
		dbNameMap.put("crawlFields", "url,title,text");
		dbNameMap.put("nlpClass", "Nlp");
		dbNameMap.put("nlpFields", "title,text");
		dbNameMap.put("pagerankField", "text");
	}

	/**
	 * 
	 */
	public void close() {
		db.close();
	}

	/**
	 * 
	 * @param dbDirectory
	 */
	public void createDatabase(String dbDirectory) {
		db = new ODatabaseDocumentTx(dbDirectory).create();
	}

	/**
	 * Create a terminal file '{mission_name}_terminals.txt' under the export
	 * directory. It uses tfidfTerms, select the first 'terminalNumber' (e.g.
	 * 1000) terms with excluding of hubs and filtering junk words.
	 * 
	 * @param terminalNumber
	 * @throws IOException
	 */
	public void createTerminals(int terminalNumber) throws IOException {
		String[] terms = this.getTerms(tfidfPath, ":");

		List<String> terminals = new ArrayList<String>();
		for (String term : terms) {
			if (terminals.size() > terminalNumber)
				break;
			Pattern filter = Pattern.compile(this
					.getAnyFilter(filterAnyTermsPath)
					+ "|"
					+ this.getExactFilter(filterExactTermsPath)
					+ "|"
					+ this.getExactFilter(hubsPath));
			if (!filter.matcher(term).matches())
				terminals.add(term);
		}
		System.out.println(terminals);

		BufferedWriter writer = this.getBufferedWriter(terminalsPath);
		for (String terminal : terminals) {
			writer.write(terminal + "\n");

		}
		writer.flush();
	}

	private String getAnyFilter(String filePath) throws IOException {
		String regexp = getExactFilter(filePath);
		return "(.*" + regexp + ".*)";
	}

	private BufferedReader getBufferedReader(String filePath)
			throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new DataInputStream(
				new FileInputStream(filePath))));
	}

	private BufferedWriter getBufferedWriter(String filePath)
			throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				filePath)));
	}

	/**
	 * 
	 * @param db
	 * @param className
	 * @param fieldName
	 * @return docs
	 */
	public List<String> getDocs(ODatabaseDocumentTx db, String className,
			String fieldName) {
		List<String> list = new ArrayList<String>();
		for (ODocument doc : db.browseClass(className)) {
			if (doc.field(fieldName) != null)
				list.add((String) doc.field(fieldName));
		}
		return list;
	}

	private String getExactFilter(String filePath) throws IOException {
		String regexp = "";
		BufferedReader reader = getBufferedReader(filePath);
		String str;
		while ((str = reader.readLine()) != null)
			regexp += str + "|";
		if (regexp.length() > 0)
			return "(" + regexp.substring(0, regexp.length() - 1) + ")";
		else
			return "";
	}

	public String[] getTerms(String filePath, String stopString)
			throws IOException {
		BufferedReader reader = this.getBufferedReader(filePath);
		List<String> list = new ArrayList<String>();
		String str;
		while ((str = reader.readLine()) != null) {
			if (stopString == null) {
				list.add(str);
			} else {
				if (str.indexOf(":") > 0) {
					String s = str.substring(0, str.indexOf(stopString));
					// if (s.length() > 1 && s.matches("[\\S&&[^╱，.]]{2,}?"))
					if (s.length() > 0)
						list.add(s);
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public void init() {
		if (tfidfPath == null)
			tfidfPath = exportDirectory + missionName + "_"
					+ dbNameMap.get("pagerankField") + "_tfidf.txt";
		if (filterAnyTermsPath == null)
			filterAnyTermsPath = "data/filter_any_terms.txt";
		if (filterExactTermsPath == null)
			filterExactTermsPath = "data/filter_exact_terms.txt";
		if (minDependencyDocsNumber == 0)
			minDependencyDocsNumber = 100;
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void runChineseNLP() throws IOException, ClassNotFoundException {
		ChineseNLProcessor proc = new ChineseNLProcessor();
		for (String fieldName : dbNameMap.get("nlpFields").split(",")) {
			List<String> docs = proc.getPosDocs(this.getDocs(db,
					dbNameMap.get("crawlClass"), fieldName));
			this.saveDocs(docs, dbNameMap.get("nlpClass"), fieldName);
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void runCrawler() throws Exception {
		BasicCrawlController controller = new BasicCrawlController();
		controller.startCrawl(crawler,
				controller.getConfig(crawlStorageFolder), crawlURLSeeds);
	}

	@SuppressWarnings("unchecked")
	public void runHubPagerank() throws IOException {
		if (hubsPath == null)
			throw new FileNotFoundException(
					"String:hubsPath need to be specify");
		if (terminalsPath == null)
			throw new FileNotFoundException(
					"String:terminalsPath need to be specify");
		String[] hubs = this.getTerms(hubsPath, null);
		String[] terminals = this.getTerms(terminalsPath, null);
		List<String> docs = this.getDocs(db, dbNameMap.get("crawlClass"),
				dbNameMap.get("pagerankField"));

		TermDependencyProcessor depProc = new TermDependencyProcessor();
		Map<String, Map<String, Double>> maps = depProc.getDependencyMaps(hubs,
				terminals, docs, minDependencyDocsNumber);
		
		System.out.println(maps);
		PagerankProcessor pgProc = new PagerankProcessor();
		BufferedWriter writer = this.getBufferedWriter(exportDirectory
				+ missionName + "_" + dbNameMap.get("pagerankField")
				+ "_hubpagerank.dot");
		pgProc.exportGraph(writer,
				pgProc.getPagerankGraph(pgProc.getTermGraph(maps)));
	}

	/**
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void runTermPagerank() throws FileNotFoundException, IOException {
		BufferedReader reader = this.getBufferedReader(tfidfPath);
		TermDependencyProcessor depProc = new TermDependencyProcessor();
		PagerankProcessor pgProc = new PagerankProcessor();

		String[] terms = depProc.getTerms(reader);
		List<String> docs = this.getDocs(db, dbNameMap.get("crawlClass"),
				dbNameMap.get("pagerankField"));
		Map<String, Map<String, Double>> maps = depProc.getDependencyMaps(
				terms, docs, maxPagerankTermNumber);
		System.out.println("read " + terms.length + " terms");

		BufferedWriter writer = this.getBufferedWriter(exportDirectory
				+ missionName + "_" + dbNameMap.get("pagerankField")
				+ "_pagerank.dot");
		pgProc.exportGraph(writer,
				pgProc.getPagerankGraph(pgProc.getTermGraph(maps)));
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void runTermTfidf() throws IOException {
		TermTfidfProcessor proc = new TermTfidfProcessor();
		for (String fieldName : dbNameMap.get("nlpFields").split(",")) {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(exportDirectory + missionName + "_"
							+ fieldName + "_tfidf.txt")));
			List<String> posDocs = this.getDocs(db, dbNameMap.get("nlpClass"),
					fieldName);
			proc.export(writer, proc.getTfidfTerms(posDocs));
		}
	}

	/**
	 * 
	 * @param docs
	 * @param className
	 * @param fieldName
	 */
	public void saveDocs(List<String> docs, String className, String fieldName) {
		for (String doc : docs) {
			ODocument odoc = new ODocument(className);
			odoc.field(fieldName, doc);
			odoc.save();
		}
	}

	/**
	 * 
	 * @param missionStack
	 * @throws Exception
	 */
	public void start(Code[] missionStack) throws Exception {
		for (Code code : missionStack) {
			switch (code) {
			case REMOVE_DB:
				break;
			case CRAWL_WEB:
				System.out.println("start mission: crawl web");
				this.runCrawler();
				break;
			case CHINESE_NLP:
				System.out.println("start mission: Chinese NLP");
				this.runChineseNLP();
				break;
			case TFIDF:
				System.out.println("start mission: term tf-idf");
				this.runTermTfidf();
				break;
			case PAGERANK:
				System.out.println("start mission: general pagerank");
				this.runTermPagerank();
				break;
			case HUB_PAGERANK:
				System.out.println("start mission: hub pagerank");
				this.runHubPagerank();
				break;
			default:
				break;
			}
		}
		System.out.println("all missions completed");
	}
}
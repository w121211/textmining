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
		CHINESE_NLP, CRAWL_WEB, PAGERANK, REMOVE_DB, TFIDF,
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
	protected String missionName;
	protected String tfidfFileDirectory;

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

	/**
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void runTermPagerank() throws FileNotFoundException, IOException {
		if (tfidfFileDirectory == null)
			tfidfFileDirectory = exportDirectory + missionName + "_"
					+ dbNameMap.get("pagerankField") + "_tfidf.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(tfidfFileDirectory))));

		TermDependencyProcessor depProc = new TermDependencyProcessor();
		PagerankProcessor pgProc = new PagerankProcessor();

		String[] terms = depProc.getTerms(reader);
		List<String> docs = this.getDocs(db, dbNameMap.get("crawlClass"),
				dbNameMap.get("pagerankField"));
		Map<String, Map<String, Double>> maps = depProc.getDependencyMaps(
				terms, docs, maxPagerankTermNumber);
		System.out.println("read " + terms.length + " terms");

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(exportDirectory + missionName + "_"
						+ dbNameMap.get("pagerankField") + "_pagerank.dot")));
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
				System.out.println("start mission: term pagerank");
				this.runTermPagerank();
				break;
			default:
				break;
			}
		}
		System.out.println("all missions completed");
	}
}
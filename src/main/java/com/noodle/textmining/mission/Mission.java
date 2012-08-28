package com.noodle.textmining.mission;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.noodle.textmining.core.BasicCrawlController;
import com.noodle.textmining.core.TermDependencyProcessor;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public abstract class Mission {

	protected enum Code {
		CRAWL_WEB, CREATE_DB, DOC_POS, TERM_DEPENDENCY, TERM_GRAPH, TERM_TFIDF,
	}

	protected static String crawlDBClassName = "Crawl";
	protected static String crawlDBFieldName = "doc";
	protected static String crawlDomain;
	protected static MissionCrawler crawler;
	protected static Pattern crawlFilters = Pattern
			.compile(".*(\\.(css|js|bmp|gif|jpe?g"
					+ "|png|tiff?|mid|mp2|mp3|mp4"
					+ "|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	protected static String crawlStorageFolder = "crawl/";
	protected static String[] crawlURLSeeds;
	protected static String databaseDirectory;
	protected static ODatabaseDocumentTx db;
	protected static String docDBClassName; // Thread, Crawl
	protected static String docDBFieldName; // title, text
	protected static int maximumTerms = -1; // negative numbers refer no limits
	protected static int numberOfCrawlers = 1;

	// Term dependency
	protected static String termFileDirectory;

	public void close() {
		this.disconnectDatabase();
	}

	private void connectDatabase(String dbDirectory) throws OStorageException {
		this.db = new ODatabaseDocumentTx(dbDirectory);
		db.open("admin", "admin");
	}

	public void createDatabase(String dbDirectory) {
		db = new ODatabaseDocumentTx(dbDirectory).create();
	}

	private void disconnectDatabase() {
		this.db.close();
	}

	public void runCrawler() throws Exception {
		// MissionCrawler crawler = new MissionCrawler();
		BasicCrawlController controller = new BasicCrawlController();
		controller.startCrawl(crawler,
				controller.getConfig(crawlStorageFolder), numberOfCrawlers,
				crawlURLSeeds);

	}

	public void runTermDependencyProcessor() throws FileNotFoundException,
			IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new DataInputStream(new FileInputStream(
						this.termFileDirectory))));
		TermDependencyProcessor processor = new TermDependencyProcessor();

		String[] terms = processor.getTerms(reader);
		String[] docs = processor.getDocs(db, docDBClassName, docDBFieldName);
		System.out.println("read " + terms.length + " terms");
	}

	public void start(Code[] missionStack) throws Exception {
		try {
			this.connectDatabase(databaseDirectory);
		} catch (OStorageException e) {
			e.printStackTrace();
		}
		for (Code code : missionStack) {
			switch (code) {
			case CREATE_DB:
				System.out.println("start mission: create database");
				this.createDatabase(databaseDirectory);
				break;
			case CRAWL_WEB:
				System.out.println();
				this.runCrawler();
				break;
			case DOC_POS:
				break;
			case TERM_TFIDF:
				break;
			case TERM_DEPENDENCY:
				System.out.println("start mission: term dependency");
				this.runTermDependencyProcessor();
				break;
			case TERM_GRAPH:
				break;
			default:
				break;
			}
		}
	}
}
package com.noodle.textmining;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {
	
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" +
    		"|png|tiff?|mid|mp2|mp3|mp4" + 
    		"|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	public static ODatabaseDocumentTx db;
	private static Properties prop;
	
	public BasicCrawler () throws FileNotFoundException, IOException {
		// load a properties file
		prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		
		// Create a new database or connect to an existing one
		try {
			db = new ODatabaseDocumentTx (prop.getProperty("DB_DIR")).create();
		} catch(ODatabaseException e) {
			System.err.println("db existed, connecting to db");
			db = new ODatabaseDocumentTx (prop.getProperty("DB_DIR")).open("admin", "admin");
		}
	}
	
	@Override
    public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && 
				href.startsWith(prop.getProperty("DOMAIN"));
    }
	
	@Override
    public void visit(Page page) {
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		System.out.println("Docid: " + docid);
		
		if (url.startsWith(prop.getProperty("DOMAIN") + "topicdetail") &&
				page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			Document dom = Jsoup.parse(htmlParseData.getHtml());
			Elements elements = dom.select("div.single-post-content");
			
			String text = "";
			for (Element e : elements) {
				text += e.text() + "\n=====\n";
			}
			
			ODatabaseRecordThreadLocal.INSTANCE.set(db);
			ODocument doc = new ODocument("Doc");
			doc.field("url", url);
			doc.field("text", text);
			doc.save();
		}
	}
}
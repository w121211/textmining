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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.noodle.textmining.core.BasicCrawler;
import com.noodle.textmining.core.TermDependencyProcessor;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class ScientificAmericanMission extends Mission {

	// General
	private String databaseDirectory = "local:/Users/chi/git/textmining/databases/sci_A";
	
	// Term dependency
	private String termFileDirectory = "/Users/chi/git/textmining/databases/sci_A_3122/tkeywords.txt";
	protected String docDBClassName = "Thread";
	protected String docDBFieldName = "title";
	protected int maximumTerms = -1;	// negative numbers refer no limits

	private Code[] missionStack = { Code.TERM_DEPENDENCY, };

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		ScientificAmericanMission mission = new ScientificAmericanMission();
		mission.start();
		mission.close();
		System.out.println("all missions completed!");
	}

}

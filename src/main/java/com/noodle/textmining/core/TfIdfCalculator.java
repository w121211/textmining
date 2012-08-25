package com.noodle.textmining.core;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import taobe.tec.jcc.JChineseConvertor;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TfIdfCalculator {

	private static Properties prop;

	public static void main(String[] args) throws IOException {
		// load a properties file
		prop = new Properties();
		prop.load(new FileInputStream("config.properties"));

		// connect to database
		ODatabaseDocumentTx db;
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin", "admin");

		List<String> docs = new ArrayList<String>();
		for (ODocument doc : db.browseClass("Thread")) {
			docs.add((String) doc.field("pos_text_simp"));
		}

		// TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		// TokenizerFactory tokenizerFactory = new RegExTokenizerFactory("(\\S)+(#NN|#VV)");
		
		// Calculate TF-IDF
		TokenizerFactory tokenizerFactory = new RegExTokenizerFactory(
				"(\\S)+(#NN)");
		TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);
		for (String s : docs)
			tfIdf.handle(s);
		List<TfidfWord> keywords = new ArrayList<TfidfWord>();
		for (String term : tfIdf.termSet()) {
			// System.out.print(term + " ");
			keywords.add(new TfidfWord(
					term, tfIdf.docFrequency(term) * tfIdf.idf(term)));
		}

		// Write keywords to the file
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				"./log/keywords.txt")));
		writer.println("Term, Tf-idf");
		Collections.sort(keywords, new ValueComparator());
		Collections.reverse(keywords);
		for (TfidfWord w : keywords) {
			writer.printf("%s:%.2f\n", w.word, w.tfidf);
		}
		writer.close();

		System.out.println("Process completed");
	}
}

class TfidfWord {
	public String word;
	public Double tfidf;

	public TfidfWord(String word, Double tfidf) throws IOException {
		JChineseConvertor chineseConverter = JChineseConvertor.getInstance();
		this.word = chineseConverter.s2t(word);
		this.word = this.word.replaceAll("#NN|#VV", "");
		this.tfidf = tfidf;
	}
}

class ValueComparator implements Comparator<TfidfWord> {
	public int compare(TfidfWord a, TfidfWord b) {
		return a.tfidf.compareTo(b.tfidf);
	}
}

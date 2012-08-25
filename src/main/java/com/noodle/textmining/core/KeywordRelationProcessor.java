package com.noodle.textmining.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class KeywordRelationProcessor { 
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		KeywordRelationProcessor processor = new KeywordRelationProcessor();
		processor.run();
//		processor.test();
	}
	
	public void test() throws FileNotFoundException, IOException {
		
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		ODatabaseDocumentTx db;
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin", "admin");
		
		ODocument doc = new ODocument("Test");
		Map<String, Double> t1 = new HashMap<String, Double>();
		t1.put("t2", 0.2);
		t1.put("t3", 0.3);

		doc.field("term", t1);
		doc.save();
		
		for (ODocument d : db.browseClass("Test")) {
			System.out.print(d.fieldType("term"));
			System.out.println(d.field("term"));
			Map<String, Double> m = d.field("term");
			System.out.println(m.get("t2"));
			System.out.println(m.get("t3"));
		}
		
		db.close();
	}
	
	public void run() throws FileNotFoundException, IOException {
		// load a properties file
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));

		// connect to database
		ODatabaseDocumentTx db;
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin", "admin");
		
		// read terms from file
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(
						"/Users/chi/Downloads/udn-tkeywords-selected.txt"))));
		List<String> list = new ArrayList<String>();
		String str;
		while ((str = reader.readLine()) != null) {
//			if (str.contains("*"))
			if (str.indexOf(":") > 0) {
				String s = str.substring(0, str.indexOf(":"));
				if (s.length() > 1 && !s.contains("/"))
					list.add(s);
			}
		}
		String[] terms = list.toArray(new String[list.size()]);
		
		// read docs from database
		list = new ArrayList<String>();
		for (ODocument doc : db.browseClass("Thread"))
			list.add((String) doc.field("title"));
		String[] docs = list.toArray(new String[list.size()]);
		
		// calculate the relation matrix
//		double[][] matrix = this.getRelationMatrix(docs, terms);
		
		// out
//		PrintWriter writer = new PrintWriter(new BufferedWriter(
//				new OutputStreamWriter(
//				new FileOutputStream("log/relation_matrix.csv"), "UTF-8")));
//		this.out(writer, matrix, terms, terms);
		
		// save to database
		for (Map.Entry<String, Map<String, Double>> e : 
			getRelationMap(docs, terms).entrySet()) {
			ODocument doc = new ODocument("Term");
			
			doc.field("term", e.getKey());
		    doc.field("term_map", e.getValue());
		    doc.save();
		}
		db.close();
		
		System.out.println("done!");
	}
	
	/*
	 * TODO bug
	 * terms: 專家, 家 cause to count twice 
	 */
	public Map<String, Map<String, Double>> getRelationMap(
			String[] docs, String[] terms) {
		
		Map<String, Map<String, Double>> maps = 
				new HashMap<String, Map<String, Double>>();
		for (int i = 0; i < terms.length; i++) {
			Map<String, Double> map  = new HashMap<String, Double>();
			for (int j = 0; j < terms.length; j++) {
				if (terms[i].equals(terms[j])) continue;
				
				double countA = 0.0D;
				double countB = 0.0D;
				for (String doc : docs) {
					if (doc.contains(terms[i])) {
						countA++;
						doc.replace(terms[i], "");
						if (doc.contains(terms[j])) countB++;
					}
				}
				if (countA > 0 && countB > 0)
					map.put(terms[j], countB / countA);
			}
			if (map.size() > 0)
				maps.put(terms[i], map);
			System.out.println("term:" + i);
		}
		
		System.out.println(maps);
		return maps;
	}
	
	public double[][] getRelationMatrix(String[] docs, String[] terms) {
		double[][] matrix = new double[terms.length][terms.length];
		for (int i = 0; i < terms.length; i++) {
			for (int j = 0; j < terms.length; j++) {
				if (terms[i].equals(terms[j])) {
					matrix[j][i] = 0.0D;
					continue;
				}
				
				double countA = 0.0D;
				double countB = 0.0D;
				for (String doc : docs) {
					if (doc.contains(terms[i])) {
						countA++;
						if (doc.contains(terms[j])) countB++;
					}
				}
				if (countA == 0)
					matrix[j][i] = 0.0D;
				else
					matrix[j][i] = countB / countA;
			}
		}
		return matrix;
	}
	
	public void out(PrintWriter writer, double[][] matrix,
			String[] colNames, String[] rowNames) {
		writer.print(",");
		for (int i = 0; i < colNames.length; i++) {
			writer.print(rowNames[i] + ",");
		}
		writer.println();
		for (int i = 0; i < rowNames.length; i++) {
			writer.print(rowNames[i] + ",");
			for (int j = 0; j < colNames.length; j++) {
				writer.printf("%.4f,", matrix[i][j]);
			}
			writer.println();
		}
	}
}

package com.noodle.textmining.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TermDependencyProcessor {

	/**
	 * 
	 * @param writer
	 * @param matrix
	 * @param colNames
	 * @param rowNames
	 */
	public void export(PrintWriter writer, double[][] matrix,
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

	/**
	 * get dependency(termA->termB). Dependency(termA->termB) = number of docs
	 * contain (termA & termB) / number of docs contain (termA). The probability
	 * of termB exist when termA exist.
	 * 
	 * @param docsContainsTermA
	 * @param termB
	 * @return dependency(termA->termB).
	 */
	private double getDependency(List<String> docsContainsTermA, String termB) {
		double countA = docsContainsTermA.size();
		double countB = 0D;
		for (String doc : docsContainsTermA) {
			if (doc.contains(termB))
				countB++;
		}
		if (countA > 0 && countB > 0)
			return countB / countA;
		return 0D;
	}
	
	/**
	 * get dependency(termA->termB). Dependency(termA->termB) = number of docs
	 * contain (termA & termB) / number of docs contain (termA). The probability
	 * of termB exist when termA exist.
	 * 
	 * @param docsContainsTermA
	 * @param termB
	 * @return dependency(termA->termB).
	 */
	public double getDependency(List<String> docsContainsTermA, String termB, int minDocsRequire) {
		if (minDocsRequire == 0)
			return getDependency(docsContainsTermA, termB);
		double weight = (double) docsContainsTermA.size() / (double) minDocsRequire;
		if (weight > 1.0)
			weight = 1.0;
		return weight * getDependency(docsContainsTermA, termB);
	}

	/**
	 * Compute dependency map with given hubs and terminals. The return map will
	 * only contain dependency(terminal->hub) but not dependency(hub->terminal),
	 * (terminal->terminal), nor (hub->hub)
	 * 
	 * @param hubs
	 * @param terminals
	 * @param docs
	 * @return dependency(terminal->hub) map
	 */
	public Map<String, Map<String, Double>> getDependencyMaps(String[] hubs,
			String[] terminals, List<String> docs, int minDocsRequire) {
		Map<String, Map<String, Double>> maps = new HashMap<String, Map<String, Double>>();
		for (String terminal : terminals) {
			Map<String, Double> map = new HashMap<String, Double>();
			List<String> docsContainsTerminal = this.getDocsContainsTerm(terminal,
					docs);
			for (String hub: hubs) {
				double dependency = this.getDependency(docsContainsTerminal,
						hub, minDocsRequire);
				if (dependency > 0)
					map.put(hub, dependency);
			}
			if (map.size() > 0) {
				maps.put(terminal, map);
			}
			System.out.println("term:" + terminal);
		}
		return maps;
	}

	/**
	 * 
	 * @param terms
	 * @param docs
	 * @param maxTermNumber
	 * @return
	 */
	public Map<String, Map<String, Double>> getDependencyMaps(String[] terms,
			List<String> docs, int maxTermNumber) {
		if (terms.length < maxTermNumber || maxTermNumber < 0)
			maxTermNumber = terms.length;
		Map<String, Map<String, Double>> maps = new HashMap<String, Map<String, Double>>();
		for (int a = 0; a < maxTermNumber; a++) {
			Map<String, Double> map = new HashMap<String, Double>();

			List<String> docsContainsTermA = this.getDocsContainsTerm(terms[a],
					docs);
			for (int b = 0; b < maxTermNumber; b++) {
				if (terms[a].equals(terms[b]))
					continue;
				double dependency = this.getDependency(docsContainsTermA,
						terms[b]);
				if (dependency > 0)
					map.put(terms[b], dependency);
			}
			if (map.size() > 0) {
				maps.put(terms[a], map);
			}
			System.out.println("term:" + a);
		}
		return maps;
	}

	/**
	 * 
	 * @param docs
	 * @param terms
	 * @return
	 */
	public double[][] getDependencyMatrix(String[] docs, String[] terms) {
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
						if (doc.contains(terms[j]))
							countB++;
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

	/**
	 * 
	 * @param term
	 * @param allDocs
	 * @return docs that contains the given term, however the given term will be
	 *         removed from the doc
	 */
	public List<String> getDocsContainsTerm(String term, List<String> allDocs) {
		List<String> docs = new ArrayList<String>();
		for (String doc : allDocs) {
			if (doc.contains(term)) {
				doc.replace(term, "");
				docs.add(doc);
			}
		}
		return docs;
	}

	/**
	 * 
	 * @param reader
	 * @return terms
	 * @throws IOException
	 */
	public String[] getTerms(BufferedReader reader) throws IOException {
		List<String> list = new ArrayList<String>();
		String str;
		while ((str = reader.readLine()) != null) {
			if (str.indexOf(":") > 0) {
				String s = str.substring(0, str.indexOf(":"));
				if (s.length() > 1 && s.matches("[\\S&&[^╱，.]]{2,}?"))
					list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}
}

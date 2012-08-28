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
				if (s.length() > 1 && s.matches("[\\S&&[^╱]]{2,}?"))
					list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * TODO bug terms: 專家, 家 cause to count twice
	 * 
	 * @param docs
	 * @param terms
	 * @return
	 */
	public Map<String, Map<String, Double>> getRelationMap(String[] docs,
			String[] terms) {
		Map<String, Map<String, Double>> maps = new HashMap<String, Map<String, Double>>();
		for (int i = 0; i < terms.length; i++) {
			Map<String, Double> map = new HashMap<String, Double>();

			for (int j = 0; j < terms.length; j++) {
				if (terms[i].equals(terms[j]))
					continue;

				double countA = 0.0D;
				double countB = 0.0D;
				for (String doc : docs) {
					if (doc.contains(terms[i])) {
						countA++;
						doc.replace(terms[i], "");
						if (doc.contains(terms[j]))
							countB++;
					}
				}
				if (countA > 0 && countB > 0)
					map.put(terms[j], countB / countA);
			}
			if (map.size() > 0)
				maps.put(terms[i], map);
			System.out.println("term:" + i);
		}
		return maps;
	}

	/**
	 * 
	 * @param db
	 * @param className
	 * @param fieldName
	 * @return docs
	 */
	public String[] getDocs(ODatabaseDocumentTx db, String className, String fieldName) {
		List<String> list = new ArrayList<String>();
		for (ODocument doc : db.browseClass(className))
			list.add((String) doc.field(fieldName));
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * 
	 * @param docs
	 * @param terms
	 * @return
	 */
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
	 * @param writer
	 * @param matrix
	 * @param colNames
	 * @param rowNames
	 */
	public void export(PrintWriter writer, double[][] matrix, String[] colNames,
			String[] rowNames) {
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

package com.noodle.textmining.core;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jgrapht.Graph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class PagerankProcessor {

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		PagerankProcessor app = new PagerankProcessor();
		app.run();
		System.out.println("done!");
	}

	public void exportGraph(Writer writer,
			final Graph<TermVertex, DefaultWeightedEdge> g)
			throws FileNotFoundException {
		ComponentAttributeProvider<TermVertex> vertexAttributeProvider = new ComponentAttributeProvider<TermVertex>() {
			public Map<String, String> getComponentAttributes(TermVertex v) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("label", v.getTerm());
				map.put("depend_pagerank",
						String.format("%.4f", v.getPagerank()));
				return map;
			}
		};
		ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = new ComponentAttributeProvider<DefaultWeightedEdge>() {
			public Map<String, String> getComponentAttributes(
					DefaultWeightedEdge e) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("label", String.format("%.6f", g.getEdgeWeight(e)));
				return map;
			}
		};
		DOTExporter<TermVertex, DefaultWeightedEdge> exporter = new DOTExporter<TermVertex, DefaultWeightedEdge>(
				new IntegerNameProvider<TermVertex>(), null, null,
				vertexAttributeProvider, edgeAttributeProvider);
		exporter.export(writer, g);
	}

	@SuppressWarnings("rawtypes")
	public DefaultDirectedWeightedGraph getPagerankGraph(
			DefaultDirectedWeightedGraph<TermVertex, DefaultWeightedEdge> g) {
		int n = g.vertexSet().size();
		double initPR = 1.0 / n;
		for (TermVertex i : g.vertexSet()) {
			i.setPagerank(initPR);
		}

		int iteration = 100;
		double damping = 0.85;
		while (iteration-- >= 0) {
			System.out.println("pagerank left iteration:" + iteration);
			for (TermVertex i : g.vertexSet()) {
				double sigma = 0D;
				for (DefaultWeightedEdge ej : g.incomingEdgesOf(i)) {
					TermVertex j = g.getEdgeSource(ej);
					double dependenceSum = 0D;
					for (DefaultWeightedEdge ek : g.outgoingEdgesOf(j))
						/* general pagerank */
						// sigma += j.getPagerank() / g.outDegreeOf(j);
						/* dependency pagerank */
						dependenceSum += g.getEdgeWeight(ek);
						sigma += j.getPagerank()
							* (g.getEdgeWeight(ej) / dependenceSum);
				}
				double pr = (1d - damping) / n + damping * sigma;
				i.setTempPagerank(pr);
			}
			for (TermVertex i : g.vertexSet())
				i.setPagerank(i.getTempPagerank());
		}
		return g;
	}

	public DefaultDirectedWeightedGraph<TermVertex, DefaultWeightedEdge> getTermGraph(
			Map<String, Map<String, Double>> termRelationMap) {

		// Create relation graph
		Map<String, TermVertex> map = new HashMap<String, TermVertex>();
		DefaultDirectedWeightedGraph<TermVertex, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph<TermVertex, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (String term : termRelationMap.keySet()) {
			TermVertex v = new TermVertex(term);
			g.addVertex(v);
			map.put(v.getTerm(), v);
		}
		for (Entry<String, Map<String, Double>> e1 : termRelationMap.entrySet()) {
			for (Entry<String, Double> e2 : e1.getValue().entrySet()) {
				DefaultWeightedEdge e = g.addEdge(map.get(e1.getKey()),
						map.get(e2.getKey()));
				g.setEdgeWeight(e, e2.getValue());
			}
		}
		return g;
	}

	@SuppressWarnings("unchecked")
	public void run() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		ODatabaseDocumentTx db;
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin",
				"admin");

		// Read dependency map from database
		Map<String, Map<String, Double>> maps = new HashMap<String, Map<String, Double>>();
		for (ODocument doc : db.browseClass("Term")) {
			String term = doc.field("term");
			Map<String, Double> map = doc.field("term_map");
			maps.put(term, map);
		}

		DefaultDirectedWeightedGraph<TermVertex, DefaultWeightedEdge> g = this
				.getTermGraph(maps);
		g = this.getPagerankGraph(g);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(prop.getProperty("EXPORT_DIR")
						+ "relation_graph.dot")));
		this.exportGraph(writer, g);
	}
}

class TermVertex {
	private double pagerank;
	private double tempPagerank;
	private String term;

	public TermVertex(String term) {
		this.term = term;
		this.pagerank = 0D;
		this.tempPagerank = 0D;
	}

	public double getPagerank() {
		return pagerank;
	}

	public double getTempPagerank() {
		return tempPagerank;
	}

	public String getTerm() {
		return term;
	}

	public void setPagerank(double pagerank) {
		this.pagerank = pagerank;
	}

	public void setTempPagerank(double tempPagerank) {
		this.tempPagerank = tempPagerank;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String toString() {
		return String.format("%s[%.6f]", term, pagerank);
	}
}

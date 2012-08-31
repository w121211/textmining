package com.noodle.textmining.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import taobe.tec.jcc.JChineseConvertor;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class TermTfidfProcessor {

	public List<TfidfTerm> getTfidfTerms(List<String> docs) throws IOException {
		TokenizerFactory tokenizerFactory = new RegExTokenizerFactory(
				"(\\S)+(#NN)");
		TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);
		for (String s : docs)
			tfIdf.handle(s);
		List<TfidfTerm> terms = new ArrayList<TfidfTerm>();
		for (String term : tfIdf.termSet()) {
			terms.add(new TfidfTerm(term, tfIdf.docFrequency(term)
					* tfIdf.idf(term)));
		}
		Collections.sort(terms, new ValueComparator());
		Collections.reverse(terms);
		return terms;
	}

	public void export(BufferedWriter writer, List<TfidfTerm> terms)
			throws IOException {
		PrintWriter pwriter = new PrintWriter(writer);
		pwriter.println("Term, Tf-idf");
		for (TfidfTerm t : terms) {
			pwriter.printf("%s:%.4f\n", t.term, t.tfidf);
		}
		writer.close();
	}

}

class TfidfTerm {
	public Double tfidf;
	public String term;

	public TfidfTerm(String word, Double tfidf) throws IOException {
		JChineseConvertor chineseConverter = JChineseConvertor.getInstance();
		this.term = chineseConverter.s2t(word);
		this.term = this.term.replaceAll("#NN|#VV", "");
		this.tfidf = tfidf;
	}
}

class ValueComparator implements Comparator<TfidfTerm> {
	public int compare(TfidfTerm a, TfidfTerm b) {
		return a.tfidf.compareTo(b.tfidf);
	}
}

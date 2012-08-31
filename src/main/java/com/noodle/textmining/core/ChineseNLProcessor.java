package com.noodle.textmining.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import taobe.tec.jcc.JChineseConvertor;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ChineseNLProcessor {

	public JChineseConvertor chineseConverter;
	public CRFClassifier<CoreLabel> classifier;
	public Dictionary dic;
	public MaxentTagger tagger;

	public ChineseNLProcessor() throws IOException, ClassNotFoundException {
		/* Setup MMSeg segmenter */
		System.setProperty("mmseg.dic.path", "./data/");
		dic = Dictionary.getInstance();

		/* Setup stanford segmenter */
		Properties props = new Properties();
		props.setProperty("sighanCorporaDict", "data");
		props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
		props.setProperty("inputEncoding", "UTF-8");
		props.setProperty("sighanPostProcessing", "true");
		classifier = new CRFClassifier<CoreLabel>(props);
		classifier.loadClassifierNoExceptions("data/ctb.gz", props);
		classifier.flags.setProperties(props);

		/* Setup stanford POS tagger */
		tagger = new MaxentTagger("models/chinese.tagger");

		/* Setup chinese convertor */
		chineseConverter = JChineseConvertor.getInstance();
	}
	
	/**
	 * 
	 * @param docs
	 * @return
	 */
	public List<String> getUniqueDocs(List<String> docs) {
		Set<String> set = new HashSet<String>(docs);
		docs.clear();
		docs.addAll(set);
		return docs;
	}
	
	/**
	 * 
	 * @param docs
	 * @return
	 */
	public List<String> getPosDocs(List<String> docs) {
		List<String> uniDocs = this.getUniqueDocs(docs);
		System.out.printf("Chinese nlp found %d duplicates\n", docs.size() - uniDocs.size());
		List<String> posDocs = new ArrayList<String>();
		int i = 0;
		for (String doc : uniDocs) {
			System.out.println("Chinese nlp runing:" + i++);
			/* Transfer text to simplified Chinese */
			String simpDoc = chineseConverter.t2s(doc);

			/* Segmentation */
			List<String> segList = classifier.segmentString(simpDoc);

			/* POS tagging */
			String segDoc = "";
			for (String s : segList)
				if (!s.isEmpty())
					segDoc += s + " ";
			posDocs.add(tagger.tagString(segDoc));
		}
		return posDocs;
	}

	public String segWords(String txt, String wordSplit) throws IOException {
		Reader input = new StringReader(txt);
		StringBuilder sb = new StringBuilder();
		Seg seg = new ComplexSeg(dic);
		MMSeg mmSeg = new MMSeg(input, seg);
		Word word = null;
		boolean first = true;
		while ((word = mmSeg.next()) != null) {
			if (!first)
				sb.append(wordSplit);
			String w = word.getString();
			sb.append(w);
			first = false;
		}
		return sb.toString();
	}
}

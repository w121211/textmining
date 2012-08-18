package com.noodle.textmining;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

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

public class ProcessChinese {

	private static Properties prop;
	public static ODatabaseDocumentTx db;
	public Dictionary dic;
	public MaxentTagger tagger;
	public CRFClassifier<CoreLabel> classifier;
	public JChineseConvertor chineseConverter;

	public ProcessChinese() throws Exception {
		// load a properties file
		prop = new Properties();
		prop.load(new FileInputStream("config.properties"));

		// Setup MMSeg segmenter
		System.setProperty("mmseg.dic.path", "./data/");
		dic = Dictionary.getInstance();

		// Setup database
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin", "admin");

		// Setup stanford segmenter
		Properties props = new Properties();
		props.setProperty("sighanCorporaDict", "data");
		props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
		props.setProperty("inputEncoding", "UTF-8");
		props.setProperty("sighanPostProcessing", "true");
		classifier = new CRFClassifier<CoreLabel>(props);
		classifier.loadClassifierNoExceptions("data/ctb.gz", props);
		classifier.flags.setProperties(props);

		// Setup stanford POS tagger
		tagger = new MaxentTagger("models/chinese.tagger");

		// Setup chinese convertor
		chineseConverter = JChineseConvertor.getInstance();
	}

	public static void main(String[] args) throws Exception {
		new ProcessChinese().run();
		System.out.println("Text process completed");
		db.close();
	}

	public void run() throws IOException {
		for (ODocument doc : db.browseClass("Thread")) {
			System.out.println(doc.getIdentity());
			String text = doc.field("text"); // retrieve text from database

			// transfer text to simplified Chinese
			text = chineseConverter.t2s(text);

			// MMSeg segmentation
			// String segText = segWords(text, " ");

			// stanford segmentation
			List<String> segList = classifier.segmentString(text);
			
			// POS tagging
			String segText = "";
			for (String s : segList)
				if (!s.isEmpty())
					segText += s + " ";
			String posText = tagger.tagString(segText);

			// Save to database
			doc.field("text_simp", text);
			doc.field("seg_text_simp", segText);
			doc.field("pos_text_simp", posText);
			doc.save();
		}
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

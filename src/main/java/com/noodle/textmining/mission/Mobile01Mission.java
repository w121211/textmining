/**
 * 
 */
package com.noodle.textmining.mission;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

final class Mobile01Mission extends Mission {

	private Mobile01Mission(String missionName, String dbDirectory) {
		super(missionName, dbDirectory);
		crawler = new Mobile01Crawler();
	}

	public static class Mobile01Crawler extends MissionCrawler {

		@Override
		public boolean shouldVisit(WebURL url) {
			String href = url.getURL().toLowerCase();
			return true;
//			return !crawlFilters.matcher(href).matches()
//					&& href.startsWith(Mission.crawlDomain);
		}

		@Override
		public void visit(Page page) {
			int docid = page.getWebURL().getDocid();
			String url = page.getWebURL().getURL();
			System.out.println("Docid: " + docid);

			if (url.startsWith(Mission.crawlDomain)
					&& page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page
						.getParseData();
				Document dom = Jsoup.parse(htmlParseData.getHtml());
				Elements elements = dom.select("div.single-post-content");

				String text = "";
				for (Element e : elements)
					text += e.text() + "\n";

				ODatabaseRecordThreadLocal.INSTANCE.set(db);
				ODocument doc = new ODocument();
				doc.field("url", url);
//				doc.field("title", title);
				doc.field("text", text);
				doc.save();
			}
		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Mission.crawlDomain = "http://www.mobile01.com/";
		String[] urlSeeds = { "http://www.mobile01.com/", };
		Mission.crawlURLSeeds = urlSeeds;
		
		
		
		Code[] missionStack = { 
				Code.CRAWL_WEB, 
//				Code.CHINESE_NLP,
//				Code.TFIDF,
//				Code.PAGERANK,
		};
		Mission mission = new Mobile01Mission("mobile01", "local:/Users/chi/git/textmining/databases/mobile01");
		mission.start(missionStack);
//		Mission.crawler = new Mobile01Crawler();
		mission.close();
	}
}
/**
 * 
 */
package com.noodle.textmining.mission;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.record.impl.ODocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author chi
 * 
 */
public class Mobile01Mission extends Mission {

	public static class Mobile01Crawler extends MissionCrawler {

		@Override
		public boolean shouldVisit(WebURL url) {
			String href = url.getURL().toLowerCase();
			return !crawlFilters.matcher(href).matches()
					&& href.startsWith(crawlDomain);
		}

		@Override
		public void visit(Page page) {
			int docid = page.getWebURL().getDocid();
			String url = page.getWebURL().getURL();
			System.out.println("Docid: " + docid);

			if (url.startsWith(crawlDomain)
					&& page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page
						.getParseData();
				Document dom = Jsoup.parse(htmlParseData.getHtml());
				Elements elements = dom.select("div.single-post-content");

				String doc = "";
				for (Element e : elements) {
					doc += e.text() + "\n=====\n";
				}

				ODatabaseRecordThreadLocal.INSTANCE.set(db);
				ODocument odoc = new ODocument(crawlDBClassName);
				odoc.field("url", url);
				odoc.field(crawlDBFieldName, doc);
				odoc.save();
			}
		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Mission.databaseDirectory = "local:/Users/chi/git/textmining/databases/mobile01";
		Mission.crawlDomain = "http://www.mobile01.com/";
		String[] urlSeeds = { "http://www.mobile01.com/", };
		Mission.crawlURLSeeds = urlSeeds;
		Mission.crawler = new Mobile01Crawler();
		
		Code[] missionStack = { 
				Code.CRAWL_WEB, 
		};
		Mission mission = new Mobile01Mission();
		mission.start(missionStack);
		mission.close();
	}
}
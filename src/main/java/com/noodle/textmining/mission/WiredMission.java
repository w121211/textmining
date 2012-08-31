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

final class WiredMission extends Mission {

	public static class MissionCrawlerImpl extends MissionCrawler {

		@Override
		public boolean shouldVisit(WebURL url) {
			String href = url.getURL().toLowerCase();
			return !Mission.crawlFilters.matcher(href).matches()
					&& href.startsWith(Mission.crawlDomain);
		}

		@Override
		public void visit(Page page) {
			int docid = page.getWebURL().getDocid();
			String url = page.getWebURL().getURL();
			System.out.println("Docid: " + docid);

			// *** Filter urls which need to be processed ************
			if (url.startsWith(Mission.crawlDomain)
					&& url.endsWith("index.html")
					&& page.getParseData() instanceof HtmlParseData) {
				// *************************************************

				HtmlParseData htmlParseData = (HtmlParseData) page
						.getParseData();
				Document dom = Jsoup.parse(htmlParseData.getHtml());

				// *** Scratch elements inside a page **************
				Elements titles = dom.select("h2.news_h2");
				Elements texts = dom.select("div.news_post_content");
				String title = titles.text();
				String text = texts.text();
				// *************************************************

				if (titles.size() == 1 && texts.size() == 1) {
					System.out.println("title:" + title);
					System.out.println("text:" + text);
					String crawlClass = Mission.dbNameMap.get("crawlClass");
					String[] crawlFields = Mission.dbNameMap.get("crawlFields")
							.split(",");
					// *** Save to database *************************
					ODatabaseRecordThreadLocal.INSTANCE.set(Mission.db);
					ODocument doc = new ODocument(crawlClass);
					doc.field(crawlFields[0], url);
					doc.field(crawlFields[1], title);
					doc.field(crawlFields[2], text);
					doc.save();
					// *************************************************
				}
			}
		}
	}

	private WiredMission(String missionName, String dbDirectory) {
		super(missionName, dbDirectory);
		// *** (Optional) Change the names for different records ***
		// *** "index"[DO NOT CHANGE], "name1,name2,name3...." *****
		dbNameMap.put("crawlFields", "url,title,text");
		dbNameMap.put("nlpFields", "title,text");
		dbNameMap.put("pagerankField", "text");
		// *********************************************
		crawler = new MissionCrawlerImpl();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// **** Add missions to execute ***************************
		Code[] missionStack = { 
//				Code.CRAWL_WEB, 	
//				Code.CHINESE_NLP, 
//				Code.TFIDF,
				Code.PAGERANK,
		};
		// *********************************************************

		// *** Initialize a mission ***********
		Mission.crawlDomain = "http://wired.tw/";
		Mission mission = new WiredMission("wired",
				"local:/Users/chi/git/textmining/databases/wired");
		mission.maxPagerankTermNumber = 2000;
		String[] urlSeeds = { "http://wired.tw/", };
		mission.crawlURLSeeds = urlSeeds;
		// *********************************************************

		mission.start(missionStack);
		mission.close();
	}
}

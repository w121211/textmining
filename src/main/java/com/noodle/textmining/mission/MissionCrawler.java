package com.noodle.textmining.mission;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public abstract class MissionCrawler extends WebCrawler {

	public abstract boolean shouldVisit(WebURL url);

	public abstract void visit(Page page);

}
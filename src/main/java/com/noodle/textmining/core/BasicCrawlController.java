package com.noodle.textmining.core;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import com.noodle.textmining.mission.MissionCrawler;

public class BasicCrawlController {

	/**
	 * 
	 * @param crawlStorageFolder
	 *            crawlStorageFolder is a folder where intermediate crawl data
	 *            is stored.
	 * 
	 * @return
	 */
	public CrawlConfig getConfig(String crawlStorageFolder) {
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);

		/*
		 * Be polite: Make sure that we don't send more than 1 request per
		 * second (1000 milliseconds between requests).
		 */
		config.setPolitenessDelay(1000);

		/*
		 * You can set the maximum crawl depth here. The default value is -1 for
		 * unlimited depth
		 */
		config.setMaxDepthOfCrawling(-1);

		/*
		 * You can set the maximum number of pages to crawl. The default value
		 * is -1 for unlimited number of pages
		 */
		config.setMaxPagesToFetch(-1);

		/*
		 * Do you need to set a proxy? If so, you can use:
		 * config.setProxyHost("proxyserver.example.com");
		 * config.setProxyPort(8080);
		 * 
		 * If your proxy also needs authentication:
		 * config.setProxyUsername(username); config.getProxyPassword(password);
		 */

		/*
		 * This config parameter can be used to set your crawl to be resumable
		 * (meaning that you can resume the crawl from a previously
		 * interrupted/crashed crawl). Note: if you enable resuming feature and
		 * want to start a fresh crawl, you need to delete the contents of
		 * rootFolder manually.
		 */
		config.setResumableCrawling(false);

		return config;
	}

	/**
	 * 
	 * @param crawler 
	 * @param <T>
	 * @param config
	 * @param numberOfCrawlers
	 *            numberOfCrawlers shows the number of concurrent threads that
	 *            should crawling.
	 * @param urlSeeds
	 * @throws Exception 
	 */
	public void startCrawl(MissionCrawler crawler, CrawlConfig config, int numberOfCrawlers,
			String[] urlSeeds) throws Exception {
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher,
				robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		for (String seed : urlSeeds)
			controller.addSeed(seed);

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		System.out.println(crawler.getClass());
		controller.start(crawler.getClass(), numberOfCrawlers);

		// Wait for 30 seconds
		Thread.sleep(30 * 1000);

		// Send the shutdown request and then wait for finishing
		controller.Shutdown();
		controller.waitUntilFinish();
		System.out.println("crawler shut down");
	}
}
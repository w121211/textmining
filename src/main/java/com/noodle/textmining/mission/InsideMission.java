package com.noodle.textmining.mission;

final class InsideMission extends Mission {

	public static void main(String[] args) throws Exception {
//		Mission.crawlDomain = "http://www.mobile01.com/";
//		String[] urlSeeds = { "http://www.mobile01.com/", };
//		Mission.crawlURLSeeds = urlSeeds;
//		Mission.crawler = new ScientificAmericanMission("local:/Users/chi/git/textmining/databases/sciAmer");
		Mission.termFileDirectory = "/Users/chi/git/textmining/databases/inside_6972/tkeywords.txt";
		
		Mission mission = new InsideMission("inside", "local:/Users/chi/git/textmining/databases/inside_copy");
		mission.dbNameMap.put("crawlClass", "Thread");
		mission.dbNameMap.put("docClass", "Doc");
		mission.dbNameMap.put("termField", "title");
		mission.maxTermNumber = 2000;
		
		Code[] missionStack = { 
//				Code.CRAWL_WEB, 
//				Code.CHINESE_NLP,
				Code.TERM_TFIDF,
//				Code.TERM_PAGERANK,
		};
		
		mission.start(missionStack);
		mission.close();
	}

	private InsideMission(String missionName, String dbDirectory) {
		super(missionName, dbDirectory);
	}

}
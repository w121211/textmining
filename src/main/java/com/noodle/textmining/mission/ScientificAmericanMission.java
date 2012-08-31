package com.noodle.textmining.mission;

final class ScientificAmericanMission extends Mission {

	public static void main(String[] args) throws Exception {
//		Mission.crawlDomain = "http://www.mobile01.com/";
//		String[] urlSeeds = { "http://www.mobile01.com/", };
//		Mission.crawlURLSeeds = urlSeeds;
//		Mission.crawler = new ScientificAmericanMission("local:/Users/chi/git/textmining/databases/sciAmer");
		Mission.termFileDirectory = "/Users/chi/git/textmining/databases/sci_A_3122/tkeywords.txt";
		Mission.maxTermNumber = 2000;
		Mission.dbNameMap.put("crawlClass", "Crawl");
		Mission.dbNameMap.put("docClass", "Thread");
		Mission.dbNameMap.put("termField", "text");
		
		Code[] missionStack = { 
//				Code.CRAWL_WEB, 
//				Code.CHINESE_NLP,
				Code.TERM_PAGERANK,
		};
		Mission mission = new ScientificAmericanMission("sci_american", "local:/Users/chi/git/textmining/databases/sci_a_copy");
		mission.start(missionStack);
		mission.close();
	}

	private ScientificAmericanMission(String missionName, String dbDirectory) {
		super(missionName, dbDirectory);
	}	

}
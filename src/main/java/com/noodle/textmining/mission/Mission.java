package com.noodle.textmining.mission;

public abstract class Mission {
	
	private String databaseDirectory;
	private String dataClassName;
	private String[] dataFieldName;
	private String crawlURL;
	
	public Mission(String databaseDirectory) {
		this.databaseDirectory = databaseDirectory;
	}
	
	public void connectDatabase() {
		
	}
	
	public void disconnectDatabase() {
		
	}
	
	public void runCrawler() {
		
	}
	
	public void crawlerShouldVist() {
		
	}
	
	public void crawlerShouldVisit() {
		
	}
	
	

}

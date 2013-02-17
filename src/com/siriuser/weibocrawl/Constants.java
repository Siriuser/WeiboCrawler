package com.siriuser.weibocrawl;


public interface Constants {
	
	public final static String SOLR_SERVER_URL = "http://10.10.7.157:8090/solr";
	
	//SOLR一次提交的最大数量
	public final static int SOLR_MAX_COMMIT_CNT = 1000;
	
	public final static String FIELD_ID = "id";
	public final static String FIELD_URL ="url";
	public final static String FIELD_CONTENT ="content";
	public final static String FIELD_TITLE ="title";
	public final static String FIELD_TSTAMP ="tstamp";
	public final static String FIELD_PUBLISHDATE ="publishedDate";
	public final static String FIELD_DIGEST ="digest";
	
	public final static String XPATH_FETCHER = "//fetcher";

}

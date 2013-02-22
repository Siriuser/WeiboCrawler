package com.siriuser.weibocrawl;


public interface Constants {
	
	public final static String SOLR_SERVER_URL = "http://localhost:8090/solr";
	
	//SOLR一次提交的最大数量
	public final static int SOLR_MAX_COMMIT_CNT = 1000;
	
	public final static String FIELD_ID = "id";
	public final static String FIELD_TREND = "2";
	public final static String FIELD_URL ="url";
	public final static String FIELD_CONTENT ="content";
	public final static String FIELD_TITLE ="title";
	public final static String FIELD_TSTAMP ="tstamp";
	public final static String FIELD_PUBLISHDATE ="publishedDate";
	public final static String FIELD_DIGEST ="digest";
	
	public final static String XPATH_FETCHER = "//fetcher";
	
	public final static int POSITIVE_NATURE = 1; // 正面新闻
	public final static int NEUTRAL_NATURE = 2; // 中性新闻
	public final static int NEGATIVE_NATURE = 4; // 负面新闻
	
	public static final String POS_WORDS_PATH = "conf/pos_words";
	public static final String NEG_WORDS_PATH = "conf/neg_words";
	public static final String REL_WORDS_PATH = "conf/ref_words";

}

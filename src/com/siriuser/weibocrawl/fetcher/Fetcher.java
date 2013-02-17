package com.siriuser.weibocrawl.fetcher;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import com.siriuser.weibocrawl.keywords.policy.KeyWordsPolicy;

public interface Fetcher {
	List<SolrInputDocument> fetch();
	void setPolicy(KeyWordsPolicy policy);
	void setDepth(int depth);
}

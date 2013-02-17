package com.siriuser.weibocrawl.fetcher;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public class FetcherThreadRunnable implements Runnable {
	
	List<SolrInputDocument> documents;
	Fetcher fetcher;
	
	public FetcherThreadRunnable(List<SolrInputDocument> documents, Fetcher fetcher){
		this.documents = documents;
		this.fetcher = fetcher;
	}

	@Override
	public void run() {
		
		documents.addAll(fetcher.fetch());

	}

}

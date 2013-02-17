package com.siriuser.weibocrawl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import com.siriuser.weibocrawl.fetcher.Fetcher;
import com.siriuser.weibocrawl.fetcher.FetcherThreadRunnable;
import com.siriuser.weibocrawl.fetcher.FetcherUtils;
import com.siriuser.weibocrawl.solr.SolrUtils;

public class Crawl {
	
	private static Set<String> crawledSet = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * 是否为添加过的URL
	 * @param url
	 * @return
	 */
	public static boolean isCrawledURL(String url){
		
		if(crawledSet.contains(url)){
			return true;
		} else{
			crawledSet.add(url);
			return false;
		}
		
	}

	public static void main(String[] args) {

		Crawl c = new Crawl();
		c.run();

	}

	public void run() {

		List<Fetcher> fetchers = FetcherUtils.getInstance().getFetchers();

		List<SolrInputDocument> documents = Collections.synchronizedList(new ArrayList<SolrInputDocument>());

		List<Thread> threads = new ArrayList<Thread>();

		try {

			for (Fetcher fetcher : fetchers) {

				Thread thread = new Thread(new FetcherThreadRunnable(documents,
						fetcher));
				thread.start();
				threads.add(thread);
				System.out.println(fetcher.getClass() + " is running");
			}

			for (Iterator<Thread> it = threads.iterator(); it.hasNext();) {
				((Thread) it.next()).join();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(documents.size());
		
		System.out.println("Start Solr Indexing :" + new Date());

		SolrUtils.getInstance().commitDocuments(documents);
		
		System.out.println("Solr Indexing Finished :" + new Date());

	}

}

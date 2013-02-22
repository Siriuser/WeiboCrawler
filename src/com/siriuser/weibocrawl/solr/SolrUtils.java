package com.siriuser.weibocrawl.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.siriuser.weibocrawl.Constants;
import com.siriuser.weibocrawl.analyser.TrendencyAnalyser;

public class SolrUtils {
	
	private static SolrUtils instance = null;
	
	private SolrUtils(){

		
	}
	
	public static SolrUtils getInstance(){
		
		if(instance == null){
			
			instance = new SolrUtils();
			
		}
		
		return instance;
	}
	
	private SolrServer server = null;

	
	/**
	 * 提交文档
	 * @param documents
	 */
	public void commitDocuments(List<SolrInputDocument> documents){
		
		if(server == null){
			try {
				server = new CommonsHttpSolrServer(Constants.SOLR_SERVER_URL);
				
			} catch (MalformedURLException e) {
				
				System.out.println(e.getMessage());
			}
		}
		
		if(server == null) return;
		
		int size = documents.size();	
		
		Date now = new Date((new Date()).getTime() + (long)3600*1000*8);
		
		for (SolrInputDocument solrInputDocument : documents) {
			
			solrInputDocument.addField(Constants.FIELD_TSTAMP, now);

			System.out.println("index url: " + solrInputDocument.getFieldValue(Constants.FIELD_URL));
		}
		
		if(size <= Constants.SOLR_MAX_COMMIT_CNT){
			
			try {
				server.add(documents);
				server.commit();

			} catch (SolrServerException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}		
			
		} else{
			
			int index = 0;
			
			while(index < size - 1){
				
				try {
					
					if(index + Constants.SOLR_MAX_COMMIT_CNT >= size){
						
						server.add(documents.subList(index, size));
						
					}else{
						
						server.add(documents.subList(index, index + Constants.SOLR_MAX_COMMIT_CNT - 1));
						
					}
					
					server.commit();
					
					index += Constants.SOLR_MAX_COMMIT_CNT;
					
				} catch (SolrServerException e) {
					
					System.out.println(e.getMessage());
					
				} catch (IOException e) {
					
					System.out.println(e.getMessage());
				}
				
			}

			
		}	
		
		
		
	}

	

}

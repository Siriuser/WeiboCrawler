package com.siriuser.weibocrawl.keywords;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class KeywordReader {

	private static KeywordReader instance;
	private List<String> keywords;

	private KeywordReader() {
		loadKeyWords();
	}

	public static KeywordReader getInstance() {

		if (instance == null) {
			instance = new KeywordReader();
		}

		return instance;

	}

	private void loadKeyWords() {

		keywords = new ArrayList<String>();

		InputStreamReader ir = null;
		BufferedReader br = null;
		
		try {
			
			ir = new InputStreamReader(new FileInputStream(
					"conf/keyword"), "utf-8");
			br = new BufferedReader(ir);
			
			String line = null;

			while ((line = br.readLine()) != null) {
				keywords.add(line);
			}
			
			
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			
		} catch (IOException e) {

			e.printStackTrace();
		} finally{
			
			try {
				
				if(br != null){
					br.close();
				}
				
				if(ir != null){
					ir.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}

	}
	
	public List<String> getKeywords() {
		return keywords;
	}

}

package com.siriuser.weibocrawl.analyser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siriuser.weibocrawl.Constants;


/**
 * 加载词汇
 * 
 * @author William Xu
 */

public class TrendencyWordsLoader {

	private Map<String, Integer> negWordMap;
	private Map<String, Integer> posWordMap;
	private List<String> refWordList;

	public TrendencyWordsLoader() {
		loadWords();
	}

	private void loadWords() {

		negWordMap = new HashMap<String, Integer>();
		posWordMap = new HashMap<String, Integer>();
		refWordList = new ArrayList<String>();

		try {

			// FileReader fr = new
			// FileReader(this.getClass().getClassLoader().getResource(PSMSConstants.NEG_WORDS_PATH).getFile());

			InputStreamReader ir = new InputStreamReader(
					new FileInputStream(Constants.NEG_WORDS_PATH), "utf-8");

			BufferedReader br = new BufferedReader(ir);

			String line = null;

			while ((line = br.readLine()) != null) {

				String[] words = line.split("#");
				negWordMap.put(words[0], Integer.parseInt(words[1]));
			}

			ir = new InputStreamReader(new FileInputStream(Constants.POS_WORDS_PATH), "utf-8");
			br = new BufferedReader(ir);
			line = null;

			while ((line = br.readLine()) != null) {

				String[] words = line.split("#");
				posWordMap.put(words[0], Integer.parseInt(words[1]));
			}

			ir = new InputStreamReader(new FileInputStream(Constants.REL_WORDS_PATH), "utf-8");
			br = new BufferedReader(ir);

			line = null;

			while ((line = br.readLine()) != null) {
				refWordList.add(line);
			}

			br.close();
			ir.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Integer> getNegWordMap() {
		return negWordMap;
	}

	public Map<String, Integer> getPosWordMap() {
		return posWordMap;
	}

	public List<String> getRefWordList() {
		return refWordList;
	}
}

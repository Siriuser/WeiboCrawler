package com.siriuser.weibocrawl.analyser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Word;
import com.siriuser.weibocrawl.Constants;

/**
 * @author William Xu
 *
 */
public class TrendencyAnalyser {

	private static TrendencyAnalyser instance;
	
	private TrendencyWordsLoader wordLoader;

	protected static final Dictionary dic = Dictionary.getInstance();
	protected static final ComplexSeg seg = new ComplexSeg(dic);

	/**
	 * 正序阈值
	 */
	private final int PS_THRESHOLD = 100;

	/**
	 * 逆序阈值
	 */
	private final int NS_THRESHOLD = 50;

	/**
	 * 整片文章分词Map
	 */
	private Map<String, List<Word>> segments = null;
	private List<Word> negs = null;
	private List<Word> poses = null;
	private List<Word> rels = null;
	
	
	private TrendencyAnalyser(){
		wordLoader = new TrendencyWordsLoader();
	}
	
	public static TrendencyAnalyser getInstance(){
		
		if(instance == null){
			
			instance = new TrendencyAnalyser();
			
		}
		
		return instance;
		
	}

	public int analyzeTrendency(String title, String content) {

		try {

			boolean flag = isRelTitle(title);

			if (flag) {

				int titleTendency = getTitleTrendency();

				if (titleTendency < 0) {
					return Constants.NEGATIVE_NATURE;
				} else if (titleTendency > 0) {
					return Constants.POSITIVE_NATURE;
				}
			}

			clearAll();

			initSegmentsMap(new StringReader(title + " " + content));
			
//			segWords(new StringReader(title + " " + content));

			parseNegWordsMap();

			parsePosWordsMap();

			int result = analyzeContentsTrendency();

//			if (flag) { // 标题相关，仅判断文本倾向性
//
//				if (result < 0) {
//
//					return SolrQueryResult.NEGATIVE_NATURE;
//
//				} else if (result == 0) {
//
//					return SolrQueryResult.NEUTRAL_NATURE;
//
//				} else {
//
//					return SolrQueryResult.POSITIVE_NATURE;
//
//				}
//
//			} else { // 标题无关，需要复杂的矩阵算法

				parseRelWordsMap();

				if (result < 0) {
					
					// 与本身无关
					if(rels == null || rels.size() < 1){
						return Constants.NEGATIVE_NATURE;
					}

					if (analyzeTrendencyByMatrix()) {
						return Constants.NEGATIVE_NATURE;
					} else {
						return Constants.NEUTRAL_NATURE;
					}

				} else if (result == 0) {

					return Constants.NEUTRAL_NATURE;

				} else {

					return Constants.POSITIVE_NATURE;

				}

//			}

		} catch (IOException e) {
			return Constants.NEUTRAL_NATURE;
		}
	}

	private void clearAll() {

		if (segments != null) {
			segments.clear();
		}
		if (negs != null) {
			negs.clear();
		}
		if (poses != null) {
			poses.clear();
		}
	}
	
	public void segWords(Reader input) throws IOException {

		ArrayList<String> result = new ArrayList<String>();

		MMSeg mmSeg = new MMSeg(input, seg);

		Word word = null;

		while ((word = mmSeg.next()) != null) {

			result.add(word.getString() + " @ " + word.getStartOffset());

		}

		for (String strResult : result) {
			System.out.println(strResult);
		}

	}

	/**
	 * 是否是倾向性相关标题
	 * 
	 * @param title
	 * @return
	 */
	private boolean isRelTitle(String title) {

		try {

			initTitleSegmentsMap(new StringReader(title));

			List<String> relWords = wordLoader.getRefWordList();

			for (String word : relWords) {

				if (segments.containsKey(word)) {
					return true;
				}

			}

		} catch (IOException e) {
			return false;
		}

		return false;

	}

	/**
	 * 获取标题倾向性
	 * 
	 * @param title
	 * @return
	 */
	private int getTitleTrendency() {

		parseNegWordsMap();
		parsePosWordsMap();

		return analyzeContentsTrendency();

	}

	/**
	 * 判断整篇文章的倾向性
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	private int analyzeContentsTrendency() {

		int negScore = 0;
		int posScore = 0;

		if (negs != null && negs.size() > 0) {

			for (Word word : negs) {
				if(wordLoader.getNegWordMap().containsKey(word.getString())){
					negScore += wordLoader.getNegWordMap().get(word.getString());
				}
			}

		}

		if (poses != null && poses.size() > 0) {

			for (Word word : poses) {
				if(wordLoader.getPosWordMap().containsKey(word.getString())){
					posScore += wordLoader.getPosWordMap().get(word.getString());
				}
			}
		}

		return posScore - negScore;
	}

	/**
	 * 交叉矩阵判断文本倾向性
	 * 
	 * @return
	 */
	private boolean analyzeTrendencyByMatrix() {

		if (rels == null || rels.size() == 0) {
			return false;
		}

		if (negs == null || negs.size() == 0) {
			return false;
		}

		for (int i = 0; i < rels.size(); i++) {

			for (int j = 0; j < negs.size(); j++) {

				Word relWord = rels.get(i);
				Word negWord = negs.get(j);

				if (relWord.getStartOffset() < negWord.getStartOffset()) {

					if (negWord.getStartOffset() - relWord.getStartOffset()
							- relWord.getLength() < PS_THRESHOLD) {

						return true;

					}

				} else {
					if (relWord.getStartOffset() - negWord.getStartOffset()
							- negWord.getLength() < NS_THRESHOLD) {
						return true;
					}
				}

			}

		}

		return false;

	}

	/**
	 * 先对标题进行分词
	 * 
	 * @param reader
	 * @throws IOException
	 */
	private void initTitleSegmentsMap(Reader reader) throws IOException {

		segments = new HashMap<String, List<Word>>();

		MMSeg mmSeg = new MMSeg(reader, seg);

		Word word = null;

		while ((word = mmSeg.next()) != null) {

			if (segments.containsKey(word.getString())) {

				segments.get(word.getString()).add(word);
			}

			List<Word> words = new ArrayList<Word>();

			words.add(word);

			segments.put(word.getString(), words);

		}
	}

	/**
	 * 对正文进行分词
	 * 
	 * @param reader
	 * @throws IOException
	 */
	private void initSegmentsMap(Reader reader) throws IOException {

		if (segments == null) {
			segments = new HashMap<String, List<Word>>();
		}

		MMSeg mmSeg = new MMSeg(reader, seg);

		Word word = null;

		while ((word = mmSeg.next()) != null) {

			if (segments.containsKey(word.getString())) {

				segments.get(word.getString()).add(word);
			}

			List<Word> words = new ArrayList<Word>();

			words.add(word);

			segments.put(word.getString(), words);

		}

	}

	/**
	 * 解析负面词汇
	 */
	private void parseNegWordsMap() {

		Map<String, Integer> negMap = wordLoader.getNegWordMap();
		Set<String> negKeys = negMap.keySet();

		for (String negKey : negKeys) {

			List<Word> negWords = segments.get(negKey);

			if (negWords != null) {

				if (negs == null) {
					negs = new ArrayList<Word>();
				}

				negs.addAll(negWords);

			}

		}

	}

	/**
	 * 解析正面词汇
	 */
	private void parsePosWordsMap() {

		Map<String, Integer> posMap = wordLoader.getPosWordMap();
		Set<String> posKeys = posMap.keySet();

		for (String posKey : posKeys) {

			List<Word> posWords = segments.get(posKey);

			if (posWords != null) {

				if (poses == null) {
					poses = new ArrayList<Word>();
				}

				poses.addAll(posWords);

			}

		}
	}

	/**
	 * 解析相关词汇
	 */
	private void parseRelWordsMap() {

		List<String> refWords = wordLoader.getRefWordList();

		for (String word : refWords) {

			List<Word> relWords = segments.get(word);

			if (relWords != null) {

				if (rels == null) {
					rels = new ArrayList<Word>();
				}

				rels.addAll(relWords);

			}
		}

	}

}

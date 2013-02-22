package com.siriuser.weibocrawl.fetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.solr.common.SolrInputDocument;

import com.siriuser.weibocrawl.Constants;
import com.siriuser.weibocrawl.Crawl;
import com.siriuser.weibocrawl.MD5Signature;
import com.siriuser.weibocrawl.analyser.TrendencyAnalyser;
import com.siriuser.weibocrawl.keywords.policy.KeyWordsPolicy;

public abstract class AbstractFetcher implements Fetcher {

	protected KeyWordsPolicy policy;
	protected int depth;
	protected MD5Signature md5Signature = new MD5Signature();
	protected List<Rule> readRules;
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public void setDepth(int depth){
		this.depth = depth;
	}
	
	
	@Override
	public void setPolicy(KeyWordsPolicy policy) {

		this.policy = policy;

//		try {
//			InputStreamReader reader = new InputStreamReader(
//					new FileInputStream("conf/fetch_filter"), "utf-8");
//			readRules = null;
//			readRules = readRules(reader);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

	/**
	 * 是否是有效URL
	 * 
	 * @param url
	 * @return
	 */
	protected boolean isAvaliableUrl(String url) {

		boolean flag = false;

		if (readRules != null && readRules.size() > 0) {

			for (Rule readRule : readRules) {

				boolean sign = readRule.isSign();

				if (sign && url.matches(readRule.getRule())) {

					flag = true;

				} else if (!sign && url.matches(readRule.getRule())) {

					return false;
				}
			}

		}

		return flag;

	}

	protected String changeString2Date(String dateStr) {

		if (dateStr.matches("\\d+")) {

			SimpleDateFormat formate = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");

			return formate.format(new Date(Long.parseLong(dateStr)));

		}

		String date = "";
		String[] tempDateStr = dateStr.trim().replaceAll("\\s+", "@")
				.replaceAll("(-|:)", "@").split("@");

		if (tempDateStr.length > 0) {
			String year = tempDateStr[0];
			if (year.length() == 2) {
				year = "20" + year;
			}
			date = date + year + "-";
		}

		if (tempDateStr.length > 1) {
			date = date
					+ (tempDateStr[1].length() > 1 ? tempDateStr[1] : "0"
							+ tempDateStr[1]) + "-";
		}

		if (tempDateStr.length > 2) {
			date = date
					+ (tempDateStr[2].length() > 1 ? tempDateStr[2] : "0"
							+ tempDateStr[2]) + " ";
		}

		if (tempDateStr.length > 3) {
			date = date
					+ (tempDateStr[3].length() > 1 ? tempDateStr[3] : "0"
							+ tempDateStr[3]) + ":";
		} else {
			date = date + "00:";
		}

		if (tempDateStr.length > 4) {
			date = date
					+ (tempDateStr[4].length() > 1 ? tempDateStr[4] : "0"
							+ tempDateStr[4]) + ":";
		} else {
			date = date + "00:";
		}

		if (tempDateStr.length > 5) {
			date = date
					+ (tempDateStr[5].length() > 1 ? tempDateStr[5] : "0"
							+ tempDateStr[5]);
		} else {
			date = date + "00";
		}

		return date;
	}

	/*
	 * 规则化正则
	 * 
	 * @param reader
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * 
	 * @throws IllegalArgumentException
	 */
	private List<Rule> readRules(Reader reader) throws IOException,
			IllegalArgumentException {

		BufferedReader in = new BufferedReader(reader);
		List<Rule> rules = new ArrayList<Rule>();
		String line;

		while ((line = in.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}
			char first = line.charAt(0);
			boolean sign = false;
			switch (first) {
			case '+':
				sign = true;
				break;
			case '-':
				sign = false;
				break;
			case ' ':
			case '\n':
			case '#': // skip blank & comment lines
				continue;
			default:
				throw new IOException("Invalid first character: " + line);
			}

			String regex = line.substring(1);
			rules.add(new Rule(regex, sign));
		}
		return rules;
	}

	public void logWrite(String errorLog) {
		// TODO Auto-generated method stub
		try {
			RandomAccessFile f2 = new RandomAccessFile("d:/log.txt", "rw");
			f2.writeBytes(errorLog + "\r\n");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected boolean isCrawledUrl(String url) {
		return (Crawl.isCrawledURL(url)&& isAvaliableUrl(url));
	}

	class Rule {

		String rule;
		boolean sign;

		public Rule(String rule, boolean sign) {
			this.rule = rule;
			this.sign = sign;
		}

		public String getRule() {
			return rule;
		}

		public void setRule(String rule) {
			this.rule = rule;
		}

		public boolean isSign() {
			return sign;
		}

		public void setSign(boolean sign) {
			this.sign = sign;
		}
	}

	protected SolrInputDocument buildSolrInputDocumentList(String url,String content, String title, Date date) {
							
		SolrInputDocument sid = null;
		if (title != null && !"".equals(title)) {
			sid = new SolrInputDocument();
			System.out.println("fetch Url: " + url);
			sid.addField(Constants.FIELD_ID, url);
			sid.addField(Constants.FIELD_URL, url);
			sid.addField(Constants.FIELD_CONTENT, content);
			sid.addField(Constants.FIELD_TITLE, title);
			sid.addField(Constants.FIELD_PUBLISHDATE, date);
			sid.addField(Constants.FIELD_DIGEST, md5Signature.calculate(content));
			
			// 增加倾向性分析
			sid.addField(Constants.FIELD_TREND, TrendencyAnalyser.getInstance().analyzeTrendency(title, content));
			
		}
		
		return sid;
	}
	
	protected Calendar getCurrentDate() {
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		calendar.set(Calendar.HOUR, -12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		return calendar;
	}
}

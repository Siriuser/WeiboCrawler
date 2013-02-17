package com.siriuser.weibocrawl.fetcher;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.siriuser.weibocrawl.WeiboBlock;
import com.siriuser.weibocrawl.keywords.KeywordReader;

public class WeiboFetcher extends AbstractFetcher{

	// 文本块正文匹配正则
	private final String blockRegex = "<script>STK\\s&&\\sSTK\\.pageletM\\s&&\\sSTK\\.pageletM\\.view\\(.*\\)";
	private Pattern pattern = Pattern.compile(blockRegex);
	private static Whitelist whitelist = new Whitelist();
	
	static{
		// 只保留em标签的文本
		whitelist.addTags("em");
	}
	
	@Override()
	public List<SolrInputDocument> fetch() {

		List<SolrInputDocument> newsResults = new ArrayList<SolrInputDocument>();
		
		newsResults = WeiboResult();

		System.out.println("WeiboFetcher Over: " + newsResults.size());

		return newsResults;
		
	}


	/**
	 * 获取关键字搜索结果
	 * @return
	 */
	private List<SolrInputDocument> WeiboResult() {

		String keyWord = null;
		List<SolrInputDocument> newsResultList = new ArrayList<SolrInputDocument>();
		
		// 获取配置的关键字
		List<String> keyWordList = KeywordReader.getInstance().getKeywords();
			
		for (String keyWordLine : keyWordList) {
			
			// 转换为新浪微博搜索接受的格式
			keyWord = policy.getKeyWord(keyWordLine,null);

			newsResultList.addAll(getWeiboContent(keyWord));
		}
			
		return newsResultList;
	}
	
	/**
	 * 获取搜索结果
	 * @param keyWord
	 * @return
	 */
	private List<SolrInputDocument> getWeiboContent(String keyWord){
		
		System.out.println("fetch keyword: " + keyWord);
		
		List<SolrInputDocument> resultList = new ArrayList<SolrInputDocument>();
		
		for(int i = 0; i < depth; i++){
			
			String page = "";
			
			if(i > 0){
				page = "&page=" + (i+1);
			}
			
			//抓取返回50个内容	
			try {
				
				System.out.println("fetch url page depth " + (i + 1));
				
				// 注意&nodup=1
				Document doc = Jsoup.connect(
						"http://s.weibo.com/weibo/" + keyWord+"&nodup=1" + page).get();
				
				String source = doc.html();
				
				// 匹配文本块
				Matcher m = pattern.matcher(source);
				
				while(m.find()){
					
					String jsonStr = m.group();
					
					jsonStr = jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf(")"));
					
					// 解析json,转换为实体类
					WeiboBlock block = JSON.parseObject(jsonStr, WeiboBlock.class);
					
					if(block.getHtml().trim().startsWith("<div class=\"search_feed\">")){
						
						doc = Jsoup.parse(block.getHtml());
					}
				}
				
				
				
				List<Element> elements = getAllElement(doc);
				
				if(elements == null || elements.size() == 0){
					
					System.out.println("No more urls to fetch with current keyword." );
					return resultList;
					
				}
				
				for (Element elem : elements) {

					String url = elem.select(".date").last().attr("href");
					String dateS = elem.select(".date").last().attr("date");
					String content = null;
					Date date = null;
					String title = null;
					
					if (!isCrawledUrl(url)){
						
						if (url != null) {
							
							if (dateS != null && !"".equals(dateS)) {
								try {
									date = sdf.parse(changeString2Date(dateS));
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
							
							if (date != null) {
								
								elem.getElementsByClass("info W_linkb W_textb").remove();
								content = Jsoup.clean(Jsoup.clean(elem.select(".content").html(), whitelist), Whitelist.none());
								title = this.parseTitle(content);
						
								url = elem.select(".date").last().attr("href");

								SolrInputDocument sid = buildSolrInputDocumentList(url, content, title, date);
								if (sid != null && sid.size() > 0) {
									resultList.add(sid);
								}
							}
						}else {
							System.out.println("current Url: ---------null------------" );
						}
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return resultList;
	}
	
	/**
	 * 获取所有的结果正文节点
	 * @param doc
	 * @return
	 */
	private List<Element> getAllElement(Document doc) {

		List<Element> resultList = new ArrayList<Element>();
		
		Elements elems = doc.select(".search_feed .feed_list");
		
		for (Element element : elems) {
			resultList.add(element);
		}
		
		return resultList;
	}
	
	
	
	@Override
	protected boolean isCrawledUrl(String url) {
		return isAvaliableUrl(url);
	}


	/**
	 * 生成标题
	 * @param htmlContent
	 * @return
	 */
	private String parseTitle(String htmlContent) {
		if (htmlContent == null || htmlContent.trim().equals(""))
			return null;
		String title = htmlContent;
		title = title.trim();
		for (int i = 0; i < title.length(); i++) {
			if (String.valueOf((title.charAt(i))).matches("[，.\\?\\!\\.,]")) {
				title = title.substring(0, i);
				break;
			}
		}
		return title;
	}
}

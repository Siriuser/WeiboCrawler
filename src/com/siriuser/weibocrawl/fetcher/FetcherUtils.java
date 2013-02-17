package com.siriuser.weibocrawl.fetcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.siriuser.weibocrawl.Constants;
import com.siriuser.weibocrawl.keywords.policy.KeyWordsPolicy;

public class FetcherUtils {

	private static FetcherUtils instance = null;

	private List<Fetcher> fetchers;

	private FetcherUtils() {
		fetchers = new ArrayList<Fetcher>();
		loadFetchers();
	}

	public static FetcherUtils getInstance() {
		if (instance == null) {
			instance = new FetcherUtils();
		}

		return instance;
	}

	private void loadFetchers() {

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new FileInputStream("conf/property.xml"));
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (document == null)
			return;

		List<Element> fetcherElements = (List<Element>) document
				.selectNodes(Constants.XPATH_FETCHER);

		for (Element element : fetcherElements) {

			String className = element.attributeValue("class");

			try {
				Class<?> cls = Class.forName(className);
				Fetcher fetcher = (Fetcher) cls.getConstructor(null)
						.newInstance(null);
				Element policy = element.element("policy");
				className = policy.attributeValue("class");
				cls = Class.forName(className);
				KeyWordsPolicy keyPolicy = (KeyWordsPolicy) cls.getConstructor(
						null).newInstance(null);
				fetcher.setPolicy(keyPolicy);
				fetcher.setDepth(Integer.parseInt(element.attributeValue("depth")));
				fetchers.add(fetcher);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public List<Fetcher> getFetchers() {
		return fetchers;
	}

	public static void main(String[] args) {

		FetcherUtils utils = FetcherUtils.getInstance();

		List<Fetcher> fetchers = utils.getFetchers();

		for (Fetcher fetcher : fetchers) {
			System.out.println(fetcher.getClass());
		}

	}
}

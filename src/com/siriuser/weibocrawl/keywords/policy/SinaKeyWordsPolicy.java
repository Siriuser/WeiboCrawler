package com.siriuser.weibocrawl.keywords.policy;

public class SinaKeyWordsPolicy implements KeyWordsPolicy {

	@Override
	public String getKeyWord(String keyWordLine, String siteLine) {
		
		String keyWord = null;

		keyWordLine = keyWordLine.replaceAll("\"", "");
		keyWordLine = keyWordLine.replaceAll("AND", " ");
		keyWordLine = keyWordLine.replaceAll("OR", "|");
//		keyWordLine = keyWordLine.replaceAll("(", "");
//		keyWordLine = keyWordLine.replaceAll(")", "");
		
		if (keyWordLine.contains("|")) {
			
			String[] tempStrings = keyWordLine.split("|");
			if (tempStrings.length > 3) {
				for (int i=0; i<3 ;i++) {
					keyWord += tempStrings[i];
					keyWord += "|";
				}
			}else {
				keyWord = keyWordLine;
			}
		}else {
			keyWord = keyWordLine;
		}
		return java.net.URLEncoder.encode(java.net.URLEncoder.encode(keyWord));
	}

}

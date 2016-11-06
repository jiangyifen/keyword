# keyword
有穷状态自动机（DFA）关键词过滤工具


    public static void main(String[] args) {
		String txt = "中文12中文AB，中国人民站起来了，这个【关键词】找到没有？【自定义关键词】";
		KeywordUtil ku = new KeywordUtil();
		
		System.out.println("原始文本：" + txt);
		
		
		//KeywordUtrl.isContainKeyword 的用法
		ku.addKeywordToMap("自定义关键词");
		
		// KeywordUtrl.isContainKeyword 的用法
		Boolean isContainKeyword = ku.isContaintKeyword(txt, MATCH_TYPE_LONG);
		System.out.println("是否包含关键词？ "+isContainKeyword);
		System.out.println("============");
		
		
		// KeywordUtrl.getKeywords 的用法
		Set<String> keywords = null;
		keywords = ku.getKeywords(txt, MATCH_TYPE_LONG );
		System.out.println("长模式下匹配到的关键词有：");
		for(String keyword: keywords){
			System.out.println(keyword);
		}
		System.out.println("============");
		
		keywords = ku.getKeywords(txt, MATCH_TYPE_SHORT);
		System.out.println("短模式下匹配到的关键词有：");
		for(String keyword: keywords){
			System.out.println(keyword);
		}
		System.out.println("============");
		
		// KeywordUtrl.replaceKeywords 的用法
		String after = null;
		after = ku.replaceKeywords(txt, MATCH_TYPE_LONG,"*");
		System.out.println("长模式下过滤后的字符串为：");
		System.out.println(after);
		System.out.println("============");
		
		after = ku.replaceKeywords(txt, MATCH_TYPE_SHORT,"*");
		System.out.println("短模式下过滤后的字符串为：");
		System.out.println(after);
		System.out.println("============");
	}

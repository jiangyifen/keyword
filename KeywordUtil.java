import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * DFA 敏感词工具
 * 
 */
public class KeywordUtil {

	/**
	 * 存放敏感词的HashMap<br>
	 * 
	 * 每一个字作为key，Map<String, Object>作为value。
	 * 这个Object是isEnd的String值（true，false），或是另一个Map<String, Object>
	 * 
	 * 例如：
     * {
	 *     关={
	 *         isEnd=false, 
	 *         键={
	 *             isEnd=false, 
	 *             词={
	 *                 isEnd=true
	 *             }
	 *         }
	 *     }, 
	 *     中={
	 *         isEnd=false, 
	 *         国={
	 *             isEnd=true,
	 *             人={
	 *                 isEnd=true,
	 *                 民={
	 *                     isEnd=true
	 *                 }
	 *             }
	 *         }
	 *     }
	 * 
	 * @param keywordsSet
	 */
	public Map<String, Object> globalKeywordsMap = new ConcurrentHashMap<>();

	/**
	 * 如【中国人民站起来了】，其中关键词为【中国】、【中国人】、【中国人民】,短模式会匹配到【中国】，长模式会匹配到【中国人民】
	 */
	public static final int MATCH_TYPE_SHORT = 1;//短模式。
	
	/**
	 * 如【中国人民站起来了】，其中关键词为【中国】、【中国人】、【中国人民】,短模式会匹配到【中国】，长模式会匹配到【中国人民】
	 */
	public static final int MATCH_TYPE_LONG = 2;//长模式
	
	private String ENCODING = "UTF-8";
	
	private String filePath = "d:\\keywords.txt";
	
	public KeywordUtil(){
		try {
			Set<String> keywordsSet = loadKeywords(filePath);//具体实现如何加载关键词。比如，从文件，从数据库，从远程等

			for (String keyword : keywordsSet) {
				addKeywordToMap(keyword);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 把指定文件中的敏感词加载到一个set中，并返回该set
	private Set<String> loadKeywords(String filePath) {
		Set<String> keywordsSet = new HashSet<>();

		File f = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			f = new File(filePath);
			isr = new InputStreamReader(new FileInputStream(f), ENCODING);
			br = new BufferedReader(isr);

			if (f.isFile() && f.exists()) {
				String txt = null;
				while ((txt = br.readLine()) != null) {
					keywordsSet.add(txt);
				}
			} else {
				System.out.println("File not found!! ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return keywordsSet;
	}




	
	/**
	 * 
	 * 检查文字中是否包含敏感字符 如果存在，则返回敏感词字符的长度，不存在返回0
	 * 
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return 如果存在，则返回敏感词字符的长度，不存在返回0
	 */
	@SuppressWarnings("unchecked")
	private int checkKeyword(String txt, int beginIndex, int matchType) {

		boolean isEnd = false; // 关键字结束标识位
		int keywordLength = 0; // 匹配到的关键字的长度
		String c = "";

		Map<String, Object> nowMap = globalKeywordsMap;
		for (int i = beginIndex; i < txt.length(); i++) {
			c = txt.charAt(i)+"";
			nowMap = (Map<String, Object>) nowMap.get(c); // 获取指定key
			if (nowMap == null) {
				break;// 不存在，直接返回
			} else { // 存在，则判断是否为最后一个
				keywordLength++; // 找到相应key，匹配标识+1
				if (nowMap.get("isEnd").equals("true")) { // 如果为最后一个匹配规则,结束循环，返回匹配标识数
					isEnd = true; // 结束标志位为true
					if (matchType == MATCH_TYPE_SHORT) { // 最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			}
		}

		if (!isEnd) {
			return 0;
		}

		return keywordLength;
	}

	/**
	 * 拼接出指定长度的字符。如：拼出7个*
	 * @param s 替换成什么符号，例如，*、x、河蟹、萌萌
	 * @param length
	 * @return
	 */
	private String getReplaceString(String s, int length) {
		String result = s;
		for (int i = 1; i < length; i++) {
			result += s;
		}

		return result;
	}
	
	/**
	 * 
	 * 向内存中实时添加一个关键词
	 * 
	 * @param keyword
	 */
	public void addKeywordToMap(String keyword){
		Map<String, Object> nowMap = globalKeywordsMap;
		for (int i = 0; i < keyword.length(); i++) {
			String keyChar = keyword.charAt(i) + "";
			@SuppressWarnings("unchecked")
			Map<String, Object> wordMap = (Map<String, Object>) nowMap.get(keyChar); // 获取

			if (wordMap != null) {
				// 如果存在该key，直接赋值
				nowMap = wordMap;
			} else { 
				// 如果不存在，则构建一个map，同时将isEnd设置为false，因为他不是最后一个
				Map<String, Object> newWordMap = new HashMap<String, Object>();
				newWordMap.put("isEnd", "false"); // 不是最后一个
				nowMap.put(keyChar, newWordMap);
				nowMap = newWordMap;
			}

			//如果当前字符是关键字的最后一个字，就置一下标记位
			if (i == keyword.length() - 1) {
				nowMap.put("isEnd", "true"); // 最后一个
			}
		}
	}
	
	/**
	 * 判断是否包含关键词，返回true，false
	 * 
	 * @param txt
	 * @param matchType
	 * @return
	 */
	public boolean isContaintKeyword(String txt, int matchType) {
		boolean result = false;
		for (int i = 0; i < txt.length(); i++) {
			int matchFlag = checkKeyword(txt, i, matchType);
			if (matchFlag > 0) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * 
	 * 获取所包含的关键词，放在set里并返回
	 * 
	 * @param txt
	 * @param matchType
	 * @return
	 */
	public Set<String> getKeywords(String txt, int matchType) {
		Set<String> keywordSet = new HashSet<String>();

		for (int i = 0; i < txt.length(); i++) {
			int length = checkKeyword(txt, i, matchType);
			if (length > 0) {
				keywordSet.add(txt.substring(i, i + length));
				i = i + length - 1;
			}
		}

		return keywordSet;
	}

	/**
	 * 
	 * 将关键词替换成指定的字符
	 * 
	 * @param txt
	 * @param matchType
	 * @param replaceChar
	 * @return
	 */
	public String replaceKeywords(String txt, int matchType, String replaceChar) {
		String resultTxt = txt;
		Set<String> set = getKeywords(txt, matchType);
		String replaceString = null;
		for (String word : set) {
			replaceString = getReplaceString(replaceChar, word.length());
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}
		return resultTxt;
	}



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
}

package com.brucezee.jspider.extrator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 扩展document,支持获取文档正文内容 date: 2018年9月18日 下午3:51:27 <br/>
 * 
 * @author hxfei
 * @version
 */
public class ArticleExtrator{
	/**
	 * 原文档
	 */
	private Document rawDocument;
	
	/**
	 * 文档发表时间格式
	 */
	private static Pattern datePattern = Pattern.compile("(\\d{4})[/\\-年](\\d{1,2})[/\\-月](\\d{1,2})[日]{0,1}");
	/**
	 * 内容框标签
	 */
	private static final String[] BOX_TAG={"table","div","ul","dl","main","article"};

	public ArticleExtrator(Document document) {
		this.rawDocument = document;
	}

	public static void main(String[] args) throws Exception {
		Document document = Jsoup.connect("http://www.sohu.com/a/254667386_157267?g=0?code=f04d10c830e6b877d2e46de3168ed8f6&_f=index_cpc_1").header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50").get();
		ArticleExtrator extrator = new ArticleExtrator(document);
		Article article=extrator.extract();
		System.out.println(article.getTitle());
		System.out.println(article.getContentText());
		System.out.println(article.getDate());
//		System.out.println(text);
		
	}

	/**
	 * 获取正文html代码
	 * 
	 * @author hxfei
	 * @date: 2018年9月18日 下午4:02:07 <br/>
	 * @return
	 */
	public Article extract() {
		Document document = rawDocument.clone();
		document = this.cleanup(document);
		String totalText = document.text();
		int totalTextCount = totalText.length();
		List<WindowBox> windowBoxList = new ArrayList<WindowBox>();
		List<Element> allBoxElements = getAllBoxElements(document);
		Iterator<Element> boxIterator = allBoxElements.iterator();
		while (boxIterator.hasNext()) {
			WindowBox windowBox = new WindowBox();
			Element boxElement = boxIterator.next();
			Element cloneElement = boxElement.clone();
			//链接字符长度
			int anchorTextCount = getAnchorTextCount(cloneElement);
			//一般字符长度
			int normalTextCount = getNormalTextCount(cloneElement);
			//没有一般字符，忽略
			if (normalTextCount == 0) {
				boxElement.remove();
				continue;
			}
			//链接与一般字符的比例超过80%,忽略
			if (((double) anchorTextCount / (double) normalTextCount) > 0.8d) {
				boxElement.remove();
				continue;
			}
			//干扰节点，忽略
			boolean onlyNoiseNode = this.isOnlyNoiseNode(boxElement);
			if (onlyNoiseNode) {
				boxElement.remove();
				continue;
			}
			windowBox.setElement(boxElement);
			windowBox.setAnchorTextCount(anchorTextCount);
			windowBox.setNormalTextCount(normalTextCount);
			//一般字符占总文档总字符的长度
			double factor = ((double) normalTextCount / (double) totalTextCount);
			windowBox.setFactor(factor);
			windowBoxList.add(windowBox);
		}
		if (windowBoxList.isEmpty()) {
			return null;
		}
		//按一般字符与所有字符的比例排序，比例最大的为正文框
		windowBoxList.sort(new Comparator<WindowBox>() {
			@Override
			public int compare(WindowBox box1, WindowBox box2) {
				double diff = box2.getFactor() - box1.getFactor();
				return diff > 0 ? 1 : -1;
			}
		});
		Article article=new Article();
		WindowBox contentBox=windowBoxList.get(0);
		Element contentElement = contentBox.getElement();
		String contentHtml=contentElement.html();
		article.setContentHtml(contentHtml);
		String title = extractTitle(rawDocument);
		article.setTitle(title);
		String date=extractDate(rawDocument);
		article.setDate(date);
		return article;
	}
	/**
	 * 解析标题
	 * @author hxfei
	 * @date: 2018年9月19日 下午5:01:45 <br/>
	 * @param document
	 * @return
	 */
	private String extractTitle(Document document) {
		String title=document.select("title").text();
		return title;
	}
	/**
	 * 解析时间
	 * @author hxfei
	 * @date: 2018年9月19日 下午5:02:03 <br/>
	 * @param document
	 * @return
	 */
	private String extractDate(Document document) {
		String text=document.text();
		Matcher matcher=datePattern.matcher(text);
		if(matcher.find()){
			return matcher.group();
		}
		return "";
	}
	/**
	 * 获取一般字符长度
	 * @author hxfei
	 * @date: 2018年9月19日 下午3:19:28 <br/>
	 * @param boxElement
	 * @return
	 */
	private int getNormalTextCount(Element boxElement) {
		String normalText = boxElement.text();// 因为<a>标签已经移除了，剩下的都是一般节点
		int normalTextCount = normalText.length();
		return normalTextCount;
	}
	/**
	 * 获取链接字符长度
	 * @author hxfei
	 * @date: 2018年9月19日 下午3:19:39 <br/>
	 * @param boxElement
	 * @return
	 */
	private int getAnchorTextCount(Element boxElement) {
		Elements anchorList = boxElement.select("a");
		StringBuilder anchorText = new StringBuilder();
		anchorList.forEach((element) -> {
			String text = element.text();
			anchorText.append(text);
			element.remove();
		});
		int anchorTextCount = anchorText.length();
		return anchorTextCount;
	}

	/**
	 * 移除不使用的节点
	 * 
	 * @author hxfei
	 * @date: 2018年9月19日 上午11:24:14 <br/>
	 * @param document
	 * @return
	 */
	private Document cleanup(Document document) {
		document.select("style").remove();
		document.select("script").remove();
		document.select("comment").remove();
		document.select("option").remove();
		document.select("iframe").remove();
		document.select("textarea").remove();
		document.select("object").remove();
		document.select("select").remove();
		this.removeMetaElements(document,":contains(当前位置：)");
		this.removeMetaElements(document,":contains(发布时间：)");
		this.removeMetaElements(document,":contains(上一篇：)");
		this.removeMetaElements(document,":contains(字体：)");
		this.removeMetaElements(document,":contains(下一篇：)");
		return document;
	}

	private void removeMetaElements(Document document,String selector) {
		Elements metaElements=document.select(selector);
		Iterator<Element> navIterator = metaElements.iterator();
		int allTextCount=document.text().length();
		while(navIterator.hasNext()){
			Element navElement=navIterator.next();
			int navTextCount=navElement.text().length();
			if((double)((double)navTextCount/(double)allTextCount)<0.2d){
				navElement.remove();
			}
			
		}
	}

	/**
	 * 是否仅包含干扰信息
	 * 
	 * @author hxfei
	 * @date: 2018年9月19日 上午11:25:55 <br/>
	 * @param element
	 * @return
	 */
	private boolean isOnlyNoiseNode(Element element) {
		List<Element> childBoxElements = this.getAllBoxElements(element);
		if (childBoxElements.isEmpty()) {
			String text = element.text();
			return text.toLowerCase().contains("copyright") || text.toLowerCase().contains("all rights reserved")
					|| text.toLowerCase().contains("\u7248\u6743\u6240\u6709") || text.toLowerCase().contains("\251")
					|| text.toLowerCase().contains("ICP\u5907");
		}
		return false;
	}

	/**
	 * 获取所有内容框
	 * 
	 * @author hxfei
	 * @date: 2018年9月18日 下午5:09:23 <br/>
	 * @return
	 */
	private List<Element> getAllBoxElements(Element rootElement) {
		List<Element> allBoxElements = new ArrayList<Element>();
		for(String tagName:BOX_TAG){
			Elements boxTagList = rootElement.select(tagName);
			boxTagList.forEach((element) -> {
				allBoxElements.add(element);
			});
		}
		return allBoxElements;
	}
}

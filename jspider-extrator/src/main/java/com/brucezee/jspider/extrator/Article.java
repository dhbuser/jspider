
package com.brucezee.jspider.extrator;

/**
 * 文档属性实体类 Date: 2018年9月19日 下午3:24:13 <br/>
 * 
 * @author hxfei
 */
public class Article {
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 内容html
	 */
	private String contentHtml;
	/**
	 * 内容文本
	 */
	private String contentText;
	/**
	 * 作者
	 */
	private String author;
	/**
	 * 日期
	 */
	private String date;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentHtml() {
		return contentHtml;
	}

	public void setContentHtml(String contentHtml) {
		this.contentHtml = contentHtml;
	}

	public String getContentText() {
		if (contentText != null) {
			return contentText;
		}
		if (contentHtml == null) {
			return "";
		}
		contentText = contentHtml.replaceAll("<[^>]*>", "");
		contentText = contentText.replaceAll("<!--[*\\w\\s]-->", "");
		contentText = contentText.replaceAll("", "");
		return contentText;
	}

	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}

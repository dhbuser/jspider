  
package com.brucezee.jspider.extrator;

import org.jsoup.nodes.Element;

/**  
 * 内容框
 * Date:     2018年9月18日 下午5:17:30 <br/>  
 * @author   hxfei     
 */
public class WindowBox {
	/**
	 * DOM元素
	 */
	private Element element;
	/**
	 * 链接字符的字数
	 */
	private int anchorTextCount;
	/**
	 * 一般字符的个数
	 */
	private int normalTextCount;
	/**
	 * 与所有文字的占比
	 */
	private double factor;
	
	public double getFactor() {
		return factor;
	}
	public void setFactor(double factor) {
		this.factor = factor;
	}
	public Element getElement() {
		return element;
	}
	public void setElement(Element element) {
		this.element = element;
	}
	public int getAnchorTextCount() {
		return anchorTextCount;
	}
	public void setAnchorTextCount(int anchorTextCount) {
		this.anchorTextCount = anchorTextCount;
	}
	public int getNormalTextCount() {
		return normalTextCount;
	}
	public void setNormalTextCount(int normalTextCount) {
		this.normalTextCount = normalTextCount;
	}
}
  

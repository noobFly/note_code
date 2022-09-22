package com.noob.dto;

import java.io.Serializable;
import java.util.List;

public class Page<E> implements Serializable, Cloneable {

	private static final long serialVersionUID = 7669027011052797260L;
	private int pageSize = 10; // 每页显示记录数
	private int totalPage; // 总页数
	private int totalCount; // 总记录数
	private int pageIndex; // 当前页
	private int currentResult; // 当前记录起始索引
	private List<E> result;

	public Page() {
	}

	public Page(int pageNo, int showCount) {
		this.pageIndex = pageNo + 1;
		this.pageSize = showCount;
	}

	public int getTotalPage() {
		if (totalCount % pageSize == 0) {
			totalPage = totalCount / pageSize;
		} else {
			totalPage = totalCount / pageSize + 1;
		}
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageIndex() {
		if (pageIndex <= 0) {
			pageIndex = 1;
		}
		if (pageIndex > getTotalPage()) {
			pageIndex = getTotalPage();
		}
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentResult() {
		currentResult = (getPageIndex() - 1) * getPageSize();
		if (currentResult < 0) {
			currentResult = 0;
		}
		return currentResult;
	}

	public void setCurrentResult(int currentResult) {
		this.currentResult = currentResult;
	}

	public List<E> getResult() {
		return result;
	}

	public void setResult(List<E> result) {
		this.result = result;
	}
}

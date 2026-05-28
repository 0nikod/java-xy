package com.campus.secondhand.model;

public class AiSearchAssistResult {

	private String keyword;
	private String category;
	private GoodsSortOption sortOption;
	private String explanation;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public GoodsSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(GoodsSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
}

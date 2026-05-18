package com.campus.secondhand.model;

/**
 * 通用图表数据点，包含一个标签和对应数值。
 */
public class ChartPoint {

	private String label;
	private double value;

	/**
	 * 创建空的数据点。
	 */
	public ChartPoint() {
	}

	/**
	 * 创建带标签和值的数据点。
	 */
	public ChartPoint(String label, double value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}

package com.campus.secondhand.model;

import org.junit.Assert;
import org.junit.Test;

public class GoodsTest {

	@Test
	public void conditionTextShouldReturnCorrectStarString() {
		Goods goods = new Goods();

		goods.setConditionLevel(1);
		Assert.assertEquals("★☆☆☆☆", goods.getConditionText());

		goods.setConditionLevel(3);
		Assert.assertEquals("★★★☆☆", goods.getConditionText());

		goods.setConditionLevel(5);
		Assert.assertEquals("★★★★★", goods.getConditionText());
	}

	@Test
	public void conditionTextShouldClampOutOfRangeValues() {
		Goods goods = new Goods();

		goods.setConditionLevel(0);
		Assert.assertEquals("★☆☆☆☆", goods.getConditionText());

		goods.setConditionLevel(10);
		Assert.assertEquals("★★★★★", goods.getConditionText());
	}
}

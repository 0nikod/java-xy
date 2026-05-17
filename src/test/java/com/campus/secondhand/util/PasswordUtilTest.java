package com.campus.secondhand.util;

import org.junit.Assert;
import org.junit.Test;

public class PasswordUtilTest {

    @Test
    public void hashPasswordShouldBeDeterministic() {
        Assert.assertEquals("e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446", PasswordUtil.hashPassword("user123"));
    }

    @Test
    public void matchesShouldValidateHash() {
        Assert.assertTrue(PasswordUtil.matches("admin123", "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"));
    }
}

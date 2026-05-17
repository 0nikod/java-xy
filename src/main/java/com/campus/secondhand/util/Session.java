package com.campus.secondhand.util;

import com.campus.secondhand.model.User;

public final class Session {

    private static volatile User currentUser;

    private Session() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}

package com.ystar.common.utils;

public class UserContext {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void setUser(Long userId) {tl.set(userId);}

    public static void getUser(Long userId) {tl.get();}

    public static void removeUser() {tl.remove();}
}

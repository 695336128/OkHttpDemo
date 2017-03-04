package com.example.zhang.okhttpdemo.Utils;

import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static String LOG_TAG = "merlin";

    //用户名正则表达式
    private static String USERNAME_PATTERN = "[A-Za-z0-9_\\-\\u4e00-\\u9fa5]+";

    //邮箱地址正则表达式
    private static String EMAIL_PATTERN = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";

    //网址正则表达式
    private static String URL_PATTERN = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";

    //手机正则表达式(国内)
    private static String TEL_PATTERN = "0?(13|14|15|18)[0-9]{9}";

    // 判断字符串是否为空
    public static boolean isNull(String s) {
        if (s == null || s.equals("null") || s.equals("") || s.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    // 判断字符串是否为空
    public static boolean isAsNull(String s) {
        if (s == null || s.equals("null") || s.equals("") || s.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    // 判断字符串是否为空
    public static boolean isSpace(String s) {
        if (s == null || s.equals("null") || s.equals("") || s.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int length(String str) {
        int length = 0;
        try {
            if (!StringUtils.isNull(str)) {
                length = str.getBytes("UTF-8").length;
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }
        return length;
    }

    /**
     * 替换Null
     *
     * @param str
     * @return
     */
    public static String repNull(String str) {
        if (str == null)
            str = "";
        return str;
    }

    public static int getStringIdx(Context context, String key) {
        int i = 0;
        try {
            Class localClass = Class.forName(context.getPackageName() + ".R$string");
            Field localField = localClass.getField(key);
            i = Integer.parseInt(localField.get(localField.getName()).toString());
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return i;
    }

    /**
     * 去除字符串中的空格回车
     *
     * @param sub
     * @return
     */
    public static String subString(String sub) {
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(sub);
        return m.replaceAll("");
    }

    public static String timePhoneNum(String mobile) {
        if (StringUtils.isNull(mobile)) {
            mobile = StringUtils.repNull(mobile);
        } else {
            mobile = mobile.trim();
            mobile = mobile.replaceAll("-", "");
            mobile = mobile.replaceAll(" ", "");// 半角空格
            mobile = mobile.replaceAll("　", "");// 全角空格
        }
        return mobile;
    }

    public static String getAlpha(String str) {
        if (str == null) {
            return "#";
        }

        if (str.trim().length() == 0) {
            return "#";
        }

        char c = str.trim().substring(0, 1).charAt(0);
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else {
            return "#";
        }
    }

    /**
     * 手机号正则表达式
     **/
    public final static String MOBLIE_PHONE_PATTERN = "^((13[0-9])|(15[0-9])|(18[0-9])|(14[7])|(17[0|6|7|8]))\\d{8}$";

    /**
     * 通过正则验证是否是合法手机号码
     *
     * @param phoneNumber
     * @return
     */
    public static boolean isMobile(String phoneNumber) {
        Pattern p = Pattern.compile(MOBLIE_PHONE_PATTERN);
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * 判断是否为URl网址
     * @param scanStr
     * @return
     */
    public static boolean isUrl(String scanStr) {
        Pattern pattern = Pattern.compile("^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+");
        Matcher matcher = pattern.matcher(scanStr);
        return matcher.matches();
    }

    /**
     * 判断是否为用户名
     * @param username
     * @return
     */
    public static boolean isUserName(String username){
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
}

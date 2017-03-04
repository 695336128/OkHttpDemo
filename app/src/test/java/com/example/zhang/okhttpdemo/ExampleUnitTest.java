package com.example.zhang.okhttpdemo;

import com.example.zhang.okhttpdemo.Utils.EncryptUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        String md2 = "md2";
        md2 = EncryptUtils.encryptMD2ToString(md2);
        System.out.println(md2);
    }
}
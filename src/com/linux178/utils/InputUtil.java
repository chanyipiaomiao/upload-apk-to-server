package com.linux178.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class InputUtil {


    /**
     * 得到从命令行输入的字符串
     * @return 得到的字符串
     */
    public static String getInputFromStandardInput(){
        String input = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            input = bufferedReader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

}

package com.linux178.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import java.io.FileInputStream;
import java.io.InputStream;


public class ConfigUtil {

    public static JSONObject getConfigFromJson(String configFileName) {

        JSONObject jsonObject = null;
        InputStream inputStream = null;

        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName);
            if (inputStream == null) {
                inputStream = new FileInputStream(configFileName);
            }

            String config = IOUtils.toString(inputStream,"utf-8");
            jsonObject = JSONObject.parseObject(config);

        } catch (Exception e) {
            System.out.println("读取配置文件出现异常,文件名: " + configFileName + " 原因:"  + e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return jsonObject;
    }
}
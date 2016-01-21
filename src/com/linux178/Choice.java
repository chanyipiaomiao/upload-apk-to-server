package com.linux178;


import com.linux178.utils.InputUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Choice {

    private static Map<String,String> itemMap = new HashMap<String, String>();

    /**
     * 输出平台列表
     * @param itemList 项目列表
     */
    public static void itemsList(List itemList){
        System.out.println("请选择这次上传的平台:");
        for (int i = 0; i < itemList.size(); i++) {
            String key = String.valueOf(i + 1);
            String value = itemList.get(i).toString();
            System.out.printf("\t%s\t%s\n", key, value);
            itemMap.put(key,value);
        }
        System.out.printf("\t%s\t%s\n","q","exit");
    }

    /**
     * 选择平台
     * @return 所选择的平台
     */
    public static String choiceItem(){
        System.out.print("请选择编号 >>>");
        String fromStandardInput = InputUtil.getInputFromStandardInput();
        String choice = null;
        if ("q".equalsIgnoreCase(fromStandardInput)){
            System.exit(0);
        } else if (itemMap.containsKey(fromStandardInput)){
            choice = itemMap.get(fromStandardInput);
            System.out.printf("--> 你选择的是 [ %s ]\n", choice);
        } else {
            System.out.println("*** 输入有误,请重新输入!");
            choiceItem();
        }
        return choice;
    }


    /**
     * 输入apk或ipa文件所在的目录
     * @return 输入的目录
     */
    public static String inputSrcDirectory(){
        String fromStandardInput;
        File dirName;
        boolean notExists;
        do {
            notExists = false;
            System.out.print("--> 请输入文件所在的目录 >>>");
            fromStandardInput = InputUtil.getInputFromStandardInput();
            dirName = new File(fromStandardInput);
            if (! dirName.exists()){
                notExists = true;
                System.out.println("输入有误,请重新输入");
            }
        } while (notExists);

        return fromStandardInput;
    }

}

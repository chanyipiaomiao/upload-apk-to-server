package com.linux178;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.linux178.models.ServerInfo;
import com.linux178.models.UploadInfo;
import com.linux178.utils.ConfigUtil;
import com.linux178.utils.FTPUtil;
import com.linux178.utils.Transfer;
import com.linux178.utils.SFTPUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class Main {

    public static void main(String[] args) {

        JSONObject jsonObject = ConfigUtil.getConfigFromJson("conf/config.json");

        if (jsonObject != null){
            JSONArray items = jsonObject.getJSONArray("items");
            List<String> itemList = new ArrayList<String>();
            for (Object object : items){
                itemList.add(object.toString());
            }

            // 输出列表
            Choice.itemsList(itemList);

            // 做出选择
            String choice = Choice.choiceItem();

            // 根据选择的平台，然后查询平台对应的服务器信息
            String platform = jsonObject.getString(choice);
            UploadInfo uploadInfo = JSONObject.parseObject(platform, UploadInfo.class);

            //得到平台所使用的服务器信息代表字符串
            String server = uploadInfo.getServerInfo();

            // 再根据服务器信息字符串查询连接服务器所需要的IP、用户名、密码、端口信息生成一个对象
            ServerInfo serverInfo = JSONObject.parseObject(jsonObject.getString(server), ServerInfo.class);

            // 获取要上传的指定文件列表
            String srcDirectory = Choice.inputSrcDirectory(); // 获取源文件目录
            String[] suffix = uploadInfo.getSuffix().split("\\s"); //上传指定后缀名的文件
            Collection<File> uploadFileList = FileUtils.listFiles(new File(srcDirectory), suffix, false);

            // 初始化
            Transfer transfer;
            if ("ftp".equals(serverInfo.getProtocol())){
                transfer = new FTPUtil(serverInfo,uploadInfo);
            } else {
                transfer = new SFTPUtil(serverInfo,uploadInfo);
            }

            // 获取服务器上的文件列表
            List<String> filesOnServerList = transfer.getFileListOnServer();

            // 生成要备份文件列表
            List<String> needBackupFileList = new ArrayList<String>();
            for (File file : uploadFileList){
                String filename = file.getName();
                if (filesOnServerList.contains(filename)){
                    needBackupFileList.add(filename);
                }
            }

            // 备份文件
            if (needBackupFileList.size() != 0){
                transfer.backupFileOnServer(needBackupFileList);
            }

            // 上传文件
            transfer.uploadFile(uploadFileList);

            // 关闭连接
            transfer.closeConnection();


        } else {
            System.out.println("*** 程序执行失败,请检查后重新执行");
        }
    }
}

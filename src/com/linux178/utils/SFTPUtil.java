package com.linux178.utils;


import com.jcraft.jsch.*;
import com.linux178.models.ServerInfo;
import com.linux178.models.UploadInfo;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;


public class SFTPUtil implements Transfer {

    private UploadInfo uploadInfo;
    private Session session;
    private ChannelSftp channelSftp;

    public SFTPUtil(ServerInfo serverInfo,UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
        JSch jSch = new JSch();
        try {
            session = jSch.getSession(serverInfo.getUsername(),serverInfo.getIp(),serverInfo.getPort());
            session.setPassword(serverInfo.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("userauth.gssapi-with-mic", "no");
            session.connect();
            session.setTimeout(3000);
            session.sendKeepAliveMsg();
            channelSftp = (ChannelSftp)session.openChannel("sftp");
            channelSftp.setEnv("LC_MESSAGES", "en_US.UTF-8");
            channelSftp.connect();
            System.out.println("--> 登录到服务器 [ 成功 ]");
        } catch (Exception e){
            System.out.println("*** SFTP客户端出错了,原因: " + e.getMessage());
            System.exit(1);
        }
    }


    /**
     * 列出服务器上指定目录下的文件
     * @param path 服务器上的目录
     * @return 文件名列表
     */
    public List<String> listSpecifyDirOnServer(String path){
        List<String> filesOnServerList = new ArrayList<String>();
        try {
            Vector lsCmdOnServer = channelSftp.ls(path);
            for (Object object : lsCmdOnServer){
                String[] result = String.valueOf(object).split("\\s+");
                String filename = result[result.length - 1];
                filesOnServerList.add(filename);
            }
        } catch (Exception e) {
            System.out.println("*** SFTP客户端出错了,原因: " + e.getMessage());
            System.exit(1);
        }
        return filesOnServerList;
    }


    /**
     * 列出服务器上当前目录下的所有文件
     * @return 一个文件列表
     */
    @Override
    public List<String> getFileListOnServer() {
        return listSpecifyDirOnServer(uploadInfo.getDstPath());
    }


    /**
     * 备份服务器上指定的文件
     * @param needBackupFileList 备份文件列表
     */
    @Override
    public void backupFileOnServer(List<String> needBackupFileList) {

        String srcPath = uploadInfo.getDstPath();
        String backupPath = uploadInfo.getBackupPath();
        List<String> backupPathFileList = listSpecifyDirOnServer(srcPath + backupPath);
        System.out.println("--> 备份服务器上对应的文件");
        try {
            for (String filename : needBackupFileList){
                String fileInBackupPath = srcPath + backupPath + filename;
                if (backupPathFileList.contains(filename)){
                    channelSftp.rm(fileInBackupPath);
                }
                channelSftp.rename(srcPath + filename,fileInBackupPath);
            }
        } catch (Exception e){
            System.out.println("*** 备份出错了,请检查一下然后重新执行程序,原因: " + e.getMessage());
            System.exit(1);
        }
    }


    /**
     * 上传文件到服务器
     * @param uploadFileList 要上传的文件列表
     */
    @Override
    public void uploadFile(Collection<File> uploadFileList) {
        System.out.println("--> 上传文件");
        System.out.println();
        List<String> downloadUrlList = new ArrayList<String>();
        try {
            for (File file : uploadFileList){
                String filePath = file.getPath();
                String filename = file.getName();
                long fileSize = file.length();
                long startTime = System.currentTimeMillis();
                System.out.printf("--> 正在上传 [ %s ],大小: %s ,请稍等 ...\n",
                        filePath, FileUtils.byteCountToDisplaySize(fileSize));
                channelSftp.put(filePath, uploadInfo.getDstPath() + filename, new SFTPProcess(fileSize, startTime));
                downloadUrlList.add(String.format("%s%s", uploadInfo.getDownloadUrlPrefix(),filename));
                System.out.println("\n");
            }
            System.out.println("--> 本次的下载地址为:");
            for (String url : downloadUrlList){
                System.out.println(url);
            }
        } catch (Exception e) {
            System.out.printf("*** SFTP客户端出错了,原因: %s",e.getMessage());
            System.exit(1);
        }
    }


    /**
     * 关闭连接
     */
    @Override
    public void closeConnection() {

        if (channelSftp.isConnected()){
            channelSftp.disconnect();
        }
        if (session.isConnected()){
            session.disconnect();
        }
    }

}

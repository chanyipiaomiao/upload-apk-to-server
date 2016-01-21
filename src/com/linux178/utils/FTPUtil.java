package com.linux178.utils;


import com.linux178.models.ServerInfo;
import com.linux178.models.UploadInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FTPUtil implements Transfer {

    private FTPClient ftpClient;
    private UploadInfo uploadInfo;

    public FTPUtil(ServerInfo serverInfo,UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(serverInfo.getIp(), serverInfo.getPort());
            ftpClient.enterLocalPassiveMode(); //设置FTP的模式为被动模式,这一步很重要,不然列不出服务器上的文件
            ftpClient.login(serverInfo.getUsername(), serverInfo.getPassword());
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置上传文件时的格式为二进制文件
            ftpClient.setBufferSize(100000);  // 设置缓冲区大小,默认1024太小,会造成传输速度过慢
            ftpClient.setControlKeepAliveTimeout(300); //在控制连接上每5分钟发送活着的信号,不让其他设备断开连接
            System.out.println("--> 登录到服务器 [ 成功 ]");
        } catch (IOException e){
            System.out.println("*** FTP客户端出错了,原因,原因:" + e.getMessage());
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
            FTPFile[] files = ftpClient.listFiles(path);
            for (FTPFile file : files){
                filesOnServerList.add(file.getName());
            }
        } catch (IOException e) {
            System.out.println("*** 列出服务器上的文件列表失败,原因: " + e.getMessage());
            System.exit(1);
        }
        return filesOnServerList;
    }


    /**
     * 列出服务器上当前目录下的所有文件
     * @return 一个文件列表
     */
    public List<String> getFileListOnServer(){
        return listSpecifyDirOnServer(uploadInfo.getDstPath());
    }


    /**
     * 备份服务器上指定的文件
     * @param needBackupFileList 备份文件列表
     */
    public void backupFileOnServer(List<String> needBackupFileList){

        String srcPath = uploadInfo.getDstPath();
        String backupPath = uploadInfo.getBackupPath();
        List<String> backupPathFileList = listSpecifyDirOnServer(srcPath + backupPath);
        System.out.println("--> 备份服务器上的文件");
        try {
            for (String filename : needBackupFileList){
                String fileInBackupPath = srcPath + backupPath + filename;
                if (backupPathFileList.contains(filename)){
                    ftpClient.deleteFile(fileInBackupPath);
                }
                ftpClient.rename(srcPath + filename,srcPath + backupPath + filename);
            }
        } catch (IOException e){
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
        List<String> downloadUrlList = new ArrayList<String>();
        try {
            for (File file : uploadFileList){
                String filePath = file.getPath();
                String filename = file.getName();
                long fileSize = file.length();
                FileInputStream fileInputStream = new FileInputStream(file);
                long startTime = System.currentTimeMillis();
                ftpClient.setCopyStreamListener(new FTPProcess(fileSize,startTime));
                System.out.printf("--> 正在上传 [ %s ],大小: %s ,请稍等 ...\n",
                        filePath, FileUtils.byteCountToDisplaySize(fileSize));
                ftpClient.storeFile(filename, fileInputStream);
                IOUtils.closeQuietly(fileInputStream);
                downloadUrlList.add(String.format("%s%s", uploadInfo.getDownloadUrlPrefix(), filename));
                System.out.println("\n");
            }
            System.out.println("--> 本次的下载地址为:");
            for (String url : downloadUrlList){
                System.out.println(url);
            }
        } catch (Exception e) {
            System.out.printf("*** FTP客户端出错了,原因: %s",e.getMessage());
        }
    }


    /**
     * 关闭连接
     */
    public void closeConnection(){
        boolean error = false;
        if (ftpClient.isConnected()){
            try {
                ftpClient.disconnect();
            } catch (IOException e){
                System.out.printf("*** 关闭FTP连接出现异常,原因: %s",e.getMessage());
                error = true;
            }
        }
        System.exit(error ? 1 : 0);
    }
}

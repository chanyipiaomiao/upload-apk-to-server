package com.linux178.utils;


import com.jcraft.jsch.SftpProgressMonitor;


/**
 * SFTP传输进度显示
 */
public class SFTPProcess implements SftpProgressMonitor {

    private long fileSize;
    private long startTime;
    private long totalBytesTransferred = 0;

    public SFTPProcess(long fileSize, long startTime) {
        this.fileSize = fileSize;
        this.startTime = startTime;
    }

    @Override
    public void init(int i, String s, String s1, long l) {
    }

    /**
     * 本次传输了多少字节
     * @param count 本次传输了多少字节
     * @return true 继续传输，false 取消传输
     */
    @Override
    public boolean count(long count) {
        totalBytesTransferred += count;
        long end_time = System.currentTimeMillis();
        long time = (end_time - startTime) / 1000; // 耗时多长时间
        long speed; // 传输速度
        if (0 == time){
            speed = 0;
        } else {
            speed = totalBytesTransferred /1024/time;
        }
        System.out.printf("\r    %d%%   %d  %dKB/s  %d   %ds",
                totalBytesTransferred * 100 /fileSize, totalBytesTransferred,speed,fileSize,time);
        return true;
    }

    @Override
    public void end() {
    }
}

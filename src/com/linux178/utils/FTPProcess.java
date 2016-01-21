package com.linux178.utils;


import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;


/**
 * FTP传输进度显示
 */
public class FTPProcess implements CopyStreamListener {

    private long fileSize;
    private long startTime;

    public FTPProcess(long fileSize,long startTime) {
        this.fileSize = fileSize;
        this.startTime = startTime;
    }

    @Override
    public void bytesTransferred(CopyStreamEvent copyStreamEvent) {

    }

    /**
     * 本次传输了多少字节
     * @param totalBytesTransferred 到目前为止已经传输了多少字节
     * @param bytesTransferred 本次传输的字节数
     * @param streamSize The number of bytes in the stream being copied
     */
    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        long end_time = System.currentTimeMillis();
        long time = (end_time - startTime) / 1000; // 耗时多长时间
        long speed; //速度
        if (0 == time){
            speed = 0;
        } else {
            speed = totalBytesTransferred/1024/time;
        }
        System.out.printf("\r    %d%%   %d  %dKB/s  %d   %ds",
                totalBytesTransferred * 100 /fileSize,totalBytesTransferred,speed,fileSize,time);
    }
}

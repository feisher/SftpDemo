package com.feisherlpf.sftpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jcraft.jsch.SftpException;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SFTPUtils sftp;
    private String localPath= "sdcard/download/";
    private String remotePath = "/shared/jk_player";
    public ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
//                        SFTPUtils sftp =new SFTPUtils("192.9.198.214:22", "sftp-user","sa");
                        try {
                            sftp = new SFTPUtils("192.9.198.214",22, "sftp-user","sa");
                            sftp.connect();
                            Log.d(TAG,"连接成功");
                            sftp.downloadFile("1212", remotePath, "yssp.mp4", localPath, "ys.mp4", new SFTPUtils.ProgressListener(){

                                @Override
                                public void progress(String resId, long remoteFileLength, long localFileLength, long percent) {
                                    //  2018/7/5 下载进度监听
                                    Log.d(TAG," 服务器文件大小："+remoteFileLength+" 下载本地文件大小："+localFileLength + " 下载百分比："+ percent + "%");
                                }
                            });
                            Log.d(TAG,"下载成功 ,下载百分百：100%");
                            sftp.disconnect();
                        } catch (SftpException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        if (sftp!=null){
            sftp.disconnect();
        }
        super.onDestroy();
    }
}

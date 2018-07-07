package com.feisherlpf.sftpdemo;

import android.annotation.SuppressLint;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
* explin:sftp工具类，用法：
 *  sftp =new SFTPUtils("SFTP服务器IP", "用户名","密码")
 *  String localPath = "sdcard/download/";
    String remotePath = "test";
    sftp.connect();
    Log.d(TAG,"连接成功");
    sftp.downloadFile(remotePath, "APPInfo.xml", localPath, "APPInfo.xml");
    Log.d(TAG,"下载成功");
    sftp.disconnect();
* auther:lipengfei
* create by ${data}
*/
public class SFTPUtils {
      
    private String TAG="SFTPUtils";  
    private String host;  
    private String username;  
    private String password;  
    private static int port = 22;
    private ChannelSftp sftp = null;
    private Session sshSession = null;
    private Channel channel;

    /**
     *  注意使用要在子线程中
     * @param host 服务器地址 不需要前缀 例如："192.9.198.214"
     * @param port 端口号  默认 22
     * @param username 用户名
     * @param password
     */
    public SFTPUtils(String host, int port , String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;  
        this.password = password;  
    }
    /**
     *  注意使用要在子线程中
     * @param host 服务器地址 不需要前缀 例如："192.9.198.214"
     *  port 端口号  默认 22
     * @param username 用户名
     * @param password 密码
     */
    public SFTPUtils(String host, String username, String password) {
       this(host,port,username,password);
    }
    /** 
     * connect server via sftp 
     */  
    public ChannelSftp connect() {
        JSch jsch = new JSch();
        try {  
            sshSession = jsch.getSession(username, host, port);  
            sshSession.setPassword(password);  
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");  
            sshSession.setConfig(sshConfig);
            sshSession.connect();

            channel = sshSession.openChannel("sftp");
            if (channel != null) {
                channel.connect();
            } else {  
                Log.e(TAG, "channel connecting failed.");
            }
            sftp = (ChannelSftp) channel;
            Log.e(TAG, "sftp 创建成功.");
        } catch (JSchException e) {
            e.printStackTrace();
            Log.e(TAG, "sftp 创建失败."+e.getMessage());
        }  
        return sftp;  
    }
    public  boolean isConnected(){
        if (channel != null) {
            return channel.isConnected();
        }
        return false;
    }


    /**
     * 单个文件下载
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean downloadFile(final String resId, String remotePath, String remoteFileName,
                                String localPath, String localFileName, final ProgressListener progressListener) throws SftpException, FileNotFoundException {
        sftp.cd(remotePath);
//        final File file = new File(localPath + localFileName);
//        mkdirs(localPath + localFileName);
//        final FileOutputStream fileOutputStream = new FileOutputStream(file);

        SftpProgressMonitor monitor = new MyProgressMonitor(); //显示进度
        sftp.get(remoteFileName,localPath + localFileName,new MyProgressMonitor(){
            private long count = 0;     //当前接收的总字节数
            private long max = 0;       //最终文件大小
            private long percent = -1;  //进度

            /**
             * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
             */
            @Override
            public boolean count(long count) {
                this.count += count;
                if (percent >= this.count * 100 / max) {
                    return true;
                }
                percent = this.count * 100 / max;
                progressListener.progress(resId,max,this.count,percent);
                Log.d(TAG,"Completed " + this.count + "(" + percent + "%) out of " + max + ".");
                return true;
            }

            /**
             * 当传输结束时，调用end方法
             */
            @Override
            public void end() {
                Log.d(TAG,"Transferring done.");
            }

            /**
             * 当文件开始传输时，调用init方法
             */
            @Override
            public void init(int op, String src, String dest, long max) {
                if (op== SftpProgressMonitor.PUT) {
                    Log.d(TAG,"Upload file begin.");
                }else {
                    Log.d(TAG,"Download file begin.");
                }
                this.max = max;
                this.count = 0;
                this.percent = -1;
            }
        }, ChannelSftp.RESUME );
//        timer.cancel();
        return true;

    }

/**
 * 断开服务器
 */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                Log.d(TAG,"sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                Log.d(TAG,"sshSession is closed already");
            }
        }
    }

    /**
     * 单个文件上传
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName,
            String localPath, String localFileName) {
        FileInputStream in = null;
        try {
            createDir(remotePath);
            System.out.println(remotePath);
            File file = new File(localPath + localFileName);
            in = new FileInputStream(file);
            System.out.println(in);
            sftp.put(in, remoteFileName);
            System.out.println(sftp);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 批量上传
     * @param remotePath
     * @param localPath
     * @param del
     * @return
     */
    public boolean bacthUploadFile(String remotePath, String localPath,
            boolean del) {
        try {
            File file = new File(localPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()
                        && files[i].getName().indexOf("bak") == -1) {
                    synchronized(remotePath){
                        if (this.uploadFile(remotePath, files[i].getName(),
                            localPath, files[i].getName())
                            && del) {
                        deleteFile(localPath + files[i].getName());
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;

    }

    /**
     * 批量下载文件
     *
     * @param remotPath
     *            远程下载目录(以路径符号结束)
     * @param localPath
     *            本地保存目录(以路径符号结束)
     * @param fileFormat
     *            下载文件格式(以特定字符开头,为空不做检验)
     * @param del
     *            下载后是否删除sftp文件
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean batchDownLoadFile(String remotPath, String localPath,
            String fileFormat, boolean del) {
        try {
            connect();
            Vector v = listFiles(remotPath);
            if (v.size() > 0) {

                Iterator it = v.iterator();
                while (it.hasNext()) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) it.next();
                    String filename = entry.getFilename();
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        if (fileFormat != null && !"".equals(fileFormat.trim())) {
                            if (filename.startsWith(fileFormat)) {
                                if (this.downloadFile(remotPath, filename,
                                        localPath, filename)
                                        && del) {
                                    deleteSFTP(remotPath, filename);
                                }
                            }
                        } else {
                            if (this.downloadFile(remotPath, filename,
                                    localPath, filename)
                                    && del) {
                                deleteSFTP(remotPath, filename);
                            }
                        }
                    }
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;
    }
    /**
     * 进度监控器-JSch每次传输一个数据块，就会调用count方法来实现主动进度通知
     *
     */
    public static class MyProgressMonitor implements SftpProgressMonitor {
        private long count = 0;     //当前接收的总字节数
        private long max = 0;       //最终文件大小
        private long percent = -1;  //进度

        /**
         * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
         */
        @Override
        public boolean count(long count) {
            this.count += count;
            if (percent >= this.count * 100 / max) {
                return true;
            }
            percent = this.count * 100 / max;

            System.out.println("Completed " + this.count + "(" + percent
                    + "%) out of " + max + ".");
            return true;
        }

        /**
         * 当传输结束时，调用end方法
         */
        @Override
        public void end() {
            System.out.println("Transferring done.");
        }

        /**
         * 当文件开始传输时，调用init方法
         */
        @Override
        public void init(int op, String src, String dest, long max) {
            if (op== SftpProgressMonitor.PUT) {
                System.out.println("Upload file begin.");
            }else {
                System.out.println("Download file begin.");
            }

            this.max = max;
            this.count = 0;
            this.percent = -1;
        }
    }

    /**
     * 单个文件下载
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean downloadFile( String remotePath, String remoteFileName, String localPath, String localFileName) {
        try {
            sftp.cd(remotePath);
            final File file = new File(localPath + localFileName);
            mkdirs(localPath + localFileName);
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            sftp.get(remoteFileName,fileOutputStream );
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** 
     * 删除文件 
     * @param filePath 
     * @return 
     */  
    public boolean deleteFile(String filePath) {  
        File file = new File(filePath);  
            if (!file.exists()) {  
                return false;  
            }  
            if (!file.isFile()) {  
                return false;  
            }  
            return file.delete();  
        }  
          
    public boolean createDir(String createpath) {  
        try {  
            if (isDirExist(createpath)) {  
                this.sftp.cd(createpath);  
                Log.d(TAG,createpath);  
                return true;  
            }  
            String pathArry[] = createpath.split("/");  
            StringBuffer filePath = new StringBuffer("/");  
            for (String path : pathArry) {  
                if (path.equals("")) {  
                    continue;  
                }  
                filePath.append(path + "/");  
                    if (isDirExist(createpath)) {  
                        sftp.cd(createpath);  
                    } else {  
                        sftp.mkdir(createpath);  
                        sftp.cd(createpath);  
                    }  
                }  
                this.sftp.cd(createpath);  
                  return true;  
            } catch (SftpException e) {
                e.printStackTrace();  
            }  
            return false;  
        }  
  
    /** 
     * 判断目录是否存在 
     * @param directory 
     * @return 
     */  
    @SuppressLint("DefaultLocale")
    public boolean isDirExist(String directory) {  
        boolean isDirExistFlag = false;  
        try {  
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;  
            return sftpATTRS.isDir();  
        } catch (Exception e) {  
            if (e.getMessage().toLowerCase().equals("no such file")) {  
                isDirExistFlag = false;  
            }  
        }  
        return isDirExistFlag;  
        }  
      
    public void deleteSFTP(String directory, String deleteFile) {  
        try {  
            sftp.cd(directory);  
            sftp.rm(deleteFile);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * 创建目录 
     * @param path 
     */  
    public void mkdirs(String path) {  
        File f = new File(path);  
        String fs = f.getParent();  
        f = new File(fs);  
        if (!f.exists()) {  
            f.mkdirs();  
        }  
    }  
  
    /** 
     * 列出目录文件 
     * @param directory 
     * @return 
     * @throws SftpException
     */  
      
    @SuppressWarnings("rawtypes")  
    public Vector listFiles(String directory) throws SftpException {
        return sftp.ls(directory);  
    }

    public interface ProgressListener {
        void progress(String resId, long remoteFileLength, long localFileLength, long percent);
    }
      
}  
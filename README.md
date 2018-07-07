# SftpDemo
sftp下载测试demo，含有上传相关代码，支持断点续传(上传+下载)，
## 文件下载

```java
public static void downloadFile(String src, String dst,
                                  Map<String, String> sftpDetails) throws Exception{
        SftpUtil sftpUtil = new SftpUtil();
        ChannelSftp chSftp = sftpUtil.getChannel(sftpDetails, 160000);
        try {
        	//此处演示 断点续传，下方完整代码中有其他方式
            chSftp.get(src, dst, new MyProgressMonitor(),ChannelSftp.RESUME); //断点续传
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chSftp.quit();
            sftpUtil.closeChannel();
        }
    }
```


------

## 测试断点续传



```java
 public static void main(String[] arg) throws Exception {
        // 设置主机ip，端口，用户名，密码
        Map<String, String> sftpDetails = new HashMap<String, String>();
        sftpDetails.put(SFTP_REQ_HOST, "192.9.198.214");
        sftpDetails.put(SFTP_REQ_USERNAME, "sftp-user");
        sftpDetails.put(SFTP_REQ_PASSWORD, "sa");
        sftpDetails.put(SFTP_REQ_PORT, "22");

        //测试文件上传
        String src = "C:\\xxx\\TMP\\site-1.10.4.zip"; // 本地文件名
        String dst = "/tmp/sftp/"; // 目标文件名
        uploadFile(src, dst, sftpDetails);

        //测试文件下载
        String srcFilename = "/tmp/sftp/site-1.10.4.zip";
        String dstFilename = "C:\\tmp\\site-1.10.4-new.zip";
        downloadFile(srcFilename, dstFilename, sftpDetails);

    }

```

[图片上传失败...(image-27c442-1530930487193)][图片上传失败...(image-6b09e6-1530930487193)]

------

## 完整程序

```java
package com.feisherlpf.sftpdemo.SFTPUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by feisher(lpf) on 2018/7/7.
 */

public class SftpUtil {
    Session session = null;
    Channel channel = null;
    public static final String SFTP_REQ_HOST = "host";
    public static final String SFTP_REQ_PORT = "port";
    public static final String SFTP_REQ_USERNAME = "username";
    public static final String SFTP_REQ_PASSWORD = "password";
    public static final int SFTP_DEFAULT_PORT = 22;
    public static final String SFTP_REQ_LOC = "location";
    /**
     * 测试程序
     * @param arg
     * @throws Exception
     */
    public static void main(String[] arg) throws Exception {
        // 设置主机ip，端口，用户名，密码
        Map<String, String> sftpDetails = new HashMap<String, String>();
        sftpDetails.put(SFTP_REQ_HOST, "192.9.198.214");
        sftpDetails.put(SFTP_REQ_USERNAME, "sftp-user");
        sftpDetails.put(SFTP_REQ_PASSWORD, "sa");
        sftpDetails.put(SFTP_REQ_PORT, "22");

        //测试文件上传
        String src = "C:\\xxx\\TMP\\site-1.10.4.zip"; // 本地文件名
        String dst = "/tmp/sftp/"; // 目标文件名
        uploadFile(src, dst, sftpDetails);

        //测试文件下载
        String srcFilename = "/tmp/sftp/site-1.10.4.zip";
        String dstFilename = "C:\\tmp\\site-1.10.4-new.zip";
        downloadFile(srcFilename, dstFilename, sftpDetails);

    }

	public static void downloadFile(String src, String dst,
                                  Map<String, String> sftpDetails) throws Exception{
        SftpUtil sftpUtil = new SftpUtil();
        ChannelSftp chSftp = sftpUtil.getChannel(sftpDetails, 160000);
        // Retrieves the file attributes of a file or directory
        // SftpATTRS attr = chSftp.stat(src);
        // long fileSize = attr.getSize();
        //代码段1/代码段2/代码段3:分别演示了如何使用JSch的各种put方法来进行文件下载
        try {
            // 代码段1:使用这个方法时，dst可以是目录，若dst为目录，则下载到本地的文件名将与src文件名相同
            chSftp.get(src, dst, new MyProgressMonitor(),ChannelSftp.RESUME); //断点续传
            /***
             OutputStream out = new FileOutputStream(dst);
             // 代码段2:将目标服务器上文件名为src的文件下载到本地的一个输出流对象，该输出流为一个文件输出流
             chSftp.get(src, out, new MyProgressMonitor());

             // 代码段3:采用读取get方法返回的输入流数据的方式来下载文件
             InputStream is = chSftp.get(src, new MyProgressMonitor(),ChannelSftp.RESUME);
             byte[] buff = new byte[1024 * 2];
             int read;
             if (is != null) {
             do {
             read = is.read(buff, 0, buff.length);
             if (read > 0) {
             out.write(buff, 0, read);
             }
             out.flush();
             } while (read >= 0);
             }
             **/

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chSftp.quit();
            sftpUtil.closeChannel();
        }
    }

    public static void uploadFile(String src, String dst,
                                  Map<String, String> sftpDetails) throws Exception {
        SftpUtil sftpUtil = new SftpUtil();
        ChannelSftp chSftp = sftpUtil.getChannel(sftpDetails, 60000);
        /**
         * 代码段1/代码段2/代码段3分别演示了如何使用JSch的不同的put方法来进行文件上传。这三段代码实现的功能是一样的，
         * 都是将本地的文件src上传到了服务器的dst文件
         */
        /**代码段1
         OutputStream out = chSftp.put(dst,new MyProgressMonitor2(), ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
         byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
         int read;
         if (out != null) {
         InputStream is = new FileInputStream(src);
         do {
         read = is.read(buff, 0, buff.length);
         if (read > 0) {
         out.write(buff, 0, read);
         }
         out.flush();
         } while (read >= 0);
         }
         **/

        // 使用这个方法时，dst可以是目录，当dst是目录时，上传后的目标文件名将与src文件名相同
        // ChannelSftp.RESUME：断点续传
        chSftp.put(src, dst, new MyProgressMonitor(), ChannelSftp.RESUME); // 代码段2
        // 将本地文件名为src的文件输入流上传到目标服务器，目标文件名为dst。
        // chSftp.put(new FileInputStream(src), dst,new MyProgressMonitor2(), ChannelSftp.OVERWRITE); // 代码段3

        chSftp.quit();
        sftpUtil.closeChannel();
    }
    /**
     * 根据ip，用户名及密码得到一个SFTP
     * channel对象，即ChannelSftp的实例对象，在应用程序中就可以使用该对象来调用SFTP的各种操作方法
     *
     * @param sftpDetails
     * @param timeout
     * @return
     * @throws JSchException
     */
    public ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout)
            throws JSchException {
        String ftpHost = sftpDetails.get(SFTP_REQ_HOST);
        String port = sftpDetails.get(SFTP_REQ_PORT);
        String ftpUserName = sftpDetails.get(SFTP_REQ_USERNAME);
        String ftpPassword = sftpDetails.get(SFTP_REQ_PASSWORD);
        int ftpPort = SFTP_DEFAULT_PORT;
        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port);
        }
        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
        if (ftpPassword != null) {
            session.setPassword(ftpPassword); // 设置密码
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config); // 为Session对象设置properties
        session.setTimeout(timeout); // 设置timeout时间
        session.connect(5000); // 通过Session建立链接
        channel = session.openChannel("sftp"); // 打开SFTP通道
        channel.connect(); // 建立SFTP通道的连接
        return (ChannelSftp) channel;
    }
    public void closeChannel() throws Exception {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
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
            if (op==SftpProgressMonitor.PUT) {
                System.out.println("Upload file begin.");
            }else {
                System.out.println("Download file begin.");
            }

            this.max = max;
            this.count = 0;
            this.percent = -1;
        }
    }

}
```

------
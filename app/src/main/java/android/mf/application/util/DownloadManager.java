package android.mf.application.util;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadManager extends Thread {
    private String TAG = "DownloadManager";

    private Context context = null;
    private URL downloadHttpUrl = null;
    private String downloadSavePath = null;
    private String downloadFileNmae = null;
    private int threadNumber = 3;
    private int gBlockSize = 0;
    private Handler MessageHandler = null;

    public DownloadManager(Context context) {
        this.context = context;
    }

    public void setFileHttpUrl(String arguments) throws MalformedURLException {
        this.downloadHttpUrl = new URL(arguments);
    }

    public void setSavePath(String arguments) {
        this.downloadSavePath = arguments;
    }

    public void setFileNmae(String arguments) {
        this.downloadFileNmae = arguments;
    }

    public void setThreadNumber(int arguments) {
        this.threadNumber = arguments;
    }

    public void setMessageHandler(Handler msg) {
        if (MessageHandler == null) {
            this.MessageHandler = msg;
        } else {
            if (MessageHandler != msg) {
                this.MessageHandler = msg;
            }
        }
    }

    @Override
    public void run() {
        ThreadManagers[] threadManagers = new ThreadManagers[threadNumber];
        try {
            URLConnection conn = downloadHttpUrl.openConnection();
            int fileSize = conn.getContentLength();
            if (fileSize <= 0) {
                System.out.println("读取文件失败");
                return;
            }
            // 计算每条线程下载的数据长度
            gBlockSize = (fileSize % threadNumber) == 0 ? fileSize / threadNumber : fileSize / threadNumber + 1;
            File file = new File(downloadSavePath + downloadFileNmae);
            for (int i = 0; i < threadManagers.length; i++) {
                // 启动线程，分别下载每个线程需要下载的部分
                threadManagers[i] = new ThreadManagers(gBlockSize, (i + 1));
                threadManagers[i].setName(TAG + ":" + i);
                threadManagers[i].start();
            }
            boolean isfinished = false;
            int downloadedAllSize = 0;
            while (!isfinished) {
                isfinished = true;
                downloadedAllSize = 0;
                for (int i = 0; i < threadManagers.length; i++) {
                    downloadedAllSize += threadManagers[i].getDownloadLength();
                    if (!threadManagers[i].isCompleted()) {
                        isfinished = false;
                    }
                }
                float num = (float) downloadedAllSize / (float) fileSize;
                if ((int) (num * 100) == 100) {
                    /*CommandManager CMDmr =new CommandManager(context);
                    ArrayList<String> cmd = new ArrayList<>();
                    cmd.add("chmod 604 "+downloadSavePath+downloadFileNmae);
                    CMDmr.executeCommand(cmd);*/
                   /* Intent IntentgService = new Intent(context, MainService.class);
                    IntentgService.putExtra("Key","FileMessage");
                    IntentgService.putExtra("Content",file.getAbsolutePath());
                    context.startService(IntentgService);*/
                }
                Thread.sleep(1000);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ThreadManagers extends Thread {

        private static final String TAG = "ThreadManagers";
        private boolean isCompleted = false;
        private int downloadLength = 0;
        private int threadId;
        private int blockSize;

        public ThreadManagers(int blocksize, int threadId) {
            this.threadId = threadId;
            this.blockSize = blocksize;
        }

        @Override
        public void run() {
            BufferedInputStream bis = null;
            RandomAccessFile raf = null;
            try {
                URLConnection conn = downloadHttpUrl.openConnection();
                conn.setAllowUserInteraction(true);
                int startPos = blockSize * (threadId - 1);
                int endPos = blockSize * threadId - 1;
                conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                //System.out.println(Thread.currentThread().getName() + "  bytes=" + startPos + "-" + endPos);
                //TestLog.Content(TAG,56,"测试");
                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(conn.getInputStream());
                raf = new RandomAccessFile(new File(downloadSavePath + downloadFileNmae), "rwd");
                raf.seek(startPos);
                int len;
                while ((len = bis.read(buffer, 0, 1024)) != -1) {
                    raf.write(buffer, 0, len);
                    downloadLength += len;
                }
                isCompleted = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 线程文件是否下载完毕
         */
        private boolean isCompleted() {
            return isCompleted;
        }

        /**
         * 线程下载文件长度
         */
        private int getDownloadLength() {
            return downloadLength;
        }
    }
}

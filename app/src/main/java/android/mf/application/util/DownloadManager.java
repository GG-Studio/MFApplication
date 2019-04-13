package android.mf.application.util;


import android.content.Context;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

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

    public void stopDownload() {

    }
    public void startDownload(String[] downloadArguments, int threadNumber) throws MalformedURLException {
        this.downloadHttpUrl = new URL(downloadArguments[0]);
        this.downloadSavePath = downloadArguments[1];
        this.downloadFileNmae = downloadArguments[2];
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        ThreadManagers[] threadManagers = new ThreadManagers[threadNumber];
        try {
            URLConnection conn = downloadHttpUrl.openConnection();
            int fileSize = conn.getContentLength();
            if (fileSize <= 0) {
                Logcat.Companion.e(TAG, "读取文件失败！");
                Message msg = new Message();
                msg.arg1 = 0;
                MessageHandler.sendMessage(msg);
                return;
            }
            // 计算每条线程下载的数据长度
            gBlockSize = (fileSize % threadNumber) == 0 ? fileSize / threadNumber : fileSize / threadNumber + 1;
            File file = new File(downloadSavePath + downloadFileNmae);
            for (int i = 0; i < threadManagers.length; i++) {
                // 启动线程，分别下载每个线程需要下载的部分
                threadManagers[i] = new ThreadManagers(gBlockSize, (i + 1));
                threadManagers[i].setName("MiUiDownloadManagers:" + i);
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
                Logcat.Companion.i(TAG, (int) (num * 100));
                if ((int) (num * 100) == 100) {
                    Logcat.Companion.i(TAG, "下载完成！");
                    Message msg = new Message();
                    msg.arg1 = 1;
                    msg.obj = downloadFileNmae;
                    MessageHandler.sendMessage(msg);
                   /* CommandManager CMDmr =new CommandManager(context);
                    ArrayList<String> cmd = new ArrayList<>();
                    cmd.add("chmod 604 "+downloadSavePath+downloadFileNmae);
                    CMDmr.executeCommand(cmd);
                    Intent IntentgService = new Intent(context, FileService.class);
                    IntentgService.putExtra("Key","operate");
                    IntentgService.putExtra("Task",1);
                    IntentgService.putExtra("Content","");
                    context.startService(IntentgService);*/
                }
                Thread.sleep(1000);
            }
        } catch (MalformedURLException e) {
            Logcat.Companion.e(TAG, e.getMessage());
        } catch (IOException e) {
            Logcat.Companion.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            Logcat.Companion.e(TAG, e.getMessage());
        }
    }

    private class ThreadManagers extends Thread {

        private static final String TAG = "DThreadManagers";
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
                Logcat.Companion.e(TAG, e.getMessage());
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        Logcat.Companion.e(TAG, e.getMessage());
                    }
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        Logcat.Companion.e(TAG, e.getMessage());
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

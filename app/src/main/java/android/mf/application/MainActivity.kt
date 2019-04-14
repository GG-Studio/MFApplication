package android.mf.application

import android.content.Context
import android.content.Intent
import android.mf.application.service.AwakenService
import android.mf.application.service.XposedTaskService
import android.mf.application.ui.LogcatUI
import android.mf.application.util.CommandManager
import android.mf.application.util.Logcat
import android.os.Bundle
import android.provider.SyncStateContract
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.*
import java.util.ArrayList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.system.Os.mkdir
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private var TAG: String = "MainActivity"
    private val FunctionNames = arrayOf("查看日志", "复制文件到Data", "检测是否有Dex文件", "删除Dex文件", "唤醒服务", "任务服务", "重启手机", "结束自身")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val FunctionAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, FunctionNames)
        TestFunction.setAdapter(FunctionAdapter)
        TestFunction.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    val intent = Intent(this@MainActivity, LogcatUI::class.java)
                    startActivity(intent)
                }

                1 -> {
                    val appDir =
                        File(Environment.getExternalStorageDirectory().toString() + "/MFAppDex_v1.0.jar").toString()
                    val file = File(getFilesDir().getAbsolutePath() + "/MFAppDex_v1.0.jar").toString()
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("mv '" + appDir + "' '" + file + "'")
                    Logcat.i(TAG, CMD.executeCommand(cmd))
                }
                2 -> {
                    var DexPath = getFilesDir().getAbsolutePath() + "/MFAppDex_v1.0.jar"
                    var file: File = File(DexPath)
                    if (!file.exists()) {
                        Logcat.i(TAG, "文件不存在！")
                        copyFiles(this, "MFAppDex_v1.0.jar", file)
                    } else {
                        Logcat.i(TAG, "文件存在！")
                    }
                }
                3 -> {
                    var DexPath = getFilesDir().getAbsolutePath() + "/MFAppDex_v1.0.jar"
                    var file: File = File(DexPath)
                    if (file.exists()) {
                        if (file.delete()) {
                            Logcat.i(TAG, "文件已删除！")
                        }
                    } else {
                        Logcat.i(TAG, "文件不存在！")
                    }
                }
                4 -> {
                    val TestTask = "123"
                    val intentService = Intent(this@MainActivity, AwakenService::class.java)
                    intentService.putExtra("Key", "Xposed")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                    val appDir = File(Environment.getExternalStorageDirectory(), "DCIM/WeChatSns")
                    val TestC = appDir.path
                    Toast.makeText(this, TestC, Toast.LENGTH_LONG).show()
                }
                5 -> {
                    val TestA =
                        "http://t-1.tuzhan.com/1604dbf01333/c-1/l/2012/09/20/19/eadbcc15d12d4100ac1de2fb787d4ae2.jpg"
                    val TestB = ""
                    val appDir = File(Environment.getExternalStorageDirectory(), "DCIM/WeChatSns")
                    val TestC = appDir.path
                    if (!appDir.exists()) {
                        appDir.mkdir()
                    }
                    val TestTask =
                        "[{\"ScriptVersion\":1.0}," +
                                "{\"AppName\":\"WeChat\",\"AppVersions\":7.3}," +
                                "{\"FileArguments\":[{\"Url\":\"" + TestA + "\",\"Name\":\"Chen\",\"SavePath\":\"" + TestC + "\"}," +
                                "{\"Url\":\"" + TestA + "\",\"Name\":\"Guo\",\"SavePath\":\"" + TestC + "\"}," +
                                "{\"Url\":\"" + TestA + "\",\"Name\":\"Gang\",\"SavePath\":\"" + TestC + "\"}]}," +
                                "{\"Operate\":1}]"

                    val intentService = Intent(this@MainActivity, XposedTaskService::class.java)
                    intentService.putExtra("Key", "Task")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                }
                6 -> {
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("reboot")
                    CMD.executeCommand(cmd)
                }
                7 -> {
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("an force-stop " + SyncStateContract.Constants.ACCOUNT_NAME)
                    CMD.executeCommand(cmd)
                }
            }
        }
        fab.setOnClickListener { view ->

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun copyFiles(context: Context, fileName: String, desFile: File) {
        var ins: InputStream? = null
        var out: OutputStream? = null
        try {
            ins = context.getApplicationContext().getAssets().open(fileName)
            out = FileOutputStream(desFile.getAbsolutePath())
            val bytes = ByteArray(1024)
            var len: Int = 0
            while (ins.read(bytes) != -1) {
                len = ins.read(bytes)
                out!!.write(bytes, 0, len)
                Logcat.i(TAG, "复制进度: " + len)
            }
            out!!.flush()
        } catch (e: IOException) {
            Logcat.e(TAG, e.printStackTrace())
        } finally {
            try {
                if (ins != null)
                    ins!!.close()
                if (out != null)
                    out!!.close()
            } catch (e: IOException) {
                Logcat.e(TAG, e.printStackTrace())
            }
        }
    }

    private fun saveImageToGallery(context: Context, bmp: Bitmap) {
        val appDir = File(Environment.getExternalStorageDirectory(), "DCIM/WeChatSns")
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val path = file.getPath()
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$path")))
    }
}

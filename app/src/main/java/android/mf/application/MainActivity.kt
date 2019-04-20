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
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private var TAG: String = "MainActivity"
    private val FunctionNames = arrayOf("查看日志", "复制文件到Data", "检测是否有Dex文件", "删除Dex文件", "唤醒服务", "任务服务", "重启手机", "结束自身","打开朋友圈发送")
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

                }
                2 -> {

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
                }
                5 -> {
                    val TestA =
                        "https://development49.baidupan.com/2019041509bb/2019/04/15/ffcb188d6485b2800deee624aab77621.rar?st=nr9_1jIL-sUbJKtQGQpGnQ&q=Test.rar&e=1555294977&ip=118.113.132.126&fi=8081374&up=1."
                    val TestB = ""
                    val appDir = File(Environment.getExternalStorageDirectory(), "DCIM/WeChatSns")
                    Logcat.i(TAG,appDir)
                    val TestC = appDir.path
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
                    Logcat.i(TAG, CMD.executeCommand(cmd))
                }
                8 -> {

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



    private fun saveImageToGallery(context: Context, bmp: Bitmap) {
        val appDir = File(Environment.getExternalStorageDirectory(), "DCIM/WeChatSns/")
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

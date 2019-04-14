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

class MainActivity : AppCompatActivity() {

    private var TAG: String = "MainActivity"
    private val FunctionNames = arrayOf("查看日志", "检测是否有Dex文件", "删除Dex文件", "唤醒服务", "任务服务", "重启手机","结束自身")
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
                    var DexPath = getFilesDir().getAbsolutePath() + "/MFAppDex_v1.0.jar"
                    var file: File = File(DexPath)
                    if (!file.exists()) {
                        Logcat.i(TAG, "文件不存在！")
                        copyFiles(this, "MFAppDex_v1.0.jar", file)
                    } else {
                        Logcat.i(TAG, "文件存在！")
                    }
                }
                2 -> {
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
                3 -> {
                    val TestTask = "123"
                    val intentService = Intent(this@MainActivity, AwakenService::class.java)
                    intentService.putExtra("Key", "Xposed")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                }
                4 -> {
                    val TestTask =
                    "[{\"ScriptVersion\":1.0},{\"AppName\":\"WeChat\",\"AppVersions\":7.3},{\"FileArguments\":[{\"Url\":\"http://\",\"Name\":\"Chen\",\"SavePath\":\"Chen\"},{\"Url\":\"http://\",\"Name\":\"Guo\",\"SavePath\":\"Guo\"},{\"Url\":\"http://\",\"Name\":\"Gang\",\"SavePath\":\"Gang\"}]},{\"Operate\":1}]"
                    val intentService = Intent(this@MainActivity, XposedTaskService::class.java)
                    intentService.putExtra("Key", "Task")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                }
                5 -> {
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("reboot")
                    CMD.executeCommand(cmd)
                }
                6 -> {
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("an force-stop "+ SyncStateContract.Constants.ACCOUNT_NAME)
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
}

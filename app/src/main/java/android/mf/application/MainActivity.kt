package android.mf.application

import android.content.Context
import android.content.Intent
import android.mf.application.service.AwakenService
import android.mf.application.service.XposedTaskService
import android.mf.application.ui.LogcatUI
import android.mf.application.util.CommandManager
import android.mf.application.util.Logcat
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var TAG: String = "MainActivity"
    private val FunctionNames = arrayOf("查看日志", "检测是否有Dex文件", "唤醒服务", "任务服务", "重启手机")
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
                    var DexPath = getFilesDir().getAbsolutePath() + "/SuperModule.dex";
                    var file: File = File(DexPath)
                    if (!file.exists()) {
                        Logcat.i(TAG,"文件不存在！")
                        //copyFiles(this, "MFAppDex_1.0.jar", file)
                    } else{
                        Logcat.i(TAG,"文件存在！")
                    }
                }
                2 -> {
                    val TestTask = "123"
                    val intentService = Intent(this@MainActivity, AwakenService::class.java)
                    intentService.putExtra("Key", "Xposed")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                }
                3 -> {
                    val TestTask =
                        "[{\"DexVersions\":\"1.0\"},{\"AppName\":\"WeChat\"},{\"FileUrl\":\"http://www.baidu.com\",\"FileName\":\"Chen\",\"FileSavePath\":\"/data/data/android.support.library/cache/\"},{\"TaskStep\":[{\"Step\":\"1\"},{\"Step\":\"2\"}]}]"
                    val intentService = Intent(this@MainActivity, XposedTaskService::class.java)
                    intentService.putExtra("Key", "Task")
                    intentService.putExtra("Content", TestTask)
                    this@MainActivity.startService(intentService)
                }
                4 -> {
                    var CMD = CommandManager(this)
                    var cmd = ArrayList<String>()
                    cmd.add("reboot")
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

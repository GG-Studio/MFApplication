package android.mf.application.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.mf.application.util.Logcat
import android.os.IBinder
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import java.io.*

class XposedTaskService : Service() {

    private val TAG = "XposedTaskService"
    private var Content: String? = null
    private var Task: ArrayList<Any>? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Content = intent.getStringExtra("Content")
        if (OperateTask(Content) > 0) {
            onCreateTask()
            onLogMsg(TAG, "xposedHook")
            this.stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        val intentService = Intent(this, AwakenService::class.java)
        intentService.putExtra("Key", "Destroy")
        intentService.putExtra("Content", "XposedTaskService")
        this.startService(intentService)
    }

    fun onCreateTask(): ArrayList<Any>? {
        return Task
    }

    private fun OperateTask(c: String?):Int {
        Task = ArrayList()
        try {
            var TaskJson = JSONArray(c)
            Task!!.add(TaskJson!!.getJSONObject(0).getDouble("ScriptVersion"))
            Task!!.add(TaskJson!!.getJSONObject(1).getString("AppName")) //操作APP
            Task!!.add(TaskJson!!.getJSONObject(1).getDouble("AppVersions")) //APP版本
            val File:ArrayList<Any> = ArrayList()
            val ScriptFileArguments = JSONArray(TaskJson!!.getJSONObject(2).getString("FileArguments"))
            for (i in 0 until ScriptFileArguments.length()) {
                val Arguments:ArrayList<String> = ArrayList()
                Arguments.add(ScriptFileArguments.getJSONObject(i).getString("Url"))
                Arguments.add(ScriptFileArguments.getJSONObject(i).getString("Name"))
                Arguments.add(ScriptFileArguments.getJSONObject(i).getString("SavePath"))
                File!!.add(Arguments)
            }
            Task!!.add(File)
            Task!!.add(TaskJson!!.getJSONObject(3).getInt("Operate")) //操作任务
            return Task!!.size
        } catch (e: JSONException) {
           Logcat.e(TAG,e.printStackTrace())
        }
        return 0
    }


    fun onLogMsg(tag: String, msg: Any) {
        Logcat.d(tag, msg)
    }
}
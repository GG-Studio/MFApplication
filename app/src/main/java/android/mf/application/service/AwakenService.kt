package android.mf.application.service


import android.app.Service
import android.content.Context
import android.content.Intent
import android.mf.application.util.Logcat
import android.os.IBinder

class AwakenService : Service() {

    private val TAG = "AwakenService"
    private var gIntent: Intent? = null
    private var isgIntent = false
    private var Key: String? = null
    private var Content: String? = null

    override fun onCreate() {
        super.onCreate()
        isgIntent = true
        object : Thread() {
            override fun run() {
                super.run()
                while (isgIntent) {
                    try {
                        if (gIntent != null) {
                            isgIntent = false
                        }
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        Logcat.e(TAG, e.message)
                    }

                }
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        gIntent = intent
        if (intent != null) {
            Key = intent.getStringExtra("Key")
            Content = intent.getStringExtra("Content")
            when (Key) {
                "Awaken" -> {
                    if (startId == 9) {
                        System.gc()
                    }
                    Logcat.i(TAG, Content)
                }
                "Xposed" -> {
                    onContext()
                    onLogMsg(TAG, "HookTest")
                }
                "Destroy" -> Logcat.i(TAG, Content)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Logcat.i(TAG, intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Logcat.i(TAG, "销毁")
    }

    // 调用
    fun onContext(): Context {

        return getApplicationContext()
    }

    fun onLogMsg(tag: String, msg: Any) {
        Logcat.d(tag, msg)
    }
}
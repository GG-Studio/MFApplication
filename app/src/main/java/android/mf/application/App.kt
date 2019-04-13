package android.mf.application

import android.app.Application
import android.mf.application.util.CommandManager
import android.mf.application.util.Logcat

class App : Application() {
    private var TAG: String = "App"
    private var CMD: CommandManager? = null
    override fun onCreate() {
        super.onCreate()
        CMD = CommandManager(this)
    }

    companion object {
        fun onXposedLogCat(tag: String, msg: Any) {
            Logcat.d(tag, msg)
        }
    }
}
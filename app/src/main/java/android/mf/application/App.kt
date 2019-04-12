package android.mf.application

import android.app.Application
import android.mf.application.util.Logcat

class App : Application() {
    private var TAG: String = "App"

    override fun onCreate() {
        super.onCreate()
        onMsg(TAG,"Test");

    }

    fun onMsg(tag:String,msg:Any) {
        Logcat.d(tag,msg)
    }
}
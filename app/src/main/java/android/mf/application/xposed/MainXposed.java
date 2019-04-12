package android.mf.application.xposed;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;
import java.util.ArrayList;

public class MainXposed implements IXposedHookLoadPackage {

    private static String TAG = "MainXposed";
    private static Context initContext = null;
    private static Context MoPfContext = null;
    private static String PfPackage = "android.mf.application";
    private static String PfApp = ".App";
    private static String PfAwakenService = ".service.AwakenService";
    private static String PfXposedTaskService = ".service.XposedTaskService";
    private static DexClassLoader dexClassLoader = null;
    private static File dexFile = null;
    private static String formDexPath = null;
    private static String loadDexPath = null;
    private static double dexVersions = 1.0;
    private static ArrayList<Object> Task = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (initContext == null) {
                            initContext = (Context) param.args[0];
                        } else if (!isServiceRunning(PfPackage + PfAwakenService)) {
                            AwakenParasitifer(new Intent());
                        }
                    }
                });
        if (lpparam.packageName.equals(PfPackage)) {

            XposedHelpers.findAndHookMethod(PfPackage + PfAwakenService,
                    lpparam.classLoader,
                    "onContext",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (MoPfContext == null) {
                                MoPfContext = (Context) param.args[0];
                            }
                        }
                    });
            XposedHelpers.findAndHookMethod(PfPackage + PfAwakenService,
                    lpparam.classLoader,
                    "onLogMsg",
                    String.class,
                    Object.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = TAG;
                            param.args[1] = initContext + "/-&&-/" + MoPfContext;
                        }
                    });
            XposedHelpers.findAndHookMethod(PfPackage + PfXposedTaskService,
                    lpparam.classLoader,
                    "onCreateTask",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (Task.size() <= 0) {
                                Task = (ArrayList<Object>) param.getResult();
                            }

                        }
                    });
        }
    }
/*
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //XposedBridge.log(TAG + "--->" + msg.obj);

            super.handleMessage(msg);
        }
    };*/

    //加载获取SuperLibrary应用
    private void AwakenParasitifer(Intent intentService) {
        ComponentName componentName = new ComponentName(PfPackage, PfPackage + PfAwakenService);
        intentService.setComponent(componentName);
        intentService.putExtra("Key", "Xposed");
        intentService.putExtra("Content", initContext.getPackageCodePath());
        initContext.startService(intentService);
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    private boolean isServiceRunning(String ServiceName) {
        if (TextUtils.isEmpty(ServiceName))
            return false;
        ActivityManager myManager = (ActivityManager) initContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }
}

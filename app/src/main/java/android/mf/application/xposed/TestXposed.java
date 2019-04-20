package android.mf.application.xposed;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.mf.application.util.SundryCodeManager;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class TestXposed implements IXposedHookLoadPackage {

    private static String TAG = "MainXposed";
    private static Context MoPfContext = null;
    private static String PfPackage = "android.mf.application";
    private static String PfApp = ".App";
    private static String PfAwakenService = ".service.AwakenService";
    private static String PfXposedTaskService = ".service.XposedTaskService";
    private static DexClassLoader dexClassLoader = null;
    private static Class AuxiliaryXposedClass = null;
    private static Object AuxiliaryXposed = null;
    private static Method onCreate = null;
    private static Method onInit = null;
    private static Method onHookLoadPackage = null;
    private static File dexFile = null;
    private static String formDexPath = null;
    private static String loadDexPath = null;
    private static boolean isUninstallDex = false;
    private static ArrayList<Object> Task = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(PfPackage)) {
            MoPf(lpparam);
        }
    }

    private void MoPf(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(PfPackage)) {
            XposedHelpers.findAndHookMethod(PfPackage + PfAwakenService,
                    lpparam.classLoader,
                    "onContext",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (MoPfContext == null) {
                                MoPfContext = (Context) param.getResult();
                            }
                        }
                    });
            XposedHelpers.findAndHookMethod(PfPackage + PfXposedTaskService,
                    lpparam.classLoader,
                    "onCreateTask",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Task = (ArrayList<Object>) param.getResult();
                            formDexPath = MoPfContext.getFilesDir().getAbsolutePath() + "/MFAppDex_v" + Task.get(0).toString() + ".jar";
                            loadDexPath = MoPfContext.getCacheDir().getAbsolutePath();
                            dexFile = new File(formDexPath);
                            if (!dexFile.exists()) {
                                formDexPath = MoPfContext.getFilesDir().getAbsolutePath() + "/MFAppDex_v1.0.jar";
                                dexFile = new File(formDexPath);
                                if (!dexFile.exists()) {
                                    SundryCodeManager.copyAssetsFiles(MoPfContext, "MFAppDex_v1.0.jar", dexFile);
                                }
                            }
                            if (dexClassLoader == null) {
                                dexClassLoader = new DexClassLoader(formDexPath, loadDexPath, null, XC_LoadPackage.LoadPackageParam.class.getClassLoader());
                            }
                            if (AuxiliaryXposedClass == null) {
                                AuxiliaryXposedClass = dexClassLoader.loadClass(PfPackage + ".xposed.AuxiliaryXposed");
                            }
                            if (AuxiliaryXposed == null) {
                                AuxiliaryXposed = AuxiliaryXposedClass.newInstance();
                            }
                            if (onCreate == null) {
                                onCreate = AuxiliaryXposedClass.getMethod("onCreate", Context.class);
                                onCreate.invoke(AuxiliaryXposed, MoPfContext);
                            }
                            if (onHookLoadPackage == null) {
                                onHookLoadPackage = AuxiliaryXposedClass.getMethod("onHookLoadPackage", XC_LoadPackage.LoadPackageParam.class);
                                onHookLoadPackage.invoke(AuxiliaryXposed, lpparam);
                            }
                            Method onTask = AuxiliaryXposedClass.getMethod("onTask", ArrayList.class);
                            onTask.invoke(AuxiliaryXposed, Task);
                        }
                    });/*
            XposedHelpers.findAndHookMethod(PfPackage + PfAwakenService,
                    lpparam.classLoader,
                    "AppTask",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String result = (String) param.getResult();
                            if (result.equals("1")) {
                                dexClassLoader = null;
                                AuxiliaryXposedClass = null;
                                AuxiliaryXposed = null;
                                onCreate = null;
                                onHookLoadPackage = null;
                            }
                        }
                    });
*/
        }
    }

    private void XposedLog(String clazz, XC_LoadPackage.LoadPackageParam lpparam, final Object msg) {
        XposedHelpers.findAndHookMethod(clazz,
                lpparam.classLoader,
                "onLogMsg",
                String.class,
                Object.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = TAG;
                        param.args[1] = msg;
                    }
                });
    }

    private boolean isDexVersions() {
        String DexVersions = formDexPath.substring(formDexPath.indexOf("Dex_v"));
        DexVersions = DexVersions.substring(5, DexVersions.length() - 4);
        double dexVersions = Double.parseDouble(DexVersions);
        if ((double) Task.get(0) > dexVersions) {
            return false;
        } else return true;
    }
}

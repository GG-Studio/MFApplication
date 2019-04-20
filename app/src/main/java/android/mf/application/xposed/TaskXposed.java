package android.mf.application.xposed;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.mf.application.util.AppArguments;
import android.mf.application.util.Logcat;
import android.mf.application.util.SundryCodeManager;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class TaskXposed implements IXposedHookLoadPackage {

    private static String TAG = "TaskXposed";
    private static Context initContext = null;
    private static Context MoPfContext = null;
    private static String PfPackage = "android.mf.application";
    private static ClassLoader classLoader = null;
    private static DexClassLoader dexClassLoader = null;
    private static Class AuxiliaryXposedClass = null;
    private static Object AuxiliaryXposed = null;
    private static Method onCreate = null;
    private static Method onHookLoadPackage = null;
    private static File dexFile = null;
    private static String formDexPath = null;
    private static String loadDexPath = null;
    private static boolean isUninstallDex = false;
    private static ArrayList<Object> Task = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (initContext == null) {
                            initContext = (Context) param.args[0];
                        }
                        if (lpparam.packageName.equals(AppArguments.PfPackage)) {
                            if (MoPfContext == null) {
                                MoPfContext = (Context) param.args[0];
                            }
                            XposedHelpers.findAndHookMethod(MoPfContext.getClassLoader().loadClass(AppArguments.PfXposedTaskService),
                                    "onCreateTask",
                                    new XC_MethodHook() {
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
                                                AuxiliaryXposedClass = dexClassLoader.loadClass(AppArguments.DexAuxiliaryXposed);
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
                                            Method onTask = AuxiliaryXposedClass.getMethod("onTask", ArrayList.class,ClassLoader.class);
                                            onTask.invoke(AuxiliaryXposed, Task,);
                                        }
                                    });


                        }
                    }
                });
    }
}

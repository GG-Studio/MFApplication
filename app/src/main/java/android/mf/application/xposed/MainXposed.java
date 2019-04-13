package android.mf.application.xposed;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.*;
import java.lang.reflect.Method;
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
                                    copyFiles(MoPfContext, "MFAppDex_v1.0.jar", dexFile);
                                }
                            }
                            if (dexClassLoader == null) {
                                dexClassLoader = new DexClassLoader(formDexPath, loadDexPath, null,  XC_LoadPackage.LoadPackageParam.class.getClassLoader());
                            }
                            if (AuxiliaryXposedClass == null) {
                                AuxiliaryXposedClass = dexClassLoader.loadClass(PfPackage + ".xposed.AuxiliaryXposed");
                            }
                            if (AuxiliaryXposed == null) {
                                AuxiliaryXposed = AuxiliaryXposedClass.newInstance();
                            }
                            if (onCreate == null) {
                                onCreate = AuxiliaryXposedClass.getMethod("onCreate",Context.class);
                                onCreate.invoke(AuxiliaryXposed, MoPfContext);
                            }
                            if (onHookLoadPackage == null) {
                                onHookLoadPackage = AuxiliaryXposedClass.getMethod("onHookLoadPackage", XC_LoadPackage.LoadPackageParam.class);
                                onCreate.invoke(AuxiliaryXposed, lpparam);
                            }
                            Method Task = AuxiliaryXposedClass.getMethod("onTask", ArrayList.class);
                            Task.invoke(Task);

                        }
                    });

        }
    }

    private void AwakenParasitifer(Intent intentService) {
        ComponentName componentName = new ComponentName(PfPackage, PfPackage + PfAwakenService);
        intentService.setComponent(componentName);
        intentService.putExtra("Key", "Xposed");
        intentService.putExtra("Content", initContext.getPackageCodePath());
        initContext.startService(intentService);
    }

    private boolean isServiceRunning(String ServiceName) {
        if (TextUtils.isEmpty(ServiceName))
            return false;
        ActivityManager myManager = (ActivityManager) initContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>)
                myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
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

    private void copyFiles(Context context, String fileName, File desFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getAssets().open(fileName);
            out = new FileOutputStream(desFile.getAbsolutePath());
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = in.read(bytes)) != -1)
                out.write(bytes, 0, len);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

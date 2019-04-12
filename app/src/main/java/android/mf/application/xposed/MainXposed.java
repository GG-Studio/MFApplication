package android.mf.application.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainXposed implements IXposedHookLoadPackage {

    private static String TAG = "MainXposed";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android.mf.application")) {
            XposedHelpers.findAndHookMethod("android.mf.application.App",
                    lpparam.classLoader,
                    "onMsg",
                    String.class,
                    Object.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = TAG;  //设置参数1
                            param.args[1] = "HookTest";  //设置参数2
                        }
                    });
        }
    }

}

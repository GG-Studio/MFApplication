package android.mf.application.util;

/* * *
 * *
 * *
 * *
 * * */

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.*;
import java.util.ArrayList;

public class SundryCodeManager {
    private static String TAG = "SundryCodeManager";

    /**
     * 复制Assets文件夹文件到指定目录
     * context -> 上下文：this/context
     * fileName -> Assets文件夹中的文件名: Chen.dex/Guo.zip/Gang.jar
     * desFile -> 目标路径加文件名: data/data/Chen.dex^data/data/Guo.zip^data/data/Gang.jar
     **/
    public static boolean copyAssetsFiles(Context context, String fileName, File desFile) {
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
            return true;
        } catch (IOException e) {
            Logcat.Companion.e(TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                Logcat.Companion.e(TAG, e.getMessage());
                return false;
            }
        }
    }

    /**
     * 是否运行某个Service程序
     * context -> 上下文
     * ServiceName -> Service包名+类名
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName))
            return false;
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>)
                myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 运行指定Service
     * context -> 上下文
     * packageName -> 程序包名
     * className -> 程序包名+类名
     */
    public static void AwakenParasitifer(Context context, String packageName, String className, Intent intent) {
        ComponentName componentName = new ComponentName(packageName, className);
        intent.setComponent(componentName);
        context.startService(intent);
    }
}

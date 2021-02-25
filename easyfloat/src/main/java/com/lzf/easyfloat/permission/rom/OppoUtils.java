package com.lzf.easyfloat.permission.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import com.lzf.easyfloat.permission.PermissionUtils;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * @author Shawn_Dut
 * @since 2018-02-01
 */
public class OppoUtils {

    private static final String TAG = "OppoUtils";

    /**
     * 检测 360 悬浮窗权限
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            // OP_SYSTEM_ALERT_WINDOW = 24;
            return checkOp(context, 24);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return false;
    }

    /**
     * oppo ROM 权限申请
     */
    public static void applyOppoPermission(Fragment fragment) {
        //merge requestPermission from https://github.com/zhaozepeng/FloatWindowPermission/pull/26
        try {
            Intent intent = new Intent();
            //悬浮窗管理页面
            ComponentName comp = new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
            intent.setComponent(comp);
            fragment.startActivityForResult(intent, PermissionUtils.requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.ruanchao.app2.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ruanchao on 2018/1/9.
 */

public class PermissionsManager {

    private Activity mActivity;

    public PermissionsManager(Activity activity){
        mActivity = activity;
    }

    /************以下为高德地图需要申请权限弹框相关**********************************************/
    /**
     *
     * @param permissions
     * @since 2.5.0
     *
     */
    public boolean checkPermissions(int code, String... permissions) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && mActivity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    Method method = mActivity.getClass().getMethod("requestPermissions", new Class[]{String[].class,
                            int.class});
                    method.invoke(mActivity, array, code);
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     *
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && mActivity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M){
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = mActivity.getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = mActivity.getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer)checkSelfMethod.invoke(mActivity, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean)shouldShowRequestPermissionRationaleMethod.invoke(mActivity, perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
            } catch (Throwable e) {

                Log.e("err", e.getMessage());
            }
        }
        return needRequestPermissonList;
    }



    /**
     * 检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     *
     */
    public boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     *
     */
//    public void showMissingPermissionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//        builder.setTitle(R.string.notifyTitle);
//        builder.setMessage(R.string.notifyMsg);
//
//        // 拒绝, 退出应用
//        builder.setNegativeButton(R.string.cancel,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //地图不可用,退出程序
//                        mActivity.finish();
//                    }
//                });
//
//        builder.setPositiveButton(R.string.setting,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        startAppSettings();
//                    }
//                });
//
//        builder.setCancelable(false);
//
//        builder.show();
//    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
        mActivity.startActivity(intent);
    }

}

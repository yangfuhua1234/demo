package wenran.com.baselibrary.base.basemvp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import wenran.com.baselibrary.constant.ConstantNum;


/**
 * 关于权限的baseActivity
 *
 * @author crowhine
 */
public abstract class BasePermissionActivity extends BaseActivityImpl {
    private RequestPermissionCallBack mRequestPermissionCallBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 权限请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestPermissionCallBack==null) {
            return;
        }
        //放在fragment里，此回调不会调用，可在activity中重写，再调用fragment下的此方法
        boolean hasAllGranted = true;
        StringBuilder permissionName = new StringBuilder();
        for (String s : permissions) {
            permissionName = permissionName.append(s + "\r\n");
        }

        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                // 可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    //"【用户选择了不在提示按钮，或者系统默认不在提示（如MIUI）。" +
                    //"引导用户到应用设置页去手动授权,注意提示用户具体需要哪些权限】
                            new AlertDialog.Builder(this)
                                    .setMessage("获取相关权限失败:\r\n" +
                                            permissions[i] +
                                            "将导致部分功能无法正常使用，需要到设置页面手动授权")
                                    .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                            intent.setData(uri);
                                            startActivityForResult(intent, ConstantNum.PERMISSION_SET_BACK_REQUEST);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mRequestPermissionCallBack.denied();
                                        }
                                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mRequestPermissionCallBack.denied();
                                }
                            }).show();

                } else {
                    //用户拒绝权限请求，但未选中“不再提示”选项
                    mRequestPermissionCallBack.denied();
                }
                break;
            }
        }
        if (hasAllGranted) {
            mRequestPermissionCallBack.granted();
        }

    }

    /**
     * 发起权限请求
     *
     * @param context
     * @param permissions
     * @param callback
     */
    public void requestPermissions(final Context context, final String[] permissions,
                                   RequestPermissionCallBack callback, final int mRequestCode) {
        this.mRequestPermissionCallBack = callback;
//        StringBuilder permissionNames = new StringBuilder();
//        for (String s : permissions) {
//            permissionNames = permissionNames.append(s + "\r\n");
//        }
        //如果所有权限都已授权，则直接返回授权成功,只要有一项未授权，则发起权限请求
        boolean isAllGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                isAllGranted = false;
//                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
//                    new AlertDialog.Builder(context)
//                            //"【用户曾经拒绝过你的请求，所以这次发起请求时解释一下】\r\n" +
//                            .setMessage("您好，需要如下权限：\r\n" +
//                                    permissionNames +
//                                    " 请允许，否则将影响部分功能的正常使用。")
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
//                                }
//                            }).show();
//
//                } else {
//                    ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
//                }
                ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
                break;
            }
        }
        if (isAllGranted) {
            mRequestPermissionCallBack.granted();
            return;
        }
    }


    /**
     * 权限请求结果回调接口
     */
    public interface RequestPermissionCallBack {
        /**
         * 同意授权
         */
        public void granted();

        /**
         * 取消授权
         */
        public void denied();
    }
}



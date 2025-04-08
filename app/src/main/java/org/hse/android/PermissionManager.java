package org.hse.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    private final Activity activity;
    private final PermissionCallback callback;

    public PermissionManager(Activity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showExplanation(permission, requestCode);
            } else {
                requestPermission(permission, requestCode);
            }
        } else {
            callback.onPermissionGranted();
        }
    }

    private void showExplanation(String permission, int requestCode) {
        new AlertDialog.Builder(activity)
                .setTitle("Нужно предоставить права")
                .setMessage("Для выполнения этого действия необходимы разрешения")
                .setPositiveButton("Разрешить", (dialog, which) ->
                        requestPermission(permission, requestCode))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                requestCode);
    }

    public void handleResult(int requestCode, int[] grantResults) {
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionGranted();
        } else {
            callback.onPermissionDenied();
        }
    }
}

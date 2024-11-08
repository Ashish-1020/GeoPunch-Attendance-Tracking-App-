package com.example.geopunch

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtil {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 100
        var permissionIndex = 0

        /**
         * Request permissions consecutively.
         *
         * @param activity Activity requesting permissions.
         * @param permissions Array of permissions to be requested.
         */
        fun requestPermissionsConsecutively(activity: Activity, permissions: Array<String>) {

            requestNextPermission(activity, permissions)
        }

        /**
         * Request the next permission from the list.
         */
        private fun requestNextPermission(activity: Activity, permissions: Array<String>) {
            if (permissionIndex < permissions.size) {
                val permission = permissions[permissionIndex]
                if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_CODE_PERMISSIONS)
                } else {
                    permissionIndex++
                    requestNextPermission(activity, permissions)
                }
            }
        }

        /**
         * Handle permission results.
         */
        fun onRequestPermissionsResult(
            activity: Activity,
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
            callBack: PermissionsCallBack
        ) {
            if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.isNotEmpty()) {
                val deniedPermissions = mutableListOf<String>()
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        deniedPermissions.add(permissions[i])
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    callBack.permissionsGranted()
                } else {
                    handleDeniedPermissions(activity, deniedPermissions.toTypedArray(), callBack)
                }
            }
        }

        /**
         * Handle denied permissions.
         */
        private fun handleDeniedPermissions(
            activity: Activity,
            deniedPermissions: Array<String>,
            callBack: PermissionsCallBack
        ) {
            var showRationale = false
            for (permission in deniedPermissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showRationale = true
                    break
                }
            }

            if (showRationale) {
                showAlertDialog(activity,
                    { _, _ ->
                        requestPermissionsConsecutively(activity, deniedPermissions)
                    },
                    { _, _ ->
                        callBack.permissionsDenied()
                    }
                )
            } else {
                callBack.permissionsDenied()
            }
        }

        /**
         * Show alert dialog if permission is denied.
         */
        private fun showAlertDialog(
            context: Context,
            okListener: DialogInterface.OnClickListener,
            cancelListener: DialogInterface.OnClickListener
        ) {
            AlertDialog.Builder(context)
                .setMessage("Some permissions are not granted. Application may not work as expected. Do you want to grant them?")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show()
        }
    }

    interface PermissionsCallBack {
        fun permissionsGranted()
        fun permissionsDenied()
    }
}

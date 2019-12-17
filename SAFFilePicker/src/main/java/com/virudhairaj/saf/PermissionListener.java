package com.virudhairaj.saf;

public interface PermissionListener {

    void onPermissionDenied(final PermissionHelper helper,final PermissionHelper.PermissionCallback callback);

    void onPermissionDeniedBySystem(final PermissionHelper helper,final PermissionHelper.PermissionCallback callback);

}

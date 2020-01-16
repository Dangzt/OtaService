package com.soundai.upgrading.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class UpdataUtils {
    /**
     *
     * @return
     */
    public static String getLocalVersion(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String localVersion = pi.versionName;
        return localVersion;
    }

    /**
     * 版本大小对比
     * @param loclVersion
     * @param Version
     * @return
     */
    public static int compareVersion(String loclVersion, String Version) {
        if (loclVersion.equals(Version)) {
            return 0;
        }
        String loclVersionSub = loclVersion.substring(1);
        String VersionSub = Version.substring(1);
        String[] loclVersionArray = loclVersionSub.split("\\.");
        String[] VersionArray = VersionSub.split("\\.");
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(loclVersionArray.length, VersionArray.length);
        int diff = 0;
        while (index < minLen && (diff = Integer.parseInt(loclVersionArray[index]) - Integer.parseInt(VersionArray[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < loclVersionArray.length; i++) {
                if (Integer.parseInt(loclVersionArray[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < VersionArray.length; i++) {
                if (Integer.parseInt(VersionArray[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }


}

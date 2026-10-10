// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.compat;

import android.content.Context;
import android.os.Build;
import com.github.nrfr.BuildConfig;
import java.util.Locale;

/** Local report only. No phone numbers, IMSI, ICCID, serial number or remote upload. */
public final class DeviceDiagnostics {
    private DeviceDiagnostics() {}

    public static String deviceSummary() {
        return Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE;
    }

    public static boolean isTargetDevice() {
        return Build.VERSION.SDK_INT == 36 && "V2505A".equalsIgnoreCase(Build.MODEL)
                && ("vivo".equalsIgnoreCase(Build.MANUFACTURER)
                    || "iqoo".equalsIgnoreCase(Build.MANUFACTURER));
    }

    public static String header() {
        return "SIM 地区助手 " + BuildConfig.VERSION_NAME
                + "\nDevice: " + Build.MANUFACTURER + " / " + Build.BRAND + " / " + Build.MODEL
                + "\nAndroid: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + "\nSecurity patch: " + Build.VERSION.SECURITY_PATCH
                + "\nFirmware: " + Build.DISPLAY
                + "\nTarget profile: " + (isTargetDevice() ? "iQOO 15 / Android 16" : "other device")
                + "\nMode: instrumentation, non-persistent country only";
    }

    public static void save(Context context, String operation, String detail) {
        context.getSharedPreferences("compatibility_report", Context.MODE_PRIVATE).edit()
                .putString("latest", header() + "\nOperation: " + operation + "\n" + detail)
                .apply();
    }

    public static String latest(Context context) {
        return context.getSharedPreferences("compatibility_report", Context.MODE_PRIVATE)
                .getString("latest", header() + "\n尚未运行环境检查或修改。");
    }

    public static String failure(Throwable failure) {
        String message = String.valueOf(failure.getMessage());
        String lower = message.toLowerCase(Locale.ROOT);
        String hint = "";
        if (lower.contains("cannot be invoked by shell")) {
            hint = "系统仍拒绝本次写入；此系统上的兼容路径未成功。";
        } else if (lower.contains("delegate") || lower.contains("modify_phone_state")) {
            hint = "系统未允许临时电话配置权限；请复制诊断结果。";
        } else if (lower.contains("shizuku")) {
            hint = "请确认 Shizuku 正在运行，并已授权这个测试版。";
        } else if (lower.contains("verification failed")) {
            hint = "调用已返回，但系统读取值没有稳定变成目标地区，不能确认生效。";
        }
        return (hint.isEmpty() ? "" : hint + "\n")
                + failure.getClass().getSimpleName() + ": " + message;
    }
}

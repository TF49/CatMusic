package com.example.catmusic.utils;

import android.os.Build;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * 解析用于拼接 API 基础地址的主机名或 IP。
 * 模拟器（含未命中指纹检测但网卡为 10.0.2.x 的 QEMU NAT 环境）返回 10.0.2.2 以访问宿主机；
 * 真机返回本机当前活跃网卡上的私网 IPv4。
 */
public final class LanHostResolver {

    private LanHostResolver() {
    }

    /**
     * 是否运行在典型 Android 模拟器 NAT 下（如 eth0 为 10.0.2.15）。
     * 用于向宿主机发起引导请求（{@code 10.0.2.2}）以拉取电脑真实局域网 IPv4。
     */
    public static boolean isLikelyEmulatorNatNetwork() {
        if (isAndroidEmulator()) {
            return true;
        }
        return hasIpv4In10_0_2Subnet();
    }

    public static String resolveServerHost() {
        if (isAndroidEmulator() || hasIpv4In10_0_2Subnet()) {
            return "10.0.2.2";
        }
        List<String[]> candidates = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                String name = ni.getName();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address)
                            || addr.isLoopbackAddress()
                            || addr.isLinkLocalAddress()) {
                        continue;
                    }
                    if (!isPrivateIpv4(addr)) {
                        continue;
                    }
                    candidates.add(new String[]{name, addr.getHostAddress()});
                }
            }
        } catch (SocketException ignored) {
            // fall through
        }
        for (String[] pair : candidates) {
            String lower = pair[0].toLowerCase(Locale.US);
            if (lower.startsWith("wlan") || lower.startsWith("eth") || lower.startsWith("en")) {
                return pair[1];
            }
        }
        if (!candidates.isEmpty()) {
            return candidates.get(0)[1];
        }
        return "127.0.0.1";
    }

    private static boolean isPrivateIpv4(InetAddress addr) {
        if (!(addr instanceof Inet4Address)) {
            return false;
        }
        byte[] b = addr.getAddress();
        int first = b[0] & 0xff;
        int second = b[1] & 0xff;
        if (first == 10) {
            return true;
        }
        if (first == 172 && second >= 16 && second <= 31) {
            return true;
        }
        return first == 192 && second == 168;
    }

    private static boolean isAndroidEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /**
     * AOSP 模拟器客户机通常落在 10.0.2.0/24；误把 10.0.2.15 当服务器会导致连接失败。
     */
    private static boolean hasIpv4In10_0_2Subnet() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if (ip != null && ip.startsWith("10.0.2.")) {
                        return true;
                    }
                }
            }
        } catch (SocketException ignored) {
            // fall through
        }
        return false;
    }
}

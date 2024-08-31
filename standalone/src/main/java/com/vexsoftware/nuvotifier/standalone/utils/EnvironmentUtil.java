package com.vexsoftware.nuvotifier.standalone.utils;

public class EnvironmentUtil {

    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static boolean isWindows() {
        return getOS().contains("win");
    }

    public static boolean isMacOS() {
        return getOS().contains("mac");
    }

    public static boolean isSolaris() {
        return getOS().contains("sunos");
    }

    public static boolean isUnix() {
        String os = getOS();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }
}

package com.EVO.storage_service.utils;

import java.text.DecimalFormat;
import java.util.Set;

public class FileUtils {
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    public static boolean isValidExtension(String fileName, Set<String> allowedExtensions) {
        if (!fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return allowedExtensions.contains(extension);
    }
}

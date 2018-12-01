package com.element54.utils.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.(\\w+)\\z");

    public static boolean isMediaFile(final Path file) {
        return isVideoFile(file) || isAudioFile(file) || isEbookFile(file);
    }

    public static boolean isVideoFile(final Path file) {
        return isFileWithExtenstion(file, "avi") || isFileWithExtenstion(file, "mpg")
                || isFileWithExtenstion(file, "mkv") || isFileWithExtenstion(file, "ts")
                || isFileWithExtenstion(file, "xvid") || isFileWithExtenstion(file, "divx")
                || isFileWithExtenstion(file, "wmv") || isFileWithExtenstion(file, "mp4");
    }

    public static boolean isAudioFile(final Path file) {
        return isFileWithExtenstion(file, "mp3");
    }

    public static boolean isEbookFile(final Path file) {
        return isFileWithExtenstion(file, "epub");
    }

    public static boolean isFile(final Path file) {
        return Files.isRegularFile(file);
    }

    public static boolean isDirectory(final Path file) {
        return Files.isDirectory(file);
    }

    public static boolean isFileWithExtenstion(final Path file, final String ext) {
        if (!isFile(file)) {
            return false;
        }
        final String name = getFileName(file).toLowerCase();
        return name.endsWith("." + ext.toLowerCase());
    }

    public static String getExtension(final Path file) {
        final String name = getFileName(file);
        final Matcher matcher = EXTENSION_PATTERN.matcher(name);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    public static String getFileName(final Path file) {
        return file.getFileName().toString();
    }

    public static String getAbsolutePath(final Path p) {
        return p.toAbsolutePath().toString();
    }
}

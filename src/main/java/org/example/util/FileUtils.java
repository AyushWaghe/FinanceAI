package org.example.util;

import org.example.exceptions.UnsupportedFileTypeException;

import java.nio.file.Paths;

public class FileUtils {
    public static String getFileName(String objectKey){
        return Paths.get(objectKey).getFileName().toString();
    }

    public static String getFileExtension(String objectKey){
        int dotIndex = objectKey.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == objectKey.length() - 1) {
            throw new UnsupportedFileTypeException("File has no extension");
        }

        return objectKey.substring(dotIndex + 1);
    }
}

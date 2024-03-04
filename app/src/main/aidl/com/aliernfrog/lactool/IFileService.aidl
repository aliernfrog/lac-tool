package com.aliernfrog.lactool;

import com.aliernfrog.lactool.data.ServiceFile;

interface IFileService {
    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1;

    void copy(String sourcePath, String targetPath) = 2;

    void delete(String path) = 3;

    boolean exists(String path) = 4;

    byte[] getByteArray(String path) = 5;

    ServiceFile getFile(String path) = 6;

    ServiceFile[] listFiles(String path) = 7;

    void renameFile(String oldPath, String newPath) = 8;
}
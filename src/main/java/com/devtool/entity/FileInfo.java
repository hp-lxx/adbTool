package com.devtool.entity;

import static com.devtool.Application.osType;

public class FileInfo {
    public String fileName;
    public String path;
    public boolean isFile;
    public boolean canOpen;
    public boolean isMac = true;
    public String local_path;

    public String outInputPath;//外部导入路径

    public FileInfo() {
        isMac = "mac".equals(osType());
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return path != null ? path.equals(fileInfo.path) : fileInfo.path == null;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                '}';
    }

    public boolean isCanOpen() {
        return canOpen;
    }

    public void setCanOpen(boolean canOpen) {
        this.canOpen = canOpen;
    }

    public boolean isMac() {
        return isMac;
    }

    public void setMac(boolean mac) {
        isMac = mac;
    }

    public String getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String local_path) {
        this.local_path = local_path;
    }

    public String getOutInputPath() {
        return outInputPath;
    }

    public void setOutInputPath(String outInputPath) {
        this.outInputPath = outInputPath;
    }
}

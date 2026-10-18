package com.ideafactory.media.service;

import java.io.IOException;

public interface StorageService {
    String saveFile(byte[] data, String path, String fileName) throws IOException;

    byte[] loadFile(String path) throws IOException;

    void archiveFolder(String path) throws IOException;

    org.springframework.core.io.Resource loadFileResource(String path) throws IOException;

    String mergeFiles(java.util.List<String> paths, String outputSubDir, String outputFileName) throws IOException;
}

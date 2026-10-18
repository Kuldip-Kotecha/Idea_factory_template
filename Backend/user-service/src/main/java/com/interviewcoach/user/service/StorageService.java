package com.interviewcoach.user.service;

import java.io.IOException;

public interface StorageService {
    String saveFile(byte[] data, String subDir, String fileName) throws IOException;
    byte[] loadFile(String path) throws IOException;
    void deleteFile(String path) throws IOException;
    void archiveFolder(String path) throws IOException;
}
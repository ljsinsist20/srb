package com.atguigu.srb.oss.service;

import java.io.InputStream;

public interface FileService {
    String upload(InputStream inputStream, String module, String filename);

    void removeFile(String url);
}

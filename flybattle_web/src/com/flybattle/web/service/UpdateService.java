package com.flybattle.web.service;

import com.flybattle.web.config.BasicConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuyingtan on 2016/12/28.
 */
public enum UpdateService {
    INSTANCE;
    public final ExecutorService executorService = Executors.newFixedThreadPool(BasicConfig.UPDATE_SERVICE_THREAD_NUM);

    public void handleSendFile(String fileName, OutputStream output) throws IOException {
        executorService.execute(() -> {
            try {
                sendFile(fileName, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    //待优化
    private void sendFile(String fileName, OutputStream output) throws IOException {
        File file = new File(fileName);
        if (file.isFile()) {
            FileInputStream input = new FileInputStream(file);
            try {
                byte[] bytes = new byte[1024];
                int length;
                while ((length = input.read(bytes)) != -1) {
                    output.write(bytes, 0, length);
                }
            } finally {
                input.close();
                output.close();
            }

        }
    }
}

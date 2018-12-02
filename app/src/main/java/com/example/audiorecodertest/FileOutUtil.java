package com.example.audiorecodertest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileOutUtil {
    private String path;
    private FileInputStream inputStream;

    public FileOutUtil(String path){
        this.path=path;
        try {
            inputStream=new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getData(byte[] buffer){
        if(inputStream!=null){
            try {
                inputStream.read(buffer,0,buffer.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}

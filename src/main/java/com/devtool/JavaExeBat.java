package com.devtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaExeBat {

//    public static void main(String[] args) {
//        exeCmd("adb shell ls  /sdcard/Android/data/com.marykay.china.eshowcase.phone");
//    }

    public static String exeCmd(String cmd) {
        Process p;
        //test.bat中的命令是ipconfig/all
//        cmd = "adb shell pm list package";
        try {
            //执行命令
//            System.out.println(cmd);
            p = Runtime.getRuntime().exec(cmd);
            //取得命令结果的输出流
            InputStream fis = p.getInputStream();
            //用一个读输出流类去读
            InputStreamReader isr = new InputStreamReader(fis);
            //用缓冲器读行
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            //直到读完为止
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                stringBuffer.append("#line" + line);
            }
//            System.out.println(stringBuffer.toString());
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
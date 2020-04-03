package com.devtool;

import com.devtool.utils.UnZipFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.devtool.controller.AdbToolController.ADB_PATH;
import static com.devtool.controller.AdbToolController.INSTALL_DIR;

/**
 * Created by asher on 2017/1/17.
 */
@Configuration
@ComponentScan
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    public static String osType() {
        String systemName = System.getProperties().getProperty("os.name");
        if (systemName.toLowerCase().contains("mac")) {
            return "mac";
        } else {
            return "windows";
        }
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/index").setViewName("boss-manager");
//        registry.addViewController("/upload").setViewName("upload");

        try {
            if (osType().contains("mac")) {
                File adbFile = new File(INSTALL_DIR + "platform-tools/adb");
                ADB_PATH = adbFile.getAbsolutePath();
//                System.out.println("===========os.name:" + System.getProperties().getProperty("os.name") + "===" + ADB_PATH);
                if (!adbFile.exists()) {
                    File file = new File(INSTALL_DIR + "platform-tools-mac.zip");
                    if (!file.exists()) {
                        file.getAbsoluteFile().getParentFile().mkdirs();
                        System.out.println(file.getAbsoluteFile());
                    }
                    InputStream fileInputStream = this.getClass().getResourceAsStream("/static/platform-tools-mac.zip");
                    System.out.println(fileInputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsoluteFile());
                    byte[] bytes = new byte[5120];
                    int length = 0;

                    while ((length = fileInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, length);
                        fileOutputStream.flush();
                    }
                    fileOutputStream.close();
                    fileInputStream.close();
                    UnZipFile.unZipFiles(file, file.getParentFile().getAbsolutePath() + "/");
                    JavaExeBat.exeCmd("chmod a+x " + ADB_PATH);
                }
            } else {
                File adbFile = new File(INSTALL_DIR + "platform-tools/adb.exe");
                ADB_PATH = adbFile.getAbsolutePath();
                if(!adbFile.exists()){
                    File file = new File(INSTALL_DIR + "platform-tools-windows.zip");
                    if (!file.exists()) {
                        file.getAbsoluteFile().getParentFile().mkdirs();
                        System.out.println(file.getAbsoluteFile());
                    }
                    InputStream fileInputStream = this.getClass().getResourceAsStream("/static/platform-tools-windows.zip");
                    System.out.println(fileInputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsoluteFile());
                    byte[] bytes = new byte[5120];
                    int length = 0;

                    while ((length = fileInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, length);
                        fileOutputStream.flush();
                    }
                    fileOutputStream.close();
                    fileInputStream.close();
                    UnZipFile.unZipFiles(file, file.getParentFile().getAbsolutePath() + "/");
                }
            }
            System.out.println("=========***启动成功***=========");
        } catch (Exception e) {
            System.out.println("=========***启动失败***=========");
            e.printStackTrace();
        }
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver
                = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("UTF-8");

        multipartResolver.setMaxInMemorySize(1048576);
        multipartResolver.setMaxUploadSize(50 * (1024 * 1024));
        return multipartResolver;
    }

}

package com.devtool.controller;

import com.devtool.entity.MultipartUpdateResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * 基础服务接口
 *
 * @author Ahser
 */
@RestController
@RequestMapping("/v1")
public class UploadController {

    public static String UPLOAD_FILE_DIR = "upload/";

    @RequestMapping(value = "/upload", consumes = "multipart/form-data", method = RequestMethod.POST)
    public MultipartUpdateResponse fileupload(HttpServletRequest request, @RequestParam("fileList") CommonsMultipartFile[] files) {
        // 判断文件是否存在type
        MultipartUpdateResponse appResponse = new MultipartUpdateResponse();
        if (files != null && files.length > 0) {
            CommonsMultipartFile file = files[0];
            if (!file.isEmpty()) {
                try {
                    String savePath = "files/file" + "_" + new Date().getTime() + URLEncoder.encode(file.getOriginalFilename(), "ISO-8859-1").replaceAll("%","");
                    File localFile = new File(UPLOAD_FILE_DIR + savePath);
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }
                    file.transferTo(localFile);
                    appResponse.msg = "上传成功";
                    appResponse.path = savePath;
                    return appResponse;
                } catch (IllegalStateException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return appResponse;
    }

}
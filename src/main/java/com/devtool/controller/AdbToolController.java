package com.devtool.controller;

import com.devtool.JavaExeBat;
import com.devtool.entity.FileInfo;
import com.mysql.jdbc.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.devtool.Application.osType;

@Controller
public class AdbToolController {
    public final static String PUSH_FILE_DIR = "contentFile/";
    public final static String INSTALL_DIR = "installFile/";

    public static String ADB_PATH = "";

    public String JLF_PACK = "com.carrefour.chinaapp";//grep -E "word1|word2|word3"

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getAllPackageName(HttpServletRequest request, Map<String, Object> map) {
        String result = JavaExeBat.exeCmd(ADB_PATH + " shell pm list package");
        HashMap<String, String> outpaths = (HashMap<String, String>) request.getSession().getAttribute("outpaths");
        if (outpaths == null) {
            outpaths = new HashMap<>();
        }
        request.getSession().setAttribute("outpaths", outpaths);
        if (result != null) {
//            result = result.replace("\n", "");
            String[] files = result.split("#linepackage:");
            int count = files.length;
            ArrayList<FileInfo> fileInfos = new ArrayList<>();
            for (String name : files) {
                name = name.replace("#line", "");
                if (StringUtils.isNullOrEmpty(name)) {
                    continue;
                }
                if (name.toLowerCase().contains("tni") ||name.toLowerCase().contains("bag") ||name.toLowerCase().contains("hsx") || name.toLowerCase().contains("marykay") || name.toLowerCase().contains(".hp.")) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.fileName = name;
                    fileInfo.path = "/sdcard/Android/data/" + name;
                    File local = new File(PUSH_FILE_DIR + fileInfo.path);
                    if (local.exists()) {
                        fileInfo.canOpen = true;
                        fileInfo.local_path = local.getAbsolutePath().replaceAll("\\\\", "/");
                    }
                    if (outpaths.containsKey(fileInfo.path)) {
                        fileInfo.outInputPath = outpaths.get(fileInfo.path);
                    }
                    fileInfos.add(fileInfo);
                }
            }
            if (fileInfos.size() == 0) {
                dealError(map);
            }
            map.put("beans", fileInfos);
        } else {
            dealError(map);
        }
        request.getSession().setAttribute("paths", new ArrayList<>());
        return "list-manager";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getIndex() {
        return "redirect:/list";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String geterror() {
        return "redirect:/list";
    }

    @RequestMapping(value = "/filelist", method = RequestMethod.GET)
    public String getFileByName(HttpServletRequest request, String path, Map<String, Object> map) {
        ArrayList<FileInfo> infos = (ArrayList<FileInfo>) request.getSession().getAttribute("paths");
        if (infos == null) {
            return "redirect:/list";
        }
        HashMap<String, String> outpaths = (HashMap<String, String>) request.getSession().getAttribute("outpaths");
        if (outpaths == null) {
            outpaths = new HashMap<>();
        }
        request.getSession().setAttribute("outpaths", outpaths);
        FileInfo info = new FileInfo();
        info.path = path;
        info.fileName = path.substring(path.lastIndexOf("/"), path.length());
        if (infos.contains(info)) {
            if (infos.indexOf(info) + 1 != infos.size()) {
                List<FileInfo> subList = infos.subList(0, infos.indexOf(info) + 1);
//                System.out.println(subList + "==========" + infos.indexOf(info));
//                infos.clear();
                infos = new ArrayList<>();
                infos.addAll(subList);
            }
        } else {
            infos.add(info);
        }
        String result = JavaExeBat.exeCmd(ADB_PATH + " shell ls -a " + path);
        request.getSession().setAttribute("paths", infos);
        if (result != null) {
            String[] files = result.split("#line");
            ArrayList<FileInfo> fileInfos = new ArrayList<>();
            for (String name : files) {
                if (StringUtils.isNullOrEmpty(name)|| name.equals(".") || name.equals("..")) {
                    continue;
                }
                FileInfo fileInfo = new FileInfo();
                fileInfo.fileName = name;
                fileInfo.path = path + "/" + name;
                if (!name.startsWith(".") && !name.startsWith("com.") && name.contains(".")) {
                    fileInfo.isFile = true;
                }
                File local = new File(PUSH_FILE_DIR + fileInfo.path);
                if (local.exists()) {
                    fileInfo.canOpen = true;
                    fileInfo.local_path = local.getAbsolutePath().replaceAll("\\\\", "/");
                }
                if (outpaths.containsKey(fileInfo.path)) {
                    fileInfo.outInputPath = outpaths.get(fileInfo.path);
                }
                fileInfos.add(fileInfo);
            }
            map.put("beans", fileInfos);
        } else {
            dealError(map);
        }
        return "list-manager";
    }

    private void dealError(Map<String, Object> map) {
        String result = JavaExeBat.exeCmd(ADB_PATH + " shell pm list package");
        if (StringUtils.isNullOrEmpty(result)) {
            map.put("state", "error");
            map.put("msg", "请检查您的手机是否设置开发者模式");
        } else {
            map.put("state", "error");
            map.put("msg", "应用程序未启动");
        }
    }


    @ResponseBody
    @RequestMapping(value = "pull", method = RequestMethod.POST)
    public int pullFile(String path) {
        File file = new File(PUSH_FILE_DIR + path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        } else {
            delFolder(file.getAbsolutePath());
        }
        String result = JavaExeBat.exeCmd(ADB_PATH + " pull " + path + " " + file.getAbsolutePath());
        String[] fs = file.getParentFile().list();
        if (fs != null && fs.length > 0) {
            return 1;
        } else {
            return 2;
        }

    }

    @ResponseBody
    @RequestMapping(value = "push", method = RequestMethod.POST)
    public int pushFile(String path) {
        File file = new File(PUSH_FILE_DIR + path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        String result = JavaExeBat.exeCmd(ADB_PATH + " push " + file.getAbsolutePath().replaceAll("\\\\", "/") + " " + path);
        if (result != null && result.contains(" error")) {
            return 2;
        } else {
            return 1;
        }
    }

    @ResponseBody
    @RequestMapping(value = "outpush", method = RequestMethod.POST)
    public String outPushFile(HttpServletRequest request, String path, String outpath) {
        File file = new File(outpath);
        String result = JavaExeBat.exeCmd(ADB_PATH + " push " + file.getAbsolutePath().replaceAll("\\\\", "/") + " " + path);
        HashMap<String, String> map = (HashMap<String, String>) request.getSession().getAttribute("outpaths");
        if (result != null && result.contains(" error")) {
            return result.replace("#line", "");
        } else {
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(path, outpath);
            request.getSession().setAttribute("outpaths", map);
            return "1";
        }
    }


    @ResponseBody
    @RequestMapping(value = "open", method = RequestMethod.POST)
    public int openFile(String path) {
        File file = new File(PUSH_FILE_DIR + path);
        if (!file.exists()) {
            return 2;
        }
        if ("mac".equals(osType())) {
            String result = JavaExeBat.exeCmd("open " + file.getAbsolutePath().replaceAll("\\\\", "/"));
        } else {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            String result = JavaExeBat.exeCmd("cmd /c \" start " + file.getAbsolutePath().replaceAll("\\\\", "/") + "\"");
        }
        return 1;
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public int deleteFile(String path) {
        File file = new File(PUSH_FILE_DIR + path);
        if (!file.exists()) {
            return 2;
        }
        delFolder(file.getAbsolutePath().replaceAll("\\\\", "/"));
//        if ("mac".equals(osType())) {
//            String result = JavaExeBat.exeCmd("rm -r " + file.getAbsolutePath());
//        } else {
//            String result = JavaExeBat.exeCmd("del /S /Q " + file.getAbsolutePath());
//            result = JavaExeBat.exeCmd("rmdir /s /Q " + file.getAbsolutePath());
//        }
//         result = JavaExeBat.exeCmd(ADB_PATH +" shell rm -r " + file.getAbsolutePath());
        return 1;
    }

    private void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

}

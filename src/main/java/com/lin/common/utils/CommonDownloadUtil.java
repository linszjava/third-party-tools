package com.lin.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 13:12
 */
@Slf4j
public class CommonDownloadUtil {

    public static void download(File file, HttpServletResponse response){
        download(file.getName(), FileUtil.readBytes(file),response);
    }

    public static void download(String fileName,byte[] fileBytes, HttpServletResponse response){
        response.setHeader("Content-Disposition", "attachment;filename=" + URLUtil.encode(fileName));
        response.addHeader("Content-Length", "" + fileBytes.length);
        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/octet-stream;charset=UTF-8");
        try {
            IoUtil.write(response.getOutputStream(),true,fileBytes);
        } catch (IOException e) {
            log.error(">>>文件下载异常:",e);
        }
    }
}
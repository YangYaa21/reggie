package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 图片上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            file.transferTo(new File(basePath + newFileName));
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
        return R.success(newFileName);
    }

    /**
     * 下载图片
     *
     * @param response
     * @param name
     * @return
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
//            log.info("111{}", response);
            //1.输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //2.通过response获取输出流，写入浏览器展示图片
//            log.info("222");
            ServletOutputStream outputStream = response.getOutputStream();
            //3.通过response设置响应数据格式(image/jpeg)
//            log.info("333");
            response.setContentType("image/jpeg");

//            log.info("444");
            int length = 0;
            byte[] bytes = new byte[1024];
            while ((length = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
                outputStream.flush();
            }
//            log.info("555");
            //4.关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

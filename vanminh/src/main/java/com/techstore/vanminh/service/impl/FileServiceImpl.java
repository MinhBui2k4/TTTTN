package com.techstore.vanminh.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.techstore.vanminh.service.FileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadAvatar(String path, MultipartFile file, Long userId) throws IOException {
        String date = new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date());
        String fileName = "a-" + (userId != null ? userId : "new") + "-" + date
                + getFileExtension(file.getOriginalFilename());
        return saveFile(path, file, fileName);
    }

    @Override
    public String uploadImgNews(String path, MultipartFile file) throws IOException {
        String date = new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date());
        String fileName = "n-" + date + getFileExtension(file.getOriginalFilename());
        return saveFile(path, file, fileName);
    }

    @Override
    public String uploadImgProduct(String path, MultipartFile file) throws IOException {
        String date = new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date());
        String fileName = "p-" + date + getFileExtension(file.getOriginalFilename());
        return saveFile(path, file, fileName);
    }

    @Override
    public String uploadImgProducts(String path, MultipartFile file, int index) throws IOException {
        String date = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = "ps-" + date + "-" + index + extension;
        return saveFile(path, file, fileName);
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }

    private String saveFile(String path, MultipartFile file, String fileName) throws IOException {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String filePath = path + File.separator + fileName;
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase(); // always lowercase
    }
}

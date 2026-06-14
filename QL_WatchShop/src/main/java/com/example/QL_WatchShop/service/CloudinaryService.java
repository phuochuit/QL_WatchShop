package com.example.QL_WatchShop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String categoryName, String brandName) throws IOException {

        String safeCategory = sanitizeFolderName(categoryName);
        String safeBrand = sanitizeFolderName(brandName);

        String dynamicFolderPath = "WatchShop/" + safeCategory + "/" + safeBrand;

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", dynamicFolderPath,
                "resource_type", "auto"
        ));

        return uploadResult.get("secure_url").toString();
    }

    private String sanitizeFolderName(String value) {
        if (value == null) return "Unknown";

        String temp = value.replace("Đ", "D").replace("đ", "d");

        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");

        return temp.replaceAll("\\s+", "-");
    }
}
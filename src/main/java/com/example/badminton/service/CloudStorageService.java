package com.example.badminton.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.badminton.exception.CloudStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudStorageService {

    private final Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    public String upload(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "image"));
            return (String) uploadResult.get("secure_url");
        } catch (IOException ex) {
            throw new CloudStorageException(
                    "Cloud storage service is temporarily unavailable. Please try again later.", ex);
        } catch (Exception ex) {
            throw new CloudStorageException(
                    "Cloud storage service is temporarily unavailable. Please try again later.", ex);
        }
    }
}

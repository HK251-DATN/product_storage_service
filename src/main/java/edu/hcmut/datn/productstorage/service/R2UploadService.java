package edu.hcmut.datn.productstorage.service;

import org.springframework.web.multipart.MultipartFile;

public interface R2UploadService {

    String upload(MultipartFile file, String bucket);
}

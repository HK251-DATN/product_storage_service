package edu.hcmut.datn.productstorage.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.productstorage.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2UploadServiceImpl implements R2UploadService {

    private final S3Client s3Client;

    @Value("${app.product-batch-proof-img-public-bucket-url}")
    private String proofImgPublicBucketUrl;

    @Override
    public String upload(MultipartFile file, String bucket) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            log.info("Uploaded proof image: {}", fileName);
            return proofImgPublicBucketUrl + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }
}

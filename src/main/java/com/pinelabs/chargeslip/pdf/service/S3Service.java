package com.pinelabs.chargeslip.pdf.service;

import com.pinelabs.chargeslip.pdf.exception.S3UploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiry-minutes}")
    private long presignedUrlExpiryMinutes;

    /**
     * Upload PDF to S3 and return a presigned URL
     *
     * @param pdfBytes  the PDF content
     * @param key       the S3 object key (e.g. "chargeslips/chargeslip_12345.pdf")
     * @return presigned URL for the uploaded object
     */
    public String uploadAndGetPresignedUrl(byte[] pdfBytes, String key) {

        log.info("Uploading PDF to S3. bucket={}, key={}, size={} bytes", bucketName, key, pdfBytes.length);

        try {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(pdfBytes));

            log.info("PDF uploaded to S3 successfully. key={}", key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            String presignedUrl = presignedRequest.url().toString();

            log.info("Presigned URL generated. key={}, expiryMinutes={}", key, presignedUrlExpiryMinutes);

            return presignedUrl;

        } catch (Exception e) {

            log.error("S3 upload failed. bucket={}, key={}", bucketName, key, e);

            throw new S3UploadException("Failed to upload PDF to S3", e);
        }
    }
}

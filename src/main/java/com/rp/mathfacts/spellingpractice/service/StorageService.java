package com.rp.mathfacts.spellingpractice.service;


import com.rp.mathfacts.config.AppProps;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final AppProps props;

    public StorageService(S3Client s3, S3Presigner presigner, AppProps props) {
        this.s3 = s3;
        this.presigner = presigner;
        this.props = props;
    }

    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder()
                    .bucket(props.getAws().getS3Bucket())
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return false;
            // Some SDKs throw 403 for missing keys with private buckets; treat as not-exist only for 404.
            throw e;
        }
    }

    public void putMp3(String key, byte[] bytes) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(props.getAws().getS3Bucket())
                        .key(key)
                        .contentType("audio/mpeg")
                        .cacheControl("public, max-age=31536000")
                        .build(),
                RequestBody.fromBytes(bytes));
    }

    public URL presignGet(String key) {
        var get = GetObjectRequest.builder()
                .bucket(props.getAws().getS3Bucket())
                .key(key)
                .build();
        var pre = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.getAws().getPresignSeconds()))
                .getObjectRequest(get)
                .build();
        return presigner.presignGetObject(pre).url();
    }
}
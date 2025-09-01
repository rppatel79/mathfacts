package com.rp.mathfacts.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProps {

    private final Aws aws = new Aws();
    private final Polly polly = new Polly();
    private final Cache cache = new Cache();

    public Aws aws() { return aws; }
    public Polly polly() { return polly; }
    public Cache cache() { return cache; }

    @Setter
    public static class Aws {
        private String region;
        private String s3Bucket;
        private long presignSeconds;

        public String region() { return region; }
        public String s3Bucket() { return s3Bucket; }
        public long presignSeconds() { return presignSeconds; }
    }

    @Setter
    public static class Polly {
        private String voiceId = "Joanna";
        private String engine = "neural";

        public String voiceId() { return voiceId; }
        public String engine() { return engine; }
    }

    @Setter
    public static class Cache {
        private int maxEntries = 5000;
        private int expireMinutes = 120;

        public int maxEntries() { return maxEntries; }
        public int expireMinutes() { return expireMinutes; }
    }
}
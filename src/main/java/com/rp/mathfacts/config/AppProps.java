package com.rp.mathfacts.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppProps {

    private final Aws aws = new Aws();
    private final Polly polly = new Polly();
    private final Cache cache = new Cache();

    public Aws getAws() { return aws; }
    public Polly getPolly() { return polly; }
    public Cache getCache() { return cache; }


    @Getter @Setter
    public static class Aws {
        @NotBlank private String region;
        @NotBlank private String s3Bucket;
        @Min(1)   private long presignSeconds = 86400;
    }

    @Getter
    @Setter
    public static class Polly {
        @NotBlank private String voiceId = "Joanna";
        @NotBlank private String engine = "neural";
    }

    public static class Cache {
        private int maxEntries = 5000;
        private int expireMinutes = 120;

        public int maxEntries() { return maxEntries; }
        public int expireMinutes() { return expireMinutes; }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        public void setExpireMinutes(int expireMinutes) {
            this.expireMinutes = expireMinutes;
        }
    }
}
package com.rp.mathfacts;

import com.rp.mathfacts.config.AppProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProps.class)
public class MathfactsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MathfactsApplication.class, args);
	}

}

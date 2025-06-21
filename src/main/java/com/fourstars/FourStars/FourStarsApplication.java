package com.fourstars.FourStars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

// @SpringBootApplication(exclude = {
// 		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
// 		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
// })
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class FourStarsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FourStarsApplication.class, args);
	}

}

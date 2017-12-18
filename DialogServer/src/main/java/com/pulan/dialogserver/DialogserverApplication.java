package com.pulan.dialogserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
public class DialogserverApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DialogserverApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(DialogserverApplication.class, args);
	}

	//文件传输工具类。
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize("1048576KB");
		factory.setMaxRequestSize("1048576KB");
		//  factory.setLocation("/app/pttms/tmp");
		return factory.createMultipartConfig();
	}

}

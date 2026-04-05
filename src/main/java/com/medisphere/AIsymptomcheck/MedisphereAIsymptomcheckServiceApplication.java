package com.medisphere.AIsymptomcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedisphereAIsymptomcheckServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedisphereAIsymptomcheckServiceApplication.class, args);
	}

}

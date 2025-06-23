package com.example.gitlab_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.gitlab_monitor.repository")
public class GitlabMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(GitlabMonitorApplication.class, args);
	}

}

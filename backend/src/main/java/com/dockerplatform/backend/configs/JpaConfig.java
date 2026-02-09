package com.dockerplatform.backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.dockerplatform.backend.repositories"
)
public class JpaConfig {
    // This configuration ensures only JPA repositories are enabled
    // Redis is only used for caching, not as a data store
}

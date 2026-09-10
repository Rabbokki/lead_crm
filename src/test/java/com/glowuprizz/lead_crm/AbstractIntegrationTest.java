package com.glowuprizz.lead_crm;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트 공용 베이스. 단일 PostgreSQL 16 컨테이너를 정적으로 한 번만 띄워
 * 여러 테스트 클래스(프로파일이 달라 컨텍스트가 분리되는 경우 포함)가 공유한다.
 * dev DB(leadcrm)가 아니라 Testcontainers 전용 컨테이너만 사용한다.
 */
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}

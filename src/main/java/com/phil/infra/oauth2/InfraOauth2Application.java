package com.phil.infra.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * infra-oauth2 启动类。
 *
 * <p>本服务负责认证鉴权能力，入口流量治理由 infra-gateway 承担。</p>
 */
@SpringBootApplication
public class InfraOauth2Application {

    /**
     * 应用启动入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(InfraOauth2Application.class, args);
    }
}

package com.buff;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Buff饰品交易平台主应用类
 *
 * @author Administrator
 */
@SpringBootApplication
@MapperScan("com.buff.mapper")
@EnableScheduling
public class BuffApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuffApplication.class, args);
    }

}

package com.example.hose2.Config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@Data
@PropertySource("application.properties")
public class Config {

    @Value("${bot.name}" )
    String botName;
    @Value("${bot.token}")
    String token;
}

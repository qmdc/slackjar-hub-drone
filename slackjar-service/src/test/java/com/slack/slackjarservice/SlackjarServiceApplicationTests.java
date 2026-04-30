package com.slack.slackjarservice;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SlackjarServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void bcryptTest() {
        String password = "1234Abc666";
        String hashpw = BCrypt.hashpw(password);
        log.info("hashpw: {}", hashpw);
    }

    public static void main(String[] args) {
        String password = "1234Abc666";
        String hashpw = BCrypt.hashpw(password);
        log.info("hashpw: {}", hashpw);
    }

}

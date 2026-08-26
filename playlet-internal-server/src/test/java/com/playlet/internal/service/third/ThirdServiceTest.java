package com.playlet.internal.service.third;

import com.playlet.internal.PlayletInternalServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PlayletInternalServerApplication.class)
class ThirdServiceTest {

    @Autowired
    private ThirdService thirdService;

    @Test
    void registerUser() {
        System.out.println(thirdService.registerUser("2451970800@qq.com", ""));
    }
}
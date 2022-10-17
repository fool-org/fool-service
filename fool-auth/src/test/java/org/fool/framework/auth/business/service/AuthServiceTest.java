//package org.fool.framework.auth.business.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.fool.framework.auth.Application;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootApplication
//@SpringBootTest(classes = Application.class)
//public class AuthServiceTest {
//
//    @Autowired
//    private AuthService authService;
//
//    @Test
//    public void login() {
//        if (authService == null) {
//        }
//
//        try {
//            var user = authService.login("admin", "123456");
//        } catch (Exception ex) {
//        }
//    }
//
//    @Test
//    public void register() {
//
////        User user = authService.register("admin", "123456","管理员","18612312326");
////        log.info("{}", user);
//    }
//
//    @Test
//    public void getAuth() {
//
//        if (authService == null) {
//        try {
//            var user = authService.login("admin", "123456");
//            log.info("login success.{}", user);
//            log.info("get auth:{}", authService.getAuth(user.getToken()));
//        } catch (Exception ex) {
//
//            log.info("error:", ex);
//
//        }
//    }
//}

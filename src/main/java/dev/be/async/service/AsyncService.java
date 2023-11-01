package dev.be.async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncService {

    private final EmailService emailService;

    public void asyncCall_1(){
        log.info("[asyncCall_1] :: {}", Thread.currentThread().getName());
        emailService.sendMail();
        emailService.sendMailWithCustomThreadPool();
    }

    public void asyncCall_2(){
        log.info("[asyncCall_2] :: {}", Thread.currentThread().getName());
        EmailService emailService = new EmailService();
        emailService.sendMail();
        emailService.sendMailWithCustomThreadPool();
    }

    public void asyncCall_3(){
        log.info("[asyncCall_3] :: {}", Thread.currentThread().getName());
        sendMail();
    }

    @Async
    public void sendMail(){
        log.info("[sendMail] :: {}", Thread.currentThread().getName());
    }
}

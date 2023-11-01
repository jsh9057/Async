package dev.be.async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Async("defaultTaskExecutor")
    public void sendMail(){
        log.info("[sendMail] :: {}", Thread.currentThread().getName());
    }

    @Async("messagingTaskExecutor")
    public void sendMailWithCustomThreadPool(){
        log.info("[messagingTaskExecutor] :: {}", Thread.currentThread().getName());
    }
}

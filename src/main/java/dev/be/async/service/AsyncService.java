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
        /*
        비동기로 동작하려면 스프링 프레임 워크의 도움을 받아야함
        등록된 빈을 가져올 때 순수한 빈을 Async 서비스에게 반환하는게 아니라
        프록시 객체로 wrapping 을 해줘서 그 프록시 객체를 리턴해 주게 됨

        - 비동기로 동작할 수 있게 Sub Thread에게 위임
         */
    }

    public void asyncCall_2(){  // 인스턴스를 직접 만들기 때문에 스프링 프레임 워크의 도움을 받지 못함
        log.info("[asyncCall_2] :: {}", Thread.currentThread().getName());
        EmailService emailService = new EmailService();
        emailService.sendMail();
        emailService.sendMailWithCustomThreadPool();
    }

    public void asyncCall_3(){  // 자주 실수하는 케이스, 이미 빈으로 등록되어(AsyncService) 스프링 프레임워크의 도움을 받지 못함
        log.info("[asyncCall_3] :: {}", Thread.currentThread().getName());
        sendMail();
    }

    @Async
    public void sendMail(){
        log.info("[sendMail] :: {}", Thread.currentThread().getName());
    }
}

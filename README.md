## 비동기 프로그래밍이란?

- Async 한 통신
- **실시간성** 응답을 필요로 하지 않는 상황에서 사용
- ex) Notification, Email 전송, Push 알림
- 개발자답게 표현해보자면
- Main Thread가 Task를 처리하는 게 아니라
- Sub Thread에게 Task를 위임하는 행위라고 말할 수 있다.

### Sub Thread는 어떻게 생성하고 어떻게 관리를 해야 할까?

- Spring 에서 비동기 프로그래밍을 하기 위해선
- ThreadPool을 정의할 필요가 있다.

## ThreadPool 생성이 필요한 이유

- 비동기 Main Thread가 아닌 Sub Thread에서 작업이 진행
- Java에서는 ThreadPool을 새성하여 Async 작업을 처리

### ThreadPool 옵션

- CorePoolSize : 해당 Thread Pool에 최소한의 Thread를 몇개를 가지고 있을 것이냐를 지정하는 옵션
- MaxPoolSize : 최대 몇개 까지 Thread를 할당할 것인지를 지정하는 옵션
- WorkQueue : 먼저들어온 요청을 먼저 처리할 수 있는 자료구조 큐를 사용하여 워크큐라는 곳에 여러 요청들을 담아 넣아 놨다가 작업하고 있는 Thread 들이 현재 Task가 마무리되면 Thread에서 다음 작업할 Task를 가져 온다.
- KeepAliveTime : 지정한 시간만큼 Thread 들이 일을 하지 않으면 자원을 반납하겠다 라는 옵션 (CorePoolSize 를 초과하는 Thread 들)
- 순서
    - CorePoolSize 만큼 Thread를 할당
    - ex) CorePoolSize = 3 ,Request = 4
    - 바로 4번 째 스레드를 바로 생성하지않고 WorkQueue에 담아둠
    - WorkQueue 사이즈 만큼 새로운 요청을 계속해서 담게됨
    - WorkQueue 사이즈가 꽉 찼을 때 MaxPoolSize 만큼 Thread 를 생성함

### ThreadPool 생성시 주의해야할 부분(1)

- CorePoolSize 값을 너무 크게 설정할 경우 Side Effect 고려해보기
    - CorePoolSize 만큼 무조건 자원을 점유하고있기 때문에 너무 큰 값을 설정하면 많은 Thread를 점유하고 있기때문. 적절하게 값을 조절해서 설정 해야함
    - 보통 기존의 프로젝트에 있는 값을 참조해서 설정하는게 일반적인 설정 방법

### ThreadPool 생성시 주의해야할 부분(2)

- IllegalArgumentException
    - corePoolSize < 0
    - keepAliveTime < 0
    - maximumPoolSize ≤ 0
    - maximumPoolSize < corePoolSize
- workQueue is null - NullPointException
- 중 하나라도 해당 되면 Exception 발생

### ThreadPool 정리 - CorePoolSize

```java
if ( Thread 수 < CorePoolSize )
	New Thread 생성

if ( Thread 수 > CorePoolSiez )
	Queue에 요청 추가
```

### ThreadPool 정리 - MaxPoolSize

```java
if ( Queue Full && Thread 수 < MaxPoolSize )
	New Thread 생성

if ( Queue Full && Thread 수 > MaxPoolSize )
	요청 거절
```

- 핸들링을 할수 있음
- 커스텀하게 Exception 을 발생시키거나 해당 요청을 무시할 수 있음

## Spring에서 Async 동작 원리

<img width="687" alt="async1" src="https://github.com/jsh9057/Async/assets/31503178/6ecfa80d-d3d7-4e35-91fc-f67fd5521919">

- Caller(AsyncService) 가 이메일 서비스를 호출
    - 여기서 스프링이 개입 하여 순수한 이메일 서비스 빈이 아니라
    - 빈을 랩핑한 이메일 서비스를 사용하도록하여 Async 하게 동작할 수 있는 메커니즘을 제공함
- **순수한 빈을 가져오는게 아니라 빈을 랩핑한 프록시 객체**를 받아온다.
- 그 사이에서 **스프링이 비동기로** 동작할 수 있도록 지원해 준다.

```java
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
```

### Spring에서 Async 사용 시 주의해야할 부분 - 문제원인

<img width="434" alt="async2" src="https://github.com/jsh9057/Async/assets/31503178/dbd1dd1e-b21c-498f-a8a4-b2c0eb4377d4">

- 스프링에서 관리하는 빈으로 등록해야 정상적으로 사용할 수 있다
    - 즉, 인스턴스를 직접 생성(new ) 하면 Async 하게 작동 x
- @Async 를 붙인다고 꼭 Async 하게 동작하지않는다
- 테스트를 만들어 확인하는 습관 필요.
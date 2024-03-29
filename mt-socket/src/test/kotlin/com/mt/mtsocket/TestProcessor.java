package com.mt.mtsocket;

import com.mt.mtsocket.common.Util;
import com.mt.mtsocket.socket.SocketSessionStore;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by gyh on 2020/4/6.
 */
//@RunWith(MockitoJUnitRunner.class)
public class TestProcessor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testList() {
        List<String> s = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
        while (s.size() >= 2) {
            String s1 = s.remove(0);
            String s2 = s.remove(0);
            System.out.println(s1 + s2);
        }
        SocketSessionStore.INSTANCE.getUserInfoMap$mt_socket();
    }

    @Test
    public void testQuery() {
        Util.INSTANCE.getQueryMap("roomId=27&bearer= eyJhbGciOiJIUzI1NiJ9.eyJpZCI6NSwicm9sZXMiOiJbXCJBRE1JTlwiLFwiVVNFUlwiXSIsIm5iZiI6MTU4OTI2OTY5MywiZXhwIjoxNTg5ODc0NDkzfQ.u5D7OTTiUKp54zcsUeJdJc3qAKlStrekqpSbeX_MI2g&userName=%E8%B4%A6%E5%8A%A1")
                .forEach((k, v) -> {
                    System.out.println(k + " " + v);
                });
    }

    @Test
    public void testFlatMap() throws InterruptedException {
        Mono<String> mono = Mono.just("12")
                .flatMap(it -> {
                    System.out.println(it);
                    return Mono.just("2");
                }).flatMap(it -> {
                    System.out.println(it);
                    return Mono.empty();
                });

        Thread t1 = new Thread(mono::block);
        Thread t2 = new Thread(mono::block);
        Thread t3 = new Thread(mono::block);
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    @Test
    public void testTime() throws InterruptedException {
        EmitterProcessor<Integer> directProcessor = EmitterProcessor.create(8);
        logger.info("start ()");

        Disposable disp = Flux.interval(Duration.ofSeconds(1),Schedulers.elastic())
                .map(it -> {
                    directProcessor.onNext(it.intValue() + 1);
                    logger.info("发布 " + it);
                    return it;
                })
                .doOnCancel(() -> logger.info("取消发布"))
                .subscribe();

        Mono<Void> flux = directProcessor
                .map( it -> {
                    logger.info("接受 " + it);
                    if (it > 5) {
                        logger.info("取消");
                        directProcessor.onComplete();
                        disp.dispose();
                    }
                    return it;
                }).then()
                .map(it -> {logger.info("接受>>>> " + it); return it;} )
                .doOnCancel(() -> logger.info("取消接收"));

        directProcessor.onNext(0);
        Thread.sleep(10_000);
        flux.subscribe(it -> logger.info("nmkasubscribe:"));

        logger.info("wangc");
        directProcessor.blockLast();
        directProcessor.onComplete();
    }

    @Test
    public void testMono() throws InterruptedException {
        MonoProcessor<Object> connectedProcessor = MonoProcessor.create();
        Mono<Void> mono = connectedProcessor
                .map(it -> {
                    logger.info("1 {} ", it);
                    return it;
                })
                .flatMap(it -> Flux.fromArray(new Integer[]{1, 2, 3})
                        .map(i -> {
                            logger.info(">> {}", i);
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return i;
                        }).then());
        Mono<Object> mono2 = connectedProcessor.map(it -> {
            logger.info("2 {} ", it);
            return it;
        }).switchIfEmpty(Mono.defer(() -> {
            System.out.println("empty");
            return Mono.just("nmka122");
        }));

        mono.subscribe();
        logger.info("step 2");
        TimeUnit.SECONDS.sleep(1);
        logger.info("step 3");
        connectedProcessor.onNext("nmka");
        mono2.subscribe();
        logger.info("step 4");
        connectedProcessor.onComplete();
        logger.info("step 5");
    }

    @Test
    public void testTime2() {
        Disposable disp = Flux.interval(Duration.ofSeconds(1))
                .map( it -> {
                    logger.info("{}", it);
                    return it;
                })
                .flatMap( it -> {
                    if (it < 5) return Mono.just(1);
                    return Mono.empty();
                })
                .subscribe();
        disp.dispose();
        logger.info("完成");
    }

    @Test
    public void testDirectProcessor() {
        DirectProcessor<Integer> directProcessor = DirectProcessor.create();
        Flux<Integer> flux = directProcessor
                .map(e -> e + 1);

        flux.subscribe(it -> logger.info("nmkasubscribe:{}", it));

        IntStream.range(1, 20)
                .forEach((it) -> {
                    logger.info("发布 " + it);
                    directProcessor.onNext(it);
                });

        directProcessor.onComplete();
        directProcessor.blockLast();
    }

    @Test
    public void testUnicastProcessor() throws InterruptedException {
        UnicastProcessor<Integer> unicastProcessor = UnicastProcessor.create(Queues.<Integer>get(18).get());
        Flux<Integer> flux = unicastProcessor
                .map(e -> e)
                .doOnError(e -> {
                    logger.error(e.getMessage(), e);
                });

        IntStream.rangeClosed(1, 12)
                .forEach(e -> {
                    logger.info("emit:{}", e);
                    unicastProcessor.onNext(e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
        logger.info("begin to sleep 7 seconds");
        TimeUnit.SECONDS.sleep(7);
        //UnicastProcessor allows only a single Subscriber
        flux.onErrorReturn(50000).subscribe(e -> {
            logger.info("flux subscriber:{}", e);
        });

        unicastProcessor.onComplete();
        TimeUnit.SECONDS.sleep(10);
        //unicastProcessor.blockLast(); //blockLast也是一个subscriber
    }

    @Test
    public void testEmitterProcessor() throws InterruptedException {
        int bufferSize = 3; //小于8的会被重置为8
        FluxProcessor<Integer, Integer> processor = EmitterProcessor.create(bufferSize);
        Flux<Integer> flux1 = processor.map(e -> e).subscribeOn(Schedulers.elastic());
        Flux<Integer> flux2 = processor.map(e -> e * 10).subscribeOn(Schedulers.elastic());

        IntStream.rangeClosed(1, 8).forEach(e -> {
            logger.info("emit:{}", e);
            processor.onNext(e); //如果发布的未消费数据超过bufferSize,则会阻塞在这里
        });

        flux1.subscribe(e -> {
            logger.info("flux1 subscriber:{}", e);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        Thread.sleep(100);
        flux2.subscribe(e -> {
            logger.info("flux2 subscriber:{}", e);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        logger.info("nmka");
        Thread.sleep(1_000);

        processor.onNext(20);
        processor.onNext(21);
        processor.onNext(22);

        Thread.sleep(3_000);
        processor.onComplete();
        processor.blockLast();
    }

    public static void main(String[] args) throws InterruptedException {
        new TestProcessor().testEmitterProcessor();
    }

    @Test
    public void testReplayProcessor() throws InterruptedException {
        ReplayProcessor<Integer> replayProcessor = ReplayProcessor.create(3);
        Flux<Integer> flux1 = replayProcessor
                .map(e -> e);
        Flux<Integer> flux2 = replayProcessor
                .map(e -> e);

        flux1.subscribe(e -> {
            logger.info("flux1 subscriber:{}", e);
        });


        IntStream.rangeClosed(1, 5)
                .forEach(e -> {
                    replayProcessor.onNext(e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });

        logger.info("finish publish data");
        TimeUnit.SECONDS.sleep(1);

        logger.info("begin to subscribe flux2");
        flux2.subscribe(e -> {
            logger.info("flux2 subscriber:{}", e);
        });

        replayProcessor.onComplete();
        replayProcessor.blockLast();
    }

    int i = 0;

}

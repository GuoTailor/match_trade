package com.mt.mtsocket;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.IntStream;

/**
 * Created by gyh on 2020/4/6.
 */
//@RunWith(MockitoJUnitRunner.class)
public class TestProcessor {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Test
    public void testDirectProcessor() {
        DirectProcessor<Integer> directProcessor = DirectProcessor.create();
        Flux<Integer> flux = directProcessor
                .map(e -> e + 1);

        flux.subscribe(new Subscriber<Integer>() {
            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                LOGGER.info("nmka");
                this.s = s;
                s.request(40);
            }

            @Override
            public void onNext(Integer integer) {
                LOGGER.info("subscribe:{}", integer);
            }

            @Override
            public void onError(Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }

            @Override
            public void onComplete() {

            }
        });

        IntStream.range(1, 20)
                .forEach((it) -> {
                    LOGGER.info("发布 " + it);
                    directProcessor.onNext(it);
                });

        directProcessor.onComplete();
        directProcessor.blockLast();
    }

    @Test
    public void testUnicastProcessor() throws InterruptedException {
        UnicastProcessor<Integer> unicastProcessor = UnicastProcessor.create(Queues.<Integer>get(8).get());
        Flux<Integer> flux = unicastProcessor
                .map(e -> e)
                .doOnError(e -> {
                    LOGGER.error(e.getMessage(), e);
                });

        IntStream.rangeClosed(1, 12)
                .forEach(e -> {
                    LOGGER.info("emit:{}", e);
                    unicastProcessor.onNext(e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
        LOGGER.info("begin to sleep 7 seconds");
        TimeUnit.SECONDS.sleep(7);
        //UnicastProcessor allows only a single Subscriber
        flux.onErrorReturn(50000).subscribe(e -> {
            LOGGER.info("flux subscriber:{}", e);
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
            LOGGER.info("emit:{}", e);
            processor.onNext(e); //如果发布的未消费数据超过bufferSize,则会阻塞在这里
        });

        flux1.subscribe(e -> {
            LOGGER.info("flux1 subscriber:{}", e);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        Thread.sleep(100);
        flux2.subscribe(e -> {
            LOGGER.info("flux2 subscriber:{}", e);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        LOGGER.info("nmka");
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
            LOGGER.info("flux1 subscriber:{}", e);
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

        LOGGER.info("finish publish data");
        TimeUnit.SECONDS.sleep(1);

        LOGGER.info("begin to subscribe flux2");
        flux2.subscribe(e -> {
            LOGGER.info("flux2 subscriber:{}", e);
        });

        replayProcessor.onComplete();
        replayProcessor.blockLast();
    }

    int i = 0;

    @Test
    public void testTopicProcessor() throws InterruptedException {
        TopicProcessor<Integer> topicProcessor = TopicProcessor.<Integer>builder()
                .share(true)
//                .executor(Executors.newSingleThreadExecutor())
                .build();
        Flux<Integer> flux1 = topicProcessor
                .map(e -> e);
        Flux<Integer> flux2 = topicProcessor
                .map(e -> e);
        Flux<Integer> flux3 = topicProcessor
                .map(e -> e);

        AtomicInteger count = new AtomicInteger(0);
        flux1.subscribe(e -> {
            if (e % 10 == 0)
                LOGGER.info("flux1 subscriber:{}", e);
            count.incrementAndGet();
            i++;
        });
        flux2.subscribe(e -> {
            if (e % 10 == 0)
                LOGGER.info("flux2 subscriber:{}", e);
        });
        flux3.subscribe(e -> {
            if (e % 10 == 0)
                LOGGER.info("flux3 subscriber:{}", e);
        });

        IntStream.rangeClosed(1, 100)
                .parallel()
                .forEach(e -> {
                    if (e % 10 == 0)
                        LOGGER.info("emit:{}", e);
                    topicProcessor.onNext(e);
                });

        topicProcessor.onComplete();
        topicProcessor.blockLast();

        TimeUnit.SECONDS.sleep(10);
        System.out.println(count.get());
        System.out.println(i);
    }

    @Test
    public void testWorkQueueProcessor() {
        WorkQueueProcessor<Integer> workQueueProcessor = WorkQueueProcessor.create();
        Flux<Integer> flux1 = workQueueProcessor
                .map(e -> e);
        Flux<Integer> flux2 = workQueueProcessor
                .map(e -> e);
        Flux<Integer> flux3 = workQueueProcessor
                .map(e -> e);

        flux1.subscribe(e -> {
            LOGGER.info("flux1 subscriber:{}", e);
        });
        flux2.subscribe(e -> {
            LOGGER.info("flux2 subscriber:{}", e);
        });
        flux3.subscribe(e -> {
            LOGGER.info("flux3 subscriber:{}", e);
        });

        IntStream.range(1, 20)
                .forEach(e -> {
                    workQueueProcessor.onNext(e);
                });

        workQueueProcessor.onComplete();
        workQueueProcessor.blockLast();
    }
}
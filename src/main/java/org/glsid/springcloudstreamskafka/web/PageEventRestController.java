package org.glsid.springcloudstreamskafka.web;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.glsid.springcloudstreamskafka.entities.PageEvent;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class PageEventRestController {

    private final StreamBridge streamBridge;
    private final InteractiveQueryService interactiveQueryService;

    @GetMapping("publish/{topic}/{name}")
    public PageEvent publish(@PathVariable String topic, @PathVariable String name) {
        PageEvent pageEvent = new PageEvent(name, Math.random() < 0.5 ? "user1" : "user2", new Date(), new Random().nextInt(9000));
        streamBridge.send(topic, pageEvent);
        return pageEvent;
    }

    @GetMapping(value = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String,Long>> analytics(){
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq->{
                    Map<String,Long> stringLongMap=new HashMap<>();
                    ReadOnlyWindowStore<String, Long> windowStore = interactiveQueryService.getQueryableStore("count-store", QueryableStoreTypes.windowStore());
                    Instant now=Instant.now();
                    Instant from=now.minusSeconds(5);
                    KeyValueIterator<Windowed<String>, Long> keyValueIterator = windowStore.fetchAll(from, now);
                    while (keyValueIterator.hasNext()){
                        KeyValue<Windowed<String>, Long> next = keyValueIterator.next();
                        stringLongMap.put(next.key.key(),next.value);
                    }
                    return stringLongMap;
                }).share(); // share() to make sure the Flux is shared between multiple subscribers
    }

    @GetMapping(value = "/analytics/one",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String,Long>> analytics2(){
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq->{
                    Map<String,Long> map=new HashMap<>();
                    ReadOnlyKeyValueStore<String, Long> stats = interactiveQueryService.getQueryableStore("count-store", QueryableStoreTypes.keyValueStore());
                    Instant now=Instant.now();
                    Instant from=now.minusSeconds(5);
                    KeyValueIterator<String, Long> keyValueIterator = stats.all();
                    while (keyValueIterator.hasNext()){
                        KeyValue<String, Long> next = keyValueIterator.next();
                        map.put(next.key,next.value);
                    }
                    return map;
                });
    }

}

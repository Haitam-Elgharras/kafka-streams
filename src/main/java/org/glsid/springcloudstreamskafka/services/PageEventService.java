package org.glsid.springcloudstreamskafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.glsid.springcloudstreamskafka.entities.PageEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class PageEventService {

    @Bean
    public Consumer<PageEvent> pageEventConsumer() {
        return pageEvent -> {
            System.out.println("***********************************");
            System.out.println("PageEvent: " + pageEvent.toString());
            System.out.println("***********************************");
        };
    }

    @Bean
    public Supplier<PageEvent> pageEventSupplier(){
        return ()-> {
//            System.out.println("supplier");
            return PageEvent.builder()
                    .page((Math.random() > 0.5) ? "P1" : "P2")
                    .user((Math.random() > 0.5) ? "U1" : "U2")
                    .date(new Date())
                    .duration(new Random().nextInt(1000))
                    .build();
        };
    }

    @Bean
    public Function<PageEvent, PageEvent> pageEventFunction(){
        return pageEvent -> {
            pageEvent.setPage("tatatatat");
            pageEvent.setUser("YYYYYYYY");
            return pageEvent;
        };
    }

    // Stream Processing
    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> kStreamFunction() {
        return (input) -> input
                .filter((k, v) -> v.getDuration() > 500) // Filter events with duration > 500
                .map((k, v) -> new KeyValue<>(v.getPage(), 0L)) // Map to page as key and 0L as value
                .groupBy((k, v) -> k, Grouped.with(Serdes.String(), Serdes.Long())) // Group by page
                .windowedBy(TimeWindows.of(Duration.ofSeconds(5000))) // Windowing by 5000 seconds
                .count() // returns a KTable<Windowed<String>, Long> where the value is the count of occurrences
                .toStream()
                .map((k, v) -> new KeyValue<>("=>" + k.window().startTime() + k.window().endTime()+ " " + k.key(), v)); // Map to new key format
    }

    }

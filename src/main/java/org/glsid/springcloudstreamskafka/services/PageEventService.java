package org.glsid.springcloudstreamskafka.services;

import org.glsid.springcloudstreamskafka.entities.PageEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

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
}

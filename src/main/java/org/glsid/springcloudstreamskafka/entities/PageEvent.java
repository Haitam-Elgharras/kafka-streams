package org.glsid.springcloudstreamskafka.entities;

import lombok.*;

import java.util.Date;

@Data @AllArgsConstructor @NoArgsConstructor @ToString @Builder
public class PageEvent {
    private String page;
    private String user;
    private Date date;
    private long duration;
}

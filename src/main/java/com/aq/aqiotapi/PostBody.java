package com.aq.aqiotapi;

import lombok.Data;

@Data
public class PostBody {
    String event;
    String signal;
    String payload;
}

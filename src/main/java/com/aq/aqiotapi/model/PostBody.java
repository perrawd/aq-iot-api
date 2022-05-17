package com.aq.aqiotapi.model;

import lombok.Data;

@Data
public class PostBody {
    String event;
    String signal;
    String payload;
}

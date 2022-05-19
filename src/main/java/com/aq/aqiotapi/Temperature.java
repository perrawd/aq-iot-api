package com.aq.aqiotapi;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Measurement(name = "temperature")
@Data
public class Temperature {
    @Column(tag = true)
    String location;

    @Column
    int value;

    @Column(timestamp = true)
    Instant time;
}

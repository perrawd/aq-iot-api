package com.aq.aqiotapi.model;

import com.influxdb.annotations.Column;
import lombok.Data;

import java.time.Instant;

@com.influxdb.annotations.Measurement(name = "measurement")
@Data
public class Measurement {
    @Column
    int temperature;

    @Column
    int humidity;

    @Column(timestamp = true)
    Instant time;
}

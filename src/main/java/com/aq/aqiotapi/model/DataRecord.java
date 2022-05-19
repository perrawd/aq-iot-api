package com.aq.aqiotapi.model;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Measurement(name = "data-record")
@Data
public class DataRecord {
    @Column(name = "temperature")
    Double temperature;

    @Column(name = "humidity")
    Double humidity;

    @Column(timestamp = true)
    Instant time;
}

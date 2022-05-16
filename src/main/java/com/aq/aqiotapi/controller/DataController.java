package com.aq.aqiotapi.controller;

import com.aq.aqiotapi.Temperature;
import com.aq.aqiotapi.utils.PropertyUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class DataController {

    private static String dbUrl  = String.valueOf(PropertyUtil.getProperties().getProperty("influx.url"));
    private static char[] token = String.valueOf(PropertyUtil.getProperties().getProperty("influx.token")).toCharArray();
    private static String org = String.valueOf(PropertyUtil.getProperties().getProperty("influx.org"));
    private static String bucket  = String.valueOf(PropertyUtil.getProperties().getProperty("influx.bucket"));

    InfluxDBClient influxDBClient;
    WriteApiBlocking writeApi;

    DataController() {
        influxDBClient = InfluxDBClientFactory.create(dbUrl, token, org, bucket);
        writeApi = influxDBClient.getWriteApiBlocking();
    }


}

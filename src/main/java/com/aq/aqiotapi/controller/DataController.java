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

    InfluxDBClient influxDBClient;
    WriteApiBlocking writeApi;

    private static final String dbUrl  = String.valueOf(PropertyUtil.getProperties().getProperty("influx.url"));
    private static final char[] token = String.valueOf(PropertyUtil.getProperties().getProperty("influx.token")).toCharArray();
    private static final String org = String.valueOf(PropertyUtil.getProperties().getProperty("influx.org"));
    private static final String bucket  = String.valueOf(PropertyUtil.getProperties().getProperty("influx.bucket"));

    DataController() {
        influxDBClient = InfluxDBClientFactory.create(dbUrl, token, org, bucket);
        writeApi = influxDBClient.getWriteApiBlocking();
    }

    // GET. Collection of all resources.
    @GetMapping("/temps")
    public CollectionModel<EntityModel<Temperature>> all() {

        List<EntityModel<Temperature>> temperatures = new ArrayList<EntityModel<Temperature>>();

        String flux = String.format("from(bucket:\"%s\") |> range(start: 0)", bucket);

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(flux);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Temperature temperature = new Temperature();
                temperature.setLocation("Placeholder");
                temperature.setValue("Placeholer");
                temperature.setTime(fluxRecord.getTime());
                EntityModel<Temperature> temperatureEntityModel = EntityModel.of(temperature);
                temperatures.add(temperatureEntityModel);
            }
        }

        return CollectionModel.of(temperatures, linkTo(methodOn(DataController.class).all()).withSelfRel());
    }
}

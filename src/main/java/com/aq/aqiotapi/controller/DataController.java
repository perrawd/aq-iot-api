package com.aq.aqiotapi.controller;

import com.aq.aqiotapi.model.Measurement;
import com.aq.aqiotapi.model.PostBody;
import com.aq.aqiotapi.Temperature;
import com.aq.aqiotapi.utils.PropertyUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;

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
                temperature.setLocation(fluxRecord.getValueByKey("location").toString());
                temperature.setValue(fluxRecord.getValueByKey("_value").toString());
                temperature.setTime(fluxRecord.getTime());
                EntityModel<Temperature> temperatureEntityModel = EntityModel.of(temperature);
                temperatures.add(temperatureEntityModel);
            }
        }

        return CollectionModel.of(temperatures, linkTo(methodOn(DataController.class).all()).withSelfRel());
    }

    // POST. Post of new resource.
    @PostMapping("/temps")
    ResponseEntity<?> newResource(@RequestBody PostBody body) {

        String[] payload = body.getPayload().split(",");

        int temperature = Integer.parseInt(payload[0].replace("[", "").trim());
        int humidity = Integer.parseInt(payload[1].replace("[", "").trim());

        Instant dateTime = LocalDateTime.parse(payload[2]
                                  .replace("]", "")
                                  .trim())
                                  .atZone(ZoneId.of("Europe/Stockholm"))
                                  .toInstant().plus(Duration.ofHours(2));

        Measurement measurement = new Measurement();
        measurement.setTemperature(temperature);
        measurement.setHumidity(humidity);
        measurement.setTime(dateTime);

        writeApi.writeMeasurement( WritePrecision.NS, measurement);
        EntityModel<Measurement> entityModel = EntityModel.of(measurement);

        System.out.println("Data ingested to influx.");

        return ResponseEntity //
                .created(URI.create("/temps/")) //
                .body(entityModel);
    }
}

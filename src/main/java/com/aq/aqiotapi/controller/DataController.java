package com.aq.aqiotapi.controller;

import com.aq.aqiotapi.model.DataRecord;
import com.aq.aqiotapi.model.PostBody;
import com.aq.aqiotapi.utils.PropertyUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    @GetMapping("/api")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public CollectionModel<EntityModel<DataRecord>> all() {

        List<EntityModel<DataRecord>> temperatures = new ArrayList<EntityModel<DataRecord>>();

        String flux = String.format("cel = from(bucket: \"%s\")\n" +
                "    |> range(start: -3d)\n" +
                "    |> tail(n: 100, offset: 0)\n" +
                "    |> filter(fn: (r) => r._measurement == \"data-record\" and r._field == \"celcius\")\n" +
                "\n" +
                "hum = from(bucket: \"%<s\")\n" +
                "    |> range(start: -3d)\n" +
                "    |> tail(n: 100, offset: 0)\n" +
                "    |> filter(fn: (r) => r._measurement == \"data-record\" and r._field == \"percent\")\n" +
                "\n" +
                "join(\n" +
                "    tables: {c:cel, h:hum},\n" +
                "    on: [\"_time\"],\n" +
                ")", bucket);

        System.out.println(flux);
        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(flux);
        System.out.println(tables);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                DataRecord temperature = new DataRecord();
                temperature.setTemperature((Double) fluxRecord.getValueByKey("_value_c"));
                temperature.setHumidity((Double) fluxRecord.getValueByKey("_value_h"));
                temperature.setTime(fluxRecord.getTime());
                EntityModel<DataRecord> temperatureEntityModel = EntityModel.of(temperature);
                temperatures.add(temperatureEntityModel);
            }
        }

        return CollectionModel.of(temperatures, linkTo(methodOn(DataController.class).all()).withSelfRel());
    }

    // POST. Post of new resource.
    @PostMapping("/api")
    ResponseEntity<?> newResource(@RequestBody PostBody body) {

        String[] payload = body.getPayload().split(",");

        Double temperature = Double.parseDouble(payload[0].replace("[", "").trim());
        Double humidity = Double.parseDouble(payload[1].replace("[", "").trim());

        DataRecord temp = new DataRecord();
        temp.setTemperature(temperature);
        temp.setHumidity(humidity);

        String timestamp = payload[2]
                .replace("]", "")
                .trim();

        System.out.println(timestamp);

        writeApi.writeRecord(WritePrecision.NS, "data-record,value1=temperature,value2=humidity celcius=%s,percent=%s".formatted(temperature,humidity));


        EntityModel<DataRecord> entityModel = EntityModel.of(temp);

        System.out.println("Data ingested to influx.");

        return ResponseEntity //
                .created(URI.create("/api/")) //
                .body(entityModel);
    }
}

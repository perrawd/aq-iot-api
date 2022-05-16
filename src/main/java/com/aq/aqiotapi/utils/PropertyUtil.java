package com.aq.aqiotapi.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@UtilityClass
public class PropertyUtil {

    public Properties getProperties () {
        Properties properties = new Properties();
        ClassLoader classLoader = PropertyUtil.class.getClassLoader();
        InputStream applicationPropertiesStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(applicationPropertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}

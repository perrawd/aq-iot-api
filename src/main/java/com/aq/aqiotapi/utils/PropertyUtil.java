package com.aq.aqiotapi.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@UtilityClass
public class PropertyUtil {

    public Properties getProperties () throws IOException {
        Properties properties = new Properties();
        ClassLoader classLoader = PropertyUtil.class.getClassLoader();
        InputStream applicationPropertiesStream = classLoader.getResourceAsStream("application.properties");
        properties.load(applicationPropertiesStream);
        return properties;
    }
}

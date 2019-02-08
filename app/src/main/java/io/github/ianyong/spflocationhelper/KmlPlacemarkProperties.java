package io.github.ianyong.spflocationhelper;

import java.util.HashMap;

public class KmlPlacemarkProperties {

    private String name;
    private HashMap<String, String> properties;

    public KmlPlacemarkProperties(String name) {
        this.name = name;
        properties = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Iterable getProperties() {
        return properties.entrySet();
    }

    public String getProperty(String keyValue) {
        return properties.get(keyValue);
    }

    public void setProperty(String keyValue, String propertyValue) {
        properties.put(keyValue, propertyValue);
    }

    public boolean hasProperties() {
        return properties.size() > 0;
    }

    public boolean hasProperty(String keyValue) {
        return properties.containsKey(keyValue);
    }

}

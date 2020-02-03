package org.georunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;


public class Route {
    public String name;
    private String values;
    private String ids;
    private int count;

    Route() {
        name="";
    }

    public Route(String name, String values, String ids, int count) {
        this.name = name;
        this.values = values;
        this.ids = ids;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.replace("dict","Route ");
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    InputStream getValues() {
        return new ByteArrayInputStream(values.getBytes(Charset.forName("UTF-8")));
    }

    void setValues(String values) {
        this.values = values;
    }

    public String getIds() {
        return ids;
    }

    void setIds(String ids) {
        this.ids = ids;
    }
}

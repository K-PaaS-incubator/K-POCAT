package io.pocat.platform.gateway.config;

import java.util.ArrayList;
import java.util.List;

public class LogHandlerType {
    private String type;
    private List<NameValueType> params = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NameValueType> getParams() {
        return params;
    }

    public void addParam(NameValueType param) {
        params.add(param);
    }
}

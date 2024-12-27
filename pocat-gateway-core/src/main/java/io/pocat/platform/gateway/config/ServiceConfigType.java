/*
 * Copyright 2024. dongobi soft inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pocat.platform.gateway.config;

import java.util.ArrayList;
import java.util.List;

public class ServiceConfigType {
    private String name;
    private String protocol;
    private String routeGroup;
    private AccessLoggerType accessLogger;
    private List<NameValueType> params = new ArrayList<>();
    private List<ConnectorConfigType> connectors = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRouteGroup() {
        return routeGroup;
    }

    public void setRouteGroup(String routeGroup) {
        this.routeGroup = routeGroup;
    }

    public AccessLoggerType getAccessLogger() {
        return accessLogger;
    }

    public void setAccessLogger(AccessLoggerType accessLogger) {
        this.accessLogger = accessLogger;
    }

    public List<NameValueType> getServiceParams() {
        return params;
    }

    public void addServiceParams(NameValueType param) {
        this.params.add(param);
    }

    public List<ConnectorConfigType> getConnectors() {
        return connectors;
    }

    public void addConnectors(ConnectorConfigType connector) {
        this.connectors.add(connector);
    }
}

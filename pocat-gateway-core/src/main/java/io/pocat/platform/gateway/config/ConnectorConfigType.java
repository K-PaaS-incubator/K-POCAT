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

public class ConnectorConfigType {
    private String name;
    private int port;
    private int acceptor = Runtime.getRuntime().availableProcessors();
    private int selector = Runtime.getRuntime().availableProcessors()*4;
    private TLSConfigType tlsConfig;
    private boolean enableWebSocket = false;
    private ServerConnectorOptionsType options = new ServerConnectorOptionsType();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public int getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(int acceptor) {
        this.acceptor = acceptor;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }

    public ServerConnectorOptionsType getOptions() {
        return this.options;
    }

    public void setConnectorOption(NameValueType option) {
        this.options.setOption(option);
    }

    public TLSConfigType getTlsConfig() {
        return this.tlsConfig;
    }

    public void setTlsConfig(TLSConfigType tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    public boolean isEnableWebsocket() {
        return this.enableWebSocket;
    }

    public void setEnableWebSocket(boolean enableWebSocket) {
        this.enableWebSocket = enableWebSocket;
    }
}

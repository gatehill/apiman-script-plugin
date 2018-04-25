package com.gatehill.apiman.plugin.script.model;

import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.util.Map;

/**
 * @author pete
 */
public class ResponseWrapper {
    private final ApiResponse response;
    private final IApimanBuffer buffer;
    private String stringBuffer;
    private boolean bufferDirty;

    public ResponseWrapper(ApiResponse response, IApimanBuffer buffer) {
        this.response = response;
        this.buffer = buffer;
    }

    public String getBody() {
        if (null == buffer) {
            throw new IllegalStateException("Body capture is not enabled in this plugin configuration");
        }
        if (null == stringBuffer) {
            stringBuffer = buffer.toString();
        }
        return stringBuffer;
    }

    public void setBody(String body) {
        bufferDirty = true;
        stringBuffer = body;
    }

    public String getStringBuffer() {
        return stringBuffer;
    }

    public boolean isBufferDirty() {
        return bufferDirty;
    }

    public int getStatusCode() {
        return response.getCode();
    }

    public Map<String, String> getHeaders() {
        return response.getHeaders().toMap();
    }
}

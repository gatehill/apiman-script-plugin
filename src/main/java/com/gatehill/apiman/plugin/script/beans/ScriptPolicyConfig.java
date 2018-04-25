package com.gatehill.apiman.plugin.script.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for the script policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ScriptPolicyConfig {
    @JsonProperty
    private String scriptFile;

    @JsonProperty
    private Boolean captureBody = false;

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public Boolean getCaptureBody() {
        return captureBody;
    }

    public void setCaptureBody(Boolean captureBody) {
        this.captureBody = captureBody;
    }
}

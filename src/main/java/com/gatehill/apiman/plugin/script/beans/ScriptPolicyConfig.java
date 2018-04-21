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

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }
}

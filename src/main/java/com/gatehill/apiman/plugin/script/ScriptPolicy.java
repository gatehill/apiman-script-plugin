/*
 * Copyright 2018 Pete Cornish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gatehill.apiman.plugin.script;

import com.gatehill.apiman.plugin.script.beans.ScriptPolicyConfig;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * Delegates response processing to a script.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class ScriptPolicy extends AbstractMappedDataPolicy<ScriptPolicyConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptPolicy.class);

    /**
     * Shared per https://stackoverflow.com/a/30159424/1691669
     */
    private final ScriptEngine engine;

    @Override
    protected Class<ScriptPolicyConfig> getConfigurationClass() {
        return ScriptPolicyConfig.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, ScriptPolicyConfig config, IPolicyChain<ApiRequest> chain) {
        context.setAttribute("url", request.getUrl());
        chain.doApply(request);
    }

    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, ScriptPolicyConfig config, IPolicyChain<ApiResponse> chain) {
        context.setAttribute("responseHeaders", response.getHeaders().toMap());
        chain.doApply(response);
    }

    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request, IPolicyContext context, ScriptPolicyConfig config) {
        return null;
    }

    public ScriptPolicy() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("javascript");
    }

    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response, IPolicyContext context, ScriptPolicyConfig config) {
        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        final IApimanBuffer buffer = bufferFactory.createBuffer();

        return new AbstractStream<ApiResponse>() {
            @Override
            protected void handleHead(ApiResponse head) {
            }

            @Override
            public ApiResponse getHead() {
                return response;
            }

            @Override
            public void write(IApimanBuffer chunk) {
                buffer.append(chunk);
            }

            @Override
            public void end() {
                final String url = context.getAttribute("url", null);

                // a bindings should be created for each invocation in a multithreaded environment,
                // if sharing an engine, per https://stackoverflow.com/a/30159424/1691669
                final Bindings bindings = engine.createBindings();

                bindings.put("logger", LOGGER);
                bindings.put("url", url);
                bindings.put("responseHeaders", context.getAttribute("responseHeaders", emptyMap()));
                bindings.put("responseBody", buffer.toString());

                try (final BufferedReader script = Files.newBufferedReader(Paths.get(config.getScriptFile()))) {
                    LOGGER.debug("Executing script on API response for {}", url);
                    engine.eval(script, bindings);

                    // the body may have been mutated
                    final IApimanBuffer responseBuffer = bufferFactory.createBuffer();
                    final String responseBody = (String) bindings.get("responseBody");
                    if (nonNull(responseBody) && !responseBody.isEmpty()) {
                        responseBuffer.append(responseBody);
                    }
                    super.write(responseBuffer);

                } catch (Exception e) {
                    LOGGER.error("Error executing script on API response for {}", url, e);
                }

                super.end();
            }
        };
    }
}

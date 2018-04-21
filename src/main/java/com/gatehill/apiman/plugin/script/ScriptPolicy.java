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
import io.apiman.gateway.engine.policy.IPolicyContext;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Delegates response processing to a script.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class ScriptPolicy extends AbstractMappedDataPolicy<ScriptPolicyConfig> {
    @Override
    protected Class<ScriptPolicyConfig> getConfigurationClass() {
        return ScriptPolicyConfig.class;
    }

    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request, IPolicyContext context, ScriptPolicyConfig config) {
        return null;
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
                final ScriptEngineManager manager = new ScriptEngineManager();
                final ScriptEngine engine = manager.getEngineByName("javascript");

                final String responseBody = buffer.toString();
                engine.put("responseBody", responseBody);

                try (final BufferedReader script = Files.newBufferedReader(Paths.get(config.getScriptFile()))) {
                    engine.eval(script);

                    final String updatedResponseBody = (String) engine.get("responseBody");
                    super.write(bufferFactory.createBuffer(updatedResponseBody));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                super.end();
            }
        };
    }
}

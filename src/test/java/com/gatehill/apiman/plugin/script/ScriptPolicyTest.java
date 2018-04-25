package com.gatehill.apiman.plugin.script;

import io.apiman.test.policies.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for {@link ScriptPolicy}.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(ScriptPolicy.class)
public class ScriptPolicyTest extends ApimanPolicyTest {
    private static final String RESOURCE = "/some/resource";

    @Test
    @Configuration(classpathConfigFile = "standard-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testModifyBody() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, RESOURCE);
        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // no content should be returned
        assertEquals("Hello world", response.body());
    }

    @Test
    @Configuration(classpathConfigFile = "no-capture-body.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testNoCaptureBody() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, RESOURCE);
        final PolicyTestResponse response = send(request);
        assertEquals(500, response.code());

        // no content should be returned
        assertNotEquals("Hello world", response.body());
    }
}

package com.gatehill.apiman.plugin.script;

import io.apiman.test.policies.*;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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
    public void testLoginSuccess() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, RESOURCE);
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // no content should be returned
        assertTrue(response.body().isEmpty());
    }
}

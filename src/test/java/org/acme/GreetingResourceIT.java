package org.acme;

/*
 * Runs the same endpoint tests as GreetingResourceTest against packaged artifacts.
 */

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class GreetingResourceIT extends GreetingResourceTest {
    // Execute the same tests but in packaged mode.
}

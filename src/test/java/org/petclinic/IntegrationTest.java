package org.petclinic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.petclinic.config.AsyncSyncConfiguration;
import org.petclinic.config.EmbeddedElasticsearch;
import org.petclinic.config.EmbeddedKafka;
import org.petclinic.config.EmbeddedRedis;
import org.petclinic.config.EmbeddedSQL;
import org.petclinic.config.JacksonConfiguration;
import org.petclinic.config.TestSecurityConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { PetclinicManagementApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
@EmbeddedKafka
public @interface IntegrationTest {
}

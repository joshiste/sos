/*
 * Copyright 2017 the original author or authors.
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
package example.sos.rest.inventory.integration;

import example.sos.rest.events.client.EnableEventClient;
import example.sos.rest.events.client.EventClientConfigurer;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.hypermedia.DiscoveredResource;
import org.springframework.cloud.client.hypermedia.RemoteResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @author Oliver Gierke
 */
@Configuration
@EnableScheduling
@EnableEventClient
class IntegrationConfiguration {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	EventClientConfigurer clientConfigurer() {

		return client -> {
			client.add(CatalogIntegration.PRODUCTS_ADDED, catalogEventsResource());
			client.add(OrdersIntegration.ORDER_COMPLETED, orderEventsResource());
		};
	}

	@Bean
	RemoteResource catalogEventsResource() {

		ServiceInstance service = new DefaultServiceInstance("catalog", "localhost", 7070, false);

		return new DiscoveredResource(() -> service, traverson -> traverson.follow("events"));
	}

	@Bean
	RemoteResource orderEventsResource() {

		ServiceInstance service = new DefaultServiceInstance("orders", "localhost", 7072, false);

		return new DiscoveredResource(() -> service, traverson -> traverson.follow("events"));
	}
}

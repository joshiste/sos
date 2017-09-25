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

import example.sos.rest.events.client.EventClient;
import example.sos.rest.events.client.Integration;
import example.sos.rest.inventory.Inventory;
import example.sos.rest.inventory.InventoryItem;
import example.sos.rest.inventory.InventoryItem.ProductId;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences.ResourcesType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Oliver Gierke
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CatalogIntegration {

	static final ResourcesType<Resource<ProductAdded>> PRODUCTS_ADDED = new ResourcesType<Resource<ProductAdded>>() {};

	private final EventClient events;
	private final Inventory inventory;

	@Scheduled(fixedDelay = 5000)
	public void updateProducts() {

		log.info("Catalog integration update triggered…");

		events.doWithIntegration(PRODUCTS_ADDED, (productAdded, repository) -> {

			log.info("Processing {} new events…", productAdded.getContent().size());

			productAdded.forEach(resource -> {

				Integration integration = repository.apply(() -> initInventory(resource),
						it -> it.withLastUpdate(resource.getContent().getPublicationDate()));

				log.info("Successful catalog update. New reference time: {}.",
						integration.getLastUpdate().map(it -> it.format(DateTimeFormatter.ISO_DATE_TIME)) //
								.orElseThrow(() -> new IllegalStateException()));
			});
		});
	}

	private void initInventory(Resource<ProductAdded> resource) {

		ProductId productId = ProductId.of(resource.getLink("product").getHref());

		log.info("Creating inventory item for product {}.", resource.getContent().getProduct().getDescription());

		inventory.findByProductId(productId) //
				.orElseGet(() -> inventory.save(InventoryItem.of(productId, 0)));
	}

	@Data
	public static class ProductAdded {

		Product product;
		LocalDateTime publicationDate;

		@Data
		public static class Product {

			String description;
			BigDecimal price;
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import java.util.*;

import com.univocity.api.config.builders.*;
import com.univocity.api.engine.*;

public class MigrateWorldCities extends EtlProcess {

	public MigrateWorldCities(String database) {
		super("MigrateCities", database);
	}

	@Override
	protected void configureMappings() {
		DataStoreMapping mapping = engine.map("csv", "database");

		EntityMapping regionMapping = mapping.map("region_codes", "region");
		regionMapping.identity().associate("country", "region_code").toGeneratedId("id").readingWith("toUpperCase");
		regionMapping.value().copy("country", "region_name").to("country", "name");
		regionMapping.persistence().usingMetadata().deleteAll().insertNewRows();

		EntityMapping cityMapping = mapping.map("worldcitiespop", "city");
		cityMapping.identity().associate("country", "city", "region").toGeneratedId("id");
		cityMapping.reference().using("country", "region").referTo("region_codes", "region").on("region_id").readingWith("toUpperCase").onMismatch().discard();

		cityMapping.value().copy("accentCity").to("name");
		cityMapping.autodetectMappings(); //population, latitude and longitude have the same names on both source and destination

		cityMapping.persistence().usingMetadata().deleteAll().insertNewRows();

		cityMapping.addInputRowReader(new RowReader() {

			int COUNTRY;
			int CITY;
			int REGION;

			Set<String> entries = new HashSet<String>();
			StringBuilder entryBuilder = new StringBuilder();

			@Override
			public void initialize(RowMappingContext context) {
				COUNTRY = context.getInputIndex("country");
				CITY = context.getInputIndex("city");
				REGION = context.getInputIndex("region");
			}

			@Override
			public void processRow(Object[] inputRow, Object[] outputRow, RowMappingContext context) {

				String entry = createEntry(inputRow);

				if (entry != null) {
					if (entries.contains(entry)) {
						context.discardRow();
					} else {
						entries.add(entry);
					}
				} else {
					context.discardRow();
				}
			}

			private String createEntry(Object[] inputRow) {
				String country = (String) inputRow[COUNTRY];
				String city = (String) inputRow[CITY];
				String region = (String) inputRow[REGION];

				if (country == null || city == null || region == null) {
					return null;
				}

				entryBuilder.setLength(0);
				entryBuilder.append(country).append('|');
				entryBuilder.append(city).append('|');
				entryBuilder.append(region);

				return entryBuilder.toString().toLowerCase();
			}

			@Override
			public void cleanup(RowMappingContext context) {
				entries.clear();
			}
		});
	}

	public static void main(String... args) {
		MigrateWorldCities cityImport = new MigrateWorldCities("postgres");
		try {
			//initial load with license: ??
			//Processed 3170238 rows with 3170238 insertions and 0 updates. Time taken: 192919 ms. Throughput 16000 rows/s rows/s.
			//initial load free: ??

			//sync with license: ??
			//Processed 3170238 rows with 0 insertions and 0 updates. Time taken: 673111 ms. Throughput 4000 rows/s rows/s.
			//sync free: ??

			cityImport.execute();
		} finally {
			cityImport.shutdown();
		}
	}
}

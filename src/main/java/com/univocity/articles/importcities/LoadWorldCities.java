/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import com.univocity.api.config.builders.*;

public class LoadWorldCities extends EtlProcess {

	public LoadWorldCities(String database) {
		super("LoadWorldCities", database);
	}

	@Override
	protected void configureMappings() {

		DataStoreMapping mapping = engine.map("csv", "database");
		EntityMapping cityMapping = mapping.map("worldcitiespop", "worldcities");

		cityMapping.identity().associate("country", "city", "region").to("country", "city_ascii", "region");

		cityMapping.value().copy("accentCity").to("city");
		cityMapping.autodetectMappings(); //population, latitude and longitude have the same names on both source and destination

		cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();
	}

	public static void main(String... args) {
		LoadWorldCities loadCities = new LoadWorldCities("OracleXE");
		try {
			//with license:
			//Processed 3173958 rows with 3173958 insertions and 0 updates. Time taken: 82136 ms. Throughput 38000 rows/s.

			//free: ~8 minutes to complete
			//Processed 3173958 rows with 3173958 insertions and 0 updates. Time taken: 565371 ms. Throughput 5614 rows/s.

			loadCities.execute();
		} finally {
			loadCities.shutdown();
		}
	}
}

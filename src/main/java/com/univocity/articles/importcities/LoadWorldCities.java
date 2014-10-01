/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import com.univocity.api.config.builders.*;
import com.univocity.api.engine.*;

/**
 * An {@link EtlProcess} that simply loads the worldcitiespop.txt file into the worldcities table,
 * which will be created in the database you configured in /src/main/resources/connection.properties.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 * @see EtlProcess
 *
 */
public class LoadWorldCities extends EtlProcess {

	/**
	 * Creates a process to load the world cities using using a {@link DataIntegrationEngine} named "LoadWorldCities"
	 */
	public LoadWorldCities() {
		super("LoadWorldCities");
	}

	@Override
	protected void configureMappings() {
		//Creates a mapping between data stores "csv" and "database"
		DataStoreMapping mapping = engine.map("csv", "database");

		//Creates a mapping between the entities available in the data stores: worldcitiespop (from "csv") and worldcities (from "database")
		EntityMapping cityMapping = mapping.map("worldcitiespop", "worldcities");

		//All mappings require an identity. The worldcitiespop file does not have a single field that works as an identity, so we define
		//a composite identity. In the destination, the worldcities table has an "ID" column, but in this process we are not interested
		//in doing anything with it. We just give the column names to where values should be copied.
		cityMapping.identity().associate("country", "city", "region").to("country", "city_ascii", "region");

		//copies the value from "accentCitity" (in the csv file) to "city" (in the database table)
		cityMapping.value().copy("accentCity").to("city");

		//population, latitude and longitude have the same names on both source and destination
		cityMapping.autodetectMappings();

		//As we are just loading the worldcities table, no metadata is required. We can just delete all rows (if any) and insert all rows from the file.
		cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();

		//And that's all.
	}

	/**
	 * Run this class to load the worldcitiespop.txt file into your database.
	 *
	 * The time this process takes to complete depends on your hardware and license you are using.
	 * On my laptop, an Intel i5-3337U @ 1.8 GHz, with 4 GB of RAM and a 128GB SSD drive, running Linux with MySQL, I got:
	 *
	 * (a) With an evaluation license:
	 * 	Processed 3173958 rows with 3173958 insertions and 0 updates. Time taken: 64.094 seconds. Throughput 49520.4 rows/s
	 *
	 * (b) With no license:
	 *  Processed 3173958 rows with 3173958 insertions and 0 updates. Time taken: 8.069 minutes. Throughput 6555.5 rows/s.
	 *
	 *  The process won't need more than 300 MB of RAM to execute, so you probably won't need to provide the Xmx and Xms arguments to the JVM here.
	 */
	@SuppressWarnings("javadoc")
	public static void main(String... args) {
		LoadWorldCities loadCities = new LoadWorldCities();
		try {
			loadCities.execute();
		} finally {
			loadCities.shutdown();
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import java.util.*;

import com.univocity.api.config.builders.*;
import com.univocity.api.engine.*;

/**
 * An {@link EtlProcess} that migrates the worldcitiespop.txt and region_codex.csv files into a the database tables "city" and "region".
 * which will be created in the database you configured in /src/main/resources/connection.properties.
 *
 * In this process, we want to create rows in the region table and associate the corresponding ID that is generated upon insertion to
 * the city records. City has a foreign key reference to the ID of region. This association does not exist in the original files, so we
 * need uniVocity to manage the references.
 *
 * The worldcitiespop.txt file is also inconsistent. There are many duplicate and cities and missing information.
 * This process will ignore duplicate rows and rows that are incomplete.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 * @see EtlProcess
 *
 */
public class MigrateWorldCities extends EtlProcess {

	/**
	 * Creates a process to load the world cities using using a {@link DataIntegrationEngine} named "MigrateCities"
	 */
	public MigrateWorldCities() {
		super("MigrateCities");
	}

	@Override
	protected void configureMappings() {
		//Creates a mapping between data stores "csv" and "database"
		DataStoreMapping mapping = engine.map("csv", "database");

		//Creates a mapping between the entities available in the data stores: region_codes (from "csv") and region (from "database")
		EntityMapping regionMapping = mapping.map("region_codes", "region");

		//Each record in the region_codes file can be identified by its "country" and "region", so we associate them to
		//the "id" generated at the destination. We also convert values in "country" and "region_code" to uppercase, in
		//order to match the case of the values in the worldcitiespop.txt file later on.
		regionMapping.identity().associate("country", "region_code").toGeneratedId("id").readingWith("toUpperCase");

		//Here we map "country" and "region_name" from the "region_codes" file to the "country" and "name" columns in the database table "region".
		regionMapping.value().copy("country", "region_name").to("country", "name");

		//Let's just delete everything in this table and insert everything in the "region_codes" file. Metadata will be generated here to
		//enable uniVocity's reference management.
		regionMapping.persistence().usingMetadata().deleteAll().insertNewRows();

		//Here we map the cities: worldcitiespop (from "csv") to city (from "database")
		EntityMapping cityMapping = mapping.map("worldcitiespop", "city");

		//Here we just select the columns in the worldcitiespop file that can identify each record uniquely, and associate their values
		//to the IDs generated upon insertion into "city"
		cityMapping.identity().associate("country", "city", "region").toGeneratedId("id");

		//This reference mapping tells uniVocity to use the values in "country" and "region" from the worldcitiespop file, and use them to retrieve the IDs generated
		//on the mapping from the "region_codes" file to the "region" table, and set that ID into the "region_id" column. "country" and "region" will be converted
		//to uppercase as to match the case of values in the identity of regionMapping. Finally, if a reference cannot be found, the record will be discarded,
		//i.e. only those records in "worldcitiespop.txt" that are associated to a region in "region_codes.csv" will be inserted.
		cityMapping.reference().using("country", "region").referTo("region_codes", "region").on("region_id").readingWith("toUpperCase").onMismatch().discard();

		//Simply copy the name (with proper accentuation) to the "name" column in the city table.
		cityMapping.value().copy("accentCity").to("name");

		//population, latitude and longitude have the same names on both source and destination
		cityMapping.autodetectMappings();

		//We don't need metadata here as the IDs generated for the city table won't be used elsewhere.
		cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();

		//finally, we attach a RowReader to the input file to discard any duplicate/incomplete records in the worldcititespop file.
		cityMapping.addInputRowReader(new RowReader() {

			//we will keep the indexes of fiels country, city and region in these variables.
			int COUNTRY;
			int CITY;
			int REGION;

			//Let's store all entries in this set
			Set<String> entries = new HashSet<String>();
			StringBuilder entryBuilder = new StringBuilder();

			@Override
			public void initialize(RowMappingContext context) {
				//before processing the first row, we have a chance to get the indexes of each
				//input field we are interesed in
				COUNTRY = context.getInputIndex("country");
				CITY = context.getInputIndex("city");
				REGION = context.getInputIndex("region");
			}

			@Override
			public void processRow(Object[] inputRow, Object[] outputRow, RowMappingContext context) {
				//let's create an entry with the input data
				String entry = createEntry(inputRow);

				//if the entry is not null, we check whether it has been processed already
				if (entry != null) {
					if (entries.contains(entry)) {
						//if it has, then we discard the row
						context.discardRow();
					} else {
						//if it hasn't then we store the entry so any other duplicate entry will be discarded
						entries.add(entry);
					}
				} else {
					//if the information in the row is incomplete, we discard the row.
					context.discardRow();
				}
			}

			private String createEntry(Object[] inputRow) {
				//Let's get the values of the elements we are interested in
				String country = (String) inputRow[COUNTRY];
				String city = (String) inputRow[CITY];
				String region = (String) inputRow[REGION];

				//If any of this information is null, then the row will be discarded
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
				//After the process has been completed, we can discard all entries.
				entries.clear();
			}
		});
	}

	/**
	 * Run this class to migrate the worldcitiespop.txt and region_codes.csv files into your database.
	 *
	 * The time this process takes to complete depends on your hardware and license you are using.
	 * On my laptop, an Intel i5-3337U @ 1.8 GHz, with 4 GB of RAM and a 128GB SSD drive, running Linux with MySQL, I got:
	 *
	 * (a) With an evaluation license, it took around 3 minutes to complete:
	 *  Processed 3170238 rows with 3170238 insertions and 0 updates. Time taken: 192919 ms. Throughput 16000 rows/s.
	 *
	 * (b) With no license, it took around 11 minutes to complete:
	 *  Processed 3170238 rows with 0 insertions and 0 updates. Time taken: 673111 ms. Throughput 4000 rows/s.
	 *
	 */
	public static void main(String... args) {
		MigrateWorldCities migrateCities = new MigrateWorldCities();
		try {
			migrateCities.execute();
		} finally {
			migrateCities.shutdown();
		}
	}
}

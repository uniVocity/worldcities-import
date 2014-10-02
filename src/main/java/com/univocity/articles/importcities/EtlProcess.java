/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import javax.sql.*;

import com.univocity.api.*;
import com.univocity.api.config.*;
import com.univocity.api.config.builders.*;
import com.univocity.api.engine.*;
import com.univocity.api.entity.custom.*;
import com.univocity.api.entity.jdbc.*;
import com.univocity.api.entity.text.csv.*;
import com.univocity.articles.importcities.databases.*;

/**
 * This abstract class configures and initializes a data integration engine with:
 *
 * <ul>
 * 	<li>A data store of name "csv", with the world cities files provided by Maxmind.
 * 		<ul>
			<li><a href="http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz">worldcitiespop.txt</a> - with all cities in the world</li>
 * 			<li><a href="http://geolite.maxmind.com/download/geoip/misc/region_codes.csv">region_codes.csv</a> - with the names of states/regions of each city</li>
 * 		</ul>
 *  </li>
 *  <li>
 *  	A data store of name "database", created using the scripts under src/main/resources/database.
 *  	The connections to the database are configured in the <i>connection.properties</i> file.
 *  </li>
 *  <li>
 *  	Configuration for the storage of metadata, which is used to enable more powerful mapping operations and to automatically detect data changes.
 *  </li>
 *  <li>
 *  	An initial batch size of 10,000 rows per batch. Notice this is affected by the license type: batch operations will not be enabled with the free, non-commercial license.
 *  	To obtain a free 30-day trial license, simply execute {@link com.univocity.LicenseRequestWizard} and send the license request to licenses@univocity.com
 *  	More information about licenses <a href="http://www.univocity.com/pages/license-request">here</a>
 *  </li>
 * </ul>
 *
 * Subclasses have access to an instance of {@link DataIntegrationEngine} and can define mappings between entities of the "csv" and "database" data stores.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public abstract class EtlProcess {

	private final Database database;
	private final Database metadataDatabase;

	/**
	 * An engine properly initialized. Subclasses are expected to use it to configure and execute actual mappings.
	 */
	protected final DataIntegrationEngine engine;

	private int batchSize = 10000;
	private final String engineName;

	/**
	 * Creates an ETL process, which will create a {@link DataIntegrationEngine} with the name given by parameter.
	 * @param engineName the name of the {@link DataIntegrationEngine} used by this ETL process.
	 */
	public EtlProcess(String engineName) {

		this.engineName = engineName;
		this.database = DatabaseFactory.getInstance().getDestinationDatabase();
		this.metadataDatabase = DatabaseFactory.getInstance().getMetadataDatabase();

		System.out.println("Starting " + getClass().getName() + " with " + database.getDatabaseName() + " and " + metadataDatabase.getDatabaseName());

		DataStoreConfiguration databaseConfig = createDatabaseConfiguration();
		DataStoreConfiguration fileConfig = createFileConfiguration();
		MetadataSettings metadataConfig = createMetadataConfiguration();

		EngineConfiguration config = new EngineConfiguration(engineName, databaseConfig, fileConfig);
		config.setMetadataSettings(metadataConfig);

		Univocity.registerEngine(config);
		engine = Univocity.getEngine(engineName);

		configureMappings();
	}

	/**
	 * Creates a {@link JdbcDataStoreConfiguration} configuration object with the appropriate settings
	 * for the underlying database.
	 *
	 * @return the configuration for the "database" data store.
	 */
	public DataStoreConfiguration createDatabaseConfiguration() {
		//Gets a javax.sql.DataSource instance from the database object.
		DataSource dataSource = database.getDataSource();

		//Creates a the configuration of a data store named "database", with the given javax.sql.DataSource
		JdbcDataStoreConfiguration config = new JdbcDataStoreConfiguration("database", dataSource);

		//when reading from tables of this database, never load more than the given number of rows at once.
		//uniVocity will block any reading process until there's room for more rows.
		config.setLimitOfRowsLoadedInMemory(batchSize);

		//By setting configuration options using the defaultEntityConfiguration, all tables will be configured to use
		//these settings by default, unless you explicitly provide alternative configurations for each one.		
		JdbcEntityConfiguration defaultConfig = config.getDefaultEntityConfiguration();
		
		//configures the batch size when inserting/updating/deleting from any table in the database.
		defaultConfig.setBatchSize(batchSize);

		//configures how generated keys should be extracted after insert operations.
		//Here we are configuring the insert operations to execute in batch, and return all generated keys at once.
		//Notice that not every JDBC driver suppports that, and you may need to change this configuration to match your specific needs.
		defaultConfig.retrieveGeneratedKeysUsingStatement(true);

		//applies any additional configuration that is database-dependent. Refer the implementation under package *com.univocity.articles.importcities.databases*
		database.applyDatabaseSpecificConfiguration(config);

		return config;
	}

	/**
	 * Creates a {@link CsvDataStoreConfiguration} configuration object with the appropriate settings
	 * to read data from the input files in /src/main/resources/files/
	 *
	 * @return the configuration for the "csv" data store.
	 */
	public DataStoreConfiguration createFileConfiguration() {
		CsvDataStoreConfiguration csv = new CsvDataStoreConfiguration("csv");
		csv.setLimitOfRowsLoadedInMemory(batchSize);
		csv.addEntities("files", "ISO-8859-1");

		CsvEntityConfiguration regionCodesConfig = csv.getEntityConfiguration("region_codes");
		regionCodesConfig.setHeaders("country", "region_code", "region_name");
		regionCodesConfig.setHeaderExtractionEnabled(false);
		return csv;
	}

	/**
	 * Creates a {@link MetadataSettings} configuration object defining how uniVocity
	 * should store metadata generated in mappings where {@link PersistenceSetup#usingMetadata()} has been used.
	 *
	 * The database configured for metadata storage in /src/main/resources/connection.properties will be used.
	 *
	 * This is configuration is option and if not provided uniVocity will create its own in-memory database.
	 *
	 * @return the configuration for uniVocity's metadata storage.
	 */
	public MetadataSettings createMetadataConfiguration() {
		MetadataSettings metadata = new MetadataSettings(metadataDatabase.getDataSource());
		metadata.setMetadataTableName("metadata");
		metadata.setTemporaryTableName("metadata_tmp");
		metadata.setBatchSize(batchSize);
		metadata.setFetchSize(batchSize);
		metadata.setTransactionIsolationLevel(java.sql.Connection.TRANSACTION_READ_COMMITTED);
		return metadata;
	}

	/**
	 * Shuts down the {@link #engine} used by this ETL process
	 */
	public void shutdown() {
		Univocity.shutdown(engineName);
	}

	/**
	 * Executes a data mapping cycle. The actual data mappings are defined by subclasses
	 * in the {@link #configureMappings()} method.
	 */
	public void execute() {
		engine.executeCycle();
	}

	/**
	 * Subclasses have access to the {@link #engine} object, which they should use to configure
	 * mappings from source to destination.
	 */
	protected abstract void configureMappings();
}

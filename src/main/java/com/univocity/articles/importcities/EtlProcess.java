/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import java.math.*;

import javax.sql.*;

import com.univocity.api.*;
import com.univocity.api.config.*;
import com.univocity.api.engine.*;
import com.univocity.api.entity.custom.*;
import com.univocity.api.entity.jdbc.*;
import com.univocity.api.entity.text.csv.*;
import com.univocity.articles.importcities.databases.*;

public abstract class EtlProcess {

	protected final Database database;
	protected final DataIntegrationEngine engine;

	protected int batchSize = 10000;
	private final String engineName;

	//http://geolite.maxmind.com/download/geoip/misc/region_codes.csv
	//http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz
	public EtlProcess(String engineName, String database) {

		this.engineName = engineName;
		this.database = DatabaseFactory.getDatabase(database);

		DataStoreConfiguration databaseConfig = createDatabaseConfiguration();
		DataStoreConfiguration fileConfig = createFileConfiguration();
		MetadataSettings metadataConfig = createMetadataConfiguration();

		EngineConfiguration config = new EngineConfiguration(engineName, databaseConfig, fileConfig);
		config.setMetadataSettings(metadataConfig);

		Univocity.registerEngine(config);
		engine = Univocity.getEngine(engineName);

		addFunctions();
		configureMappings();
	}

	private void addFunctions() {
		engine.addFunction(EngineScope.STATELESS, "toUpperCase", new FunctionCall<String, String>() {
			@Override
			public String execute(String input) {
				return input == null ? null : input.toUpperCase();
			}
		});

		engine.addFunction(EngineScope.STATELESS, "toBigDecimal", new FunctionCall<BigDecimal, String>() {
			@Override
			public BigDecimal execute(String input) {
				return input == null ? null : new BigDecimal(input);
			}
		});

		engine.addFunction(EngineScope.STATELESS, "toInteger", new FunctionCall<Integer, String>() {
			@Override
			public Integer execute(String input) {
				return input == null ? 0 : Integer.parseInt(input);
			}
		});
	}

	private DataStoreConfiguration createDatabaseConfiguration() {
		DataSource dataSource = database.getDataSource();

		JdbcDataStoreConfiguration config = new JdbcDataStoreConfiguration("database", dataSource);
		config.setLimitOfRowsLoadedInMemory(batchSize);
		config.getDefaultEntityConfiguration().setBatchSize(batchSize);
		config.getDefaultEntityConfiguration().retrieveGeneratedKeysUsingStatement(true);

		database.applyDatabaseSpecificConfiguration(config);

		return config;
	}

	private DataStoreConfiguration createFileConfiguration() {
		CsvDataStoreConfiguration csv = new CsvDataStoreConfiguration("csv");
		csv.setLimitOfRowsLoadedInMemory(batchSize);
		csv.addEntities("files", "ISO-8859-1");

		csv.getEntityConfiguration("region_codes").setHeaders("country", "region_code", "region_name");
		csv.getEntityConfiguration("region_codes").setHeaderExtractionEnabled(false);
		return csv;
	}

	private MetadataSettings createMetadataConfiguration() {
		MetadataSettings metadata = new MetadataSettings(database.getDataSource());
		metadata.setMetadataTableName("metadata");
		metadata.setTemporaryTableName("metadata_tmp");
		metadata.setBatchSize(batchSize);
		metadata.setFetchSize(batchSize);
		metadata.setTransactionIsolationLevel(java.sql.Connection.TRANSACTION_READ_COMMITTED);
		return metadata;
	}

	public void shutdown() {
		Univocity.shutdown(engineName);
	}

	public void execute() {
		engine.executeCycle();
	}

	protected abstract void configureMappings();
}

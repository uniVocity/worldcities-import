package com.univocity.articles.importcities;

import java.io.*;

import javax.sql.*;

import com.univocity.api.*;
import com.univocity.api.config.*;
import com.univocity.api.engine.*;
import com.univocity.api.entity.custom.*;
import com.univocity.api.entity.jdbc.*;
import com.univocity.api.entity.text.csv.*;

public abstract class WorldCitiesEtl {

	protected final Database citiesdb;
	protected final Database metadatadb;
	protected final DataIntegrationEngine engine;

	//http://geolite.maxmind.com/download/geoip/misc/region_codes.csv
	//http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz
	public WorldCitiesEtl() {
		citiesdb = new Database("citiesdb", new File("src/main/resources/ddl/citiesdb"));
		metadatadb = new Database("metadatadb", new File("src/main/resources/ddl/metadatadb"));

		EngineConfiguration config = new EngineConfiguration(getEngineName(), createDatabaseConfiguration(), createFileConfiguration());
		config.setMetadataSettings(createMetadataConfiguration());

		Univocity.registerEngine(config);
		engine = Univocity.getEngine(getEngineName());

		configureMappings();
	}

	private DataStoreConfiguration createDatabaseConfiguration() {
		DataSource dataSource = citiesdb.getDataSource();
		JdbcDataStoreConfiguration database = new JdbcDataStoreConfiguration("database", dataSource);
		database.setLimitOfRowsLoadedInMemory(getBatchSize());
		database.getDefaultEntityConfiguration().setBatchSize(getBatchSize());
		database.getDefaultEntityConfiguration().retrieveGeneratedKeysUsingStatement(true);

		database.setIdentifierEscaper(new DefaultEscaper("`"));
		return database;
	}

	private DataStoreConfiguration createFileConfiguration() {
		CsvDataStoreConfiguration csv = new CsvDataStoreConfiguration("csv");
		csv.setLimitOfRowsLoadedInMemory(getBatchSize());
		csv.addEntities("files", "ISO-8859-1");

		csv.getEntityConfiguration("region_codes").setHeaders("country", "region_code", "region_name");
		csv.getEntityConfiguration("region_codes").setHeaderExtractionEnabled(false);
		return csv;
	}

	private MetadataSettings createMetadataConfiguration() {
		MetadataSettings metadata = new MetadataSettings(metadatadb.getDataSource());
		metadata.setMetadataTableName("metadata");
		metadata.setTemporaryTableName("tmp");
		metadata.setBatchSize(getBatchSize());
		metadata.setFetchSize(getBatchSize());
		return metadata;
	}

	public void shutdown() {
		Univocity.shutdown(getEngineName());
	}

	protected abstract int getBatchSize();

	protected abstract String getEngineName();

	protected abstract void configureMappings();
}

/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import javax.sql.*;

import org.springframework.jdbc.datasource.*;

import com.univocity.api.entity.jdbc.*;

class PostgresDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "Postgres";
	}

	@Override
	DataSource createDataSource() {
		return new SingleConnectionDataSource("jdbc:postgresql://localhost/testdb", "jbax", "", true);
	}

	@Override
	String getDriverClassName() {
		return "org.postgresql.Driver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		jdbcDataStoreConfig.getDefaultEntityConfiguration().setParameterConversionEnabled(true);
	}
}

/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import javax.sql.*;

import org.springframework.jdbc.datasource.*;

import com.univocity.api.entity.jdbc.*;

class OracleXEDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "OracleXE";
	}

	@Override
	DataSource createDataSource() {
		return new SingleConnectionDataSource("jdbc:oracle:thin:@localhost:1521:XE", "jbax", "qwerty", true);
	}

	@Override
	String getDriverClassName() {
		return "oracle.jdbc.driver.OracleDriver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {

		jdbcDataStoreConfig.setTransactionIsolationLevel(java.sql.Connection.TRANSACTION_READ_COMMITTED);

		jdbcDataStoreConfig.getEntityConfiguration("region").configureField("id").setGenerated(true);
		jdbcDataStoreConfig.getEntityConfiguration("city").configureField("id").setGenerated(true);
		jdbcDataStoreConfig.getEntityConfiguration("worldcities").configureField("id").setGenerated(true);
	}
}

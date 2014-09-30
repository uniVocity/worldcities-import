/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import javax.sql.*;

import org.springframework.jdbc.datasource.*;

import com.univocity.api.entity.jdbc.*;

class MySqlDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "MySql";
	}

	@Override
	DataSource createDataSource() {
		return new SingleConnectionDataSource("jdbc:mysql://localhost:3306/testdb?useServerPrepStmts=false&rewriteBatchedStatements=true", "root", "", true);
	}

	@Override
	String getDriverClassName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		jdbcDataStoreConfig.setIdentifierEscaper(new DefaultEscaper("`"));
	}
}

package com.univocity.articles.importcities.databases;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for Microsoft SQL Server
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public class SqlServerDatabase extends Database{

	@Override
	public String getDatabaseName() {
		return "SqlServer";
	}

	@Override
	String getDriverClassName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		//no specific configuration required.
	}

}

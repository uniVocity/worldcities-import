package com.univocity.articles.importcities.databases;

import org.springframework.jdbc.core.*;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for HSQLDB
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
class HsqlDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "HSQLDB";
	}

	@Override
	String getDriverClassName() {
		return "org.hsqldb.jdbcDriver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		/*
		 * Shuts down HSQLDB after executing the data migration processes. 
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				new JdbcTemplate(getDataSource()).execute("SHUTDOWN");
			}
		});
	}
}

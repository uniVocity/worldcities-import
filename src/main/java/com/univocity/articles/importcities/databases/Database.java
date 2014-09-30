/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import java.io.*;
import java.util.*;

import javax.sql.*;

import org.springframework.jdbc.core.*;

import com.univocity.api.entity.jdbc.*;

public abstract class Database {

	private JdbcTemplate jdbcTemplate;

	Database() {
	}

	void initialize() {
		try {
			Class.forName(getDriverClassName());
			DataSource dataSource = createDataSource();
			this.jdbcTemplate = new JdbcTemplate(dataSource);

		} catch (Exception ex) {
			throw new IllegalStateException("Error creating database using scripts for database " + getDatabaseName(), ex);
		}

		createTables();
	}

	public abstract String getDatabaseName();

	abstract DataSource createDataSource();

	abstract String getDriverClassName();

	private void createTables() {
		File dirWithCreateTableScripts = new File("src/main/resources/database/" + getDatabaseName().toLowerCase());
		Map<String, String> scripts = new HashMap<String, String>();
		for (File scriptFile : dirWithCreateTableScripts.listFiles()) {
			String name = scriptFile.getName();
			String script = readFile(scriptFile);
			scripts.put(name.toLowerCase(), script);
		}

		createTables(scripts);
	}

	private void createTables(Map<String, String> scripts) {
		String scriptOrder = "worldcities,region,city,metadata,metadata_tmp";
		String[] order = scriptOrder.split(",");

		boolean tablesCreated = false;
		for (String tableName : order) {
			String createTableScript = scripts.get(tableName + ".tbl");

			try {
				jdbcTemplate.execute("select count(*) from " + tableName);
			} catch (Exception ex) {
				jdbcTemplate.execute(createTableScript);
				tablesCreated = true;
			}
		}

		String sequences = scripts.get("sequences.sql");
		if (tablesCreated && sequences != null) {
			for (String script : sequences.split("\\n")) {
				jdbcTemplate.execute(script);
			}
		}
	}

	private String readFile(File file) {
		StringBuilder out = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				out.append(str).append('\n');
			}
			in.close();
		} catch (IOException e) {
			throw new IllegalStateException("Error reading file " + file.getAbsolutePath(), e);
		}
		return out.toString();
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public abstract void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig);
}

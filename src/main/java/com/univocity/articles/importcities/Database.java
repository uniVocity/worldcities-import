/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import java.io.*;
import java.util.*;

import javax.sql.*;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.*;

class Database {

	private final JdbcTemplate jdbcTemplate;
	private final Set<String> tableNames = new TreeSet<String>();

	public Database(DataSource dataSource, File dirWithCreateTableScripts) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		createTables(dirWithCreateTableScripts);
	}

	public Database(String databaseName, File dirWithCreateTableScripts) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			DataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost:3306/testdb?useServerPrepStmts=false&rewriteBatchedStatements=true", "root", "", true);
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		} catch (Exception ex) {
			throw new IllegalStateException("Error creating database " + databaseName, ex);
		}

		try {
			createTables(dirWithCreateTableScripts);
		} catch (Exception ex) {
			//exceptions will be thrown here if the tables already exist in the database. Just ignore them.
			ex.printStackTrace();
		}
	}

	private void createTables(File dirWithCreateTableScripts) {
		Map<String, String> scripts = new HashMap<String, String>();
		for (File scriptFile : dirWithCreateTableScripts.listFiles()) {
			if (scriptFile.isDirectory()) {
				continue;
			}
			String name = scriptFile.getName();
			String script = readFile(scriptFile);
			scripts.put(name.toLowerCase(), script);
		}

		createTables(scripts);
	}

	private void createTables(Map<String, String> scripts) {
		String scriptOrder = scripts.get("script_order");
		String[] order = scriptOrder.split(",");

		for (String tableName : order) {
			tableName = tableName.trim().toLowerCase();
			tableNames.add(tableName);

			String scriptName = tableName + ".tbl";
			String createTableScript = scripts.get(scriptName);
			try {
				jdbcTemplate.execute(createTableScript);
			} catch (Exception e) {
				throw new IllegalArgumentException("Error creating table with script " + scriptName, e);
			}

			scriptName = tableName + ".idx";
			String createIndexScript = scripts.get(scriptName);
			if (createIndexScript != null) {
				try {
					jdbcTemplate.execute(createIndexScript);
				} catch (Exception e) {
					throw new IllegalArgumentException("Error creating index with script " + scriptName, e);
				}
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

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public Set<String> getTableNames() {
		return Collections.unmodifiableSet(this.tableNames);
	}
}

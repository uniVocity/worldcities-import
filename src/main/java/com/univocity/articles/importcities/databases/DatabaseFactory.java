/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import java.util.*;

public class DatabaseFactory {

	private static final Map<String, Database> databases;
	static {
		databases = new TreeMap<String, Database>();

		registerDatabase(new MySqlDatabase());
		registerDatabase(new OracleXEDatabase());
		registerDatabase(new PostgresDatabase());
	}

	private static void registerDatabase(Database database) {
		databases.put(database.getDatabaseName().toLowerCase(), database);
	}

	public static Database getDatabase(String databaseName) {
		Database database = databases.get(databaseName.toLowerCase());
		if (database == null) {
			throw new IllegalArgumentException("Unknown database name: " + databaseName + ". Available databases: " + databases.keySet());
		}
		database.initialize();
		return database;
	}

	public static Set<String> getAvailableDatabases() {
		return Collections.unmodifiableSet(databases.keySet());
	}
}

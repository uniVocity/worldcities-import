package com.univocity.articles.importcities;

import com.univocity.api.config.builders.*;
import com.univocity.api.engine.*;

public class MigrateWorldCities extends WorldCitiesEtl {

	@Override
	protected void configureMappings() {
		engine.addFunction(EngineScope.STATELESS, "toUpperCase", new FunctionCall<String, String>() {
			@Override
			public String execute(String input) {
				return input == null ? null : input.toUpperCase();
			}
		});

		DataStoreMapping mapping = engine.map("csv", "database");

		EntityMapping regionMapping = mapping.map("region_codes", "region");
		regionMapping.identity().associate("country", "region_code").toGeneratedId("id").readingWith("toUpperCase");
		regionMapping.value().copy("country", "region_name").to("country", "name");
		regionMapping.persistence().usingMetadata().deleteAbsent().updateModified().insertNewRows();

		EntityMapping cityMapping = mapping.map("worldcitiespop", "city");
		cityMapping.identity().associate("country", "city", "region").toGeneratedId("id");
		cityMapping.reference().using("country", "region").referTo("region_codes", "region").on("region_id").readingWith("toUpperCase").onMismatch().ignore();
		cityMapping.value().copy("accentCity").to("name");
		cityMapping.autodetectMappings(); //longitude, latitude and population have the same names.
		cityMapping.persistence().usingMetadata().deleteAbsent().updateModified().insertNewRows();
	}

	public void synchronize() {
		engine.executeCycle(Transactions.PER_MAPPING);
	}

	@Override
	protected int getBatchSize() {
		return 100000;
	}

	@Override
	protected String getEngineName() {
		return "cityMigration";
	}

	public static void main(String... args) {
		MigrateWorldCities cityImport = new MigrateWorldCities();
		try {
			cityImport.synchronize();
		} finally {
			cityImport.shutdown();
		}
	}
}

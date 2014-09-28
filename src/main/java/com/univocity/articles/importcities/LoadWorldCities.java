package com.univocity.articles.importcities;

import com.univocity.api.config.builders.*;

public class LoadWorldCities extends WorldCitiesEtl {

	@Override
	protected void configureMappings() {

		DataStoreMapping mapping = engine.map("csv", "database");

		EntityMapping cityMapping = mapping.map("worldcitiespop", "worldcities");
		cityMapping.identity().associate("country", "city", "region").to("country", "city_ascii", "region");
		cityMapping.value().copy("accentCity").to("city");
		cityMapping.autodetectMappings(); //longitude, latitude and population have the same names.
		cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();
	}

	public void execute() {
		engine.executeCycle();
	}

	@Override
	protected int getBatchSize() {
		return 100000;
	}

	@Override
	protected String getEngineName() {
		return "loadCities";
	}

	public static void main(String... args) {
		LoadWorldCities loadCities = new LoadWorldCities();
		try {
			loadCities.execute();
		} finally {
			loadCities.shutdown();
		}
	}
}

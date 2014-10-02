# Adding more databases


To user another database, follow these instructions: 


## Add scripts to create your tables

Create a folder under [src/main/resources/database](./src/main/resources/database) with the create table scripts adjusted to use your specific database syntax.

Create a `.tbl` file per script, with the name of each table:

 * city.tbl
 * region.tbl
 * worldcities.tbl

Also create `.tbl` files for metadata tables if you are interested in persisting uniVocity's metadata in your database:

 * metadata.tbl
 * metadata_tmp.tbl

If you need to execute additional commands, create a file named `scripts.sql`, with a single command per row. These commands will be executed *after* the tables have been created, and *only if* any table has been created.

Refer to the scripts under [oraclexe](./src/main/resources/database/oraclexe) for an example.

## Create your own Database class

Create a class that extends from the abstract class [com.univocity.articles.importcities.databases.Database](./src/main/java/com/univocity/articles/importcities/databases/Database.java), and implement its abstract methods. The following example presents our own implementation for the HSQLDB datase:  
 

```java

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
```

The `getDatabaseName` must return the same name of the directory with scripts you created on the previous step. The name is case insensitive.

On the `getDriverClassName` method, you return the name of your JDBC driver class. Ensure the driver is on the classpath (either add it to the [pom.xml](./pom.xml), or include it manually.)

Finally, on the `applyDatabaseSpecificConfiguration` method, you can apply configurations specific to your database to the JdbcDataStoreConfiguration object.  Refer to the implementations for other database under the [databases package](./src/main/java/com/univocity/articles/importcities/databases).

## Register your database class to the DatabaseFactory.

Simply invoke `DatabaseFactory.getInstance().registerDatabase(YourDatabase.class)`, before executing the processes, to add your database class to the [DatabaseFactory](./src/main/java/com/univocity/articles/importcities/databases/DatabaseFactory.java). 

Alternatively, you can edit the [DatabaseFactory](./src/main/java/com/univocity/articles/importcities/databases/DatabaseFactory.java) directly and add your database class in its private constructor: 

```java

private DatabaseFactory() {
	databases = new TreeMap<String, Class<? extends Database>>();
	
	registerDatabase(YourDatabase.class);

	registerDatabase(MySqlDatabase.class);
	...
}
```  

## Edit the connection properties with your database details

Open the (connection.properties)[./src/main/resources/connection.properties] and adjust the connection details to use your database:

```

	destination.database.name=yourDatabase
	destination.database.url=jdbc:yourDatabase://host:<port>
	destination.database.user=yourUsername
	destination.database.password=yourPassword

	# if you want to store metadata in this database:

	metadata.database.name=yourDatabase
	metadata.database.url=jdbc:yourDatabase://host:<port>
	metadata.database.user=yourUsername
	metadata.database.password=yourPassword	
```

## Run the processes.

The processes will start by instantiating your database class, executing the create table scripts (if they are not in the database yet) and finally the data loading will start.

     
#### If you have any questions and suggestions don't hesitate to [e-mail us](mailto:dev@univocity.com) 
package kr.panda.bot.utils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

/**
 * The DatabaseHelper class provides utility methods for interacting with a DynamoDB database.
 */
public class DatabaseHelper {
	private static final ExecutorService sDatabaseWorker = Executors.newSingleThreadExecutor();
	private static final AmazonDynamoDB sDB = AmazonDynamoDBClientBuilder.defaultClient();
	private static final Map<String, List<String>> TABLE_COLUMN_MAP = new HashMap<>();

	static {
		TABLE_COLUMN_MAP.put("ClanList", Arrays.asList(
				new String[]{"GuildId", "ClanTag"}));
	}

	/**
	 * Creates a table in the DynamoDB database if it does not already exist.
	 *
	 * @param tableName The name of the table to create.
	 * @param keyName   The name of the primary key attribute.
	 * @param attrType  The data type of the primary key attribute.
	 */
	private static void createTable(String tableName, String keyName, ScalarAttributeType attrType) {
		try {
			sDB.describeTable(tableName);
		} catch (ResourceNotFoundException e) {
			CreateTableRequest request = new CreateTableRequest()
					.withAttributeDefinitions(new AttributeDefinition(
							keyName, attrType))
					.withKeySchema(new KeySchemaElement(keyName, KeyType.HASH))
					.withProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
					.withTableName(tableName);
			try {
				sDB.createTable(request);
			} catch (ResourceInUseException | LimitExceededException e2) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes the database by creating the necessary tables.
	 */
	public static void initialize() {
		createTable("ClanList", "ChannelId", ScalarAttributeType.S);
	}

	/**
	 * Retrieves the string value of an AttributeValue object.
	 *
	 * @param value The AttributeValue object.
	 * @return The string value of the AttributeValue object.
	 */
	private static String getAttributeValue(AttributeValue value) {
		String result = null;
		if (value.getS() != null) {
			result = value.getS();
		} else if (value.getN() != null) {
			result = value.getN();
		} else if (value.isBOOL()) {
			result = value.getBOOL() ? "1" : "0";
		}

		return result;
	}

	/**
	 * Converts a map of string values to a map of AttributeValue objects.
	 *
	 * @param map The map of string values.
	 * @return The map of AttributeValue objects.
	 */
	private static Map<String, AttributeValue> getAttributeMap(Map<String, String> map) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> new AttributeValue().withS(e.getValue())));
	}

	/**
	 * Converts a map of AttributeValue objects to a map of string values.
	 *
	 * @param map The map of AttributeValue objects.
	 * @return The map of string values.
	 */
	private static Map<String, String> getStringMap(Map<String, AttributeValue> map) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> getAttributeValue(e.getValue())));
	}

	/**
	 * Converts a map of string values to a map of AttributeValueUpdate objects for updating an item in the database.
	 *
	 * @param key The key of the item to update.
	 * @param map The map of string values.
	 * @return The map of AttributeValueUpdate objects.
	 */
	private static Map<String, AttributeValueUpdate> getAttributeUpdateMap(String key, Map<String, String> map) {
		return map.entrySet().stream()
				.filter(e -> !e.getKey().equals(key))
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> new AttributeValueUpdate()
								.withAction(AttributeAction.PUT)
								.withValue(new AttributeValue().withS(e.getValue()))));
	}

	/**
	 * Adds a new row to the specified table in the database.
	 *
	 * @param tableName The name of the table.
	 * @param map       The map of string values representing the row data.
	 * @return A Future representing the result of the operation.
	 */
	public static synchronized Future<PutItemResult> addRow(String tableName, Map<String, String> map) {
		System.out.println(MessageFormat.format("INSERT INTO {0}", tableName));
		return sDatabaseWorker.submit(() -> sDB.putItem(tableName, getAttributeMap(map)));
	}

	/**
	 * Deletes a row from the specified table in the database.
	 *
	 * @param tableName The name of the table.
	 * @param key       The key of the row to delete.
	 * @param value     The value of the key attribute.
	 * @return A Future representing the result of the operation.
	 */
	public static synchronized Future<DeleteItemResult> deleteRow(String tableName, String key, String value) {
		System.out.println(MessageFormat.format("DELETE FROM {0} WHERE {1} = ?", tableName, key));
		return sDatabaseWorker.submit(() -> sDB.deleteItem(tableName,
				Collections.singletonMap(key, new AttributeValue().withS(value)), "ALL_OLD"));
	}

	/**
	 * Retrieves a row from the specified table in the database.
	 *
	 * @param tableName The name of the table.
	 * @param key       The key of the row to retrieve.
	 * @param value     The value of the key attribute.
	 * @return A map of string values representing the retrieved row, or null if the row does not exist.
	 */
	public static synchronized Map<String, String> getRow(String tableName, String key, String value) {
		System.out.println(MessageFormat.format("SELECT FROM {0} WHERE {1} = ?", tableName, key));
		GetItemResult getResult = sDB.getItem(tableName,
				Collections.singletonMap(key, new AttributeValue().withS(value)));
		Map<String, AttributeValue> item = getResult.getItem();
		Map<String, String> result = null;
		if (item != null) {
			result = getStringMap(item);
		}

		return result;
	}

	/**
	 * Retrieves all rows from the specified table in the database.
	 *
	 * @param tableName The name of the table.
	 * @return A list of maps, where each map represents a row in the table.
	 */
	public static synchronized List<Map<String, String>> getRows(String tableName) {
		System.out.println(MessageFormat.format("SELECT FROM {0}", tableName));
		ScanResult scanResult = sDB.scan(tableName, TABLE_COLUMN_MAP.get(tableName));
		List<Map<String, String>> result = scanResult.getItems().stream()
				.map(DatabaseHelper::getStringMap)
				.collect(Collectors.toList());

		return result;
	}

	/**
	 * Updates a row in the specified table in the database.
	 *
	 * @param tableName The name of the table.
	 * @param key       The key of the row to update.
	 * @param map       The map of string values representing the updated row data.
	 * @return A Future representing the result of the operation.
	 */
	public static synchronized Future<UpdateItemResult> updateRow(String tableName, String key, Map<String, String> map) {
		System.out.println(MessageFormat.format("UPDATE {0} WHERE {1} = ?", tableName, key));
		Map<String, AttributeValue> primaryKeyMap = getAttributeMap(
				Collections.singletonMap(key, map.get(key)));
		Map<String, AttributeValueUpdate> updateMap = getAttributeUpdateMap(key, map);
		return sDatabaseWorker.submit(() -> sDB.updateItem(tableName, primaryKeyMap, updateMap));
	}
}

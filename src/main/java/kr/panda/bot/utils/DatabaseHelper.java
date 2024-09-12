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

public class DatabaseHelper {
	private static final ExecutorService sDatabaseWorker = Executors.newSingleThreadExecutor();
	private static final AmazonDynamoDB sDB = AmazonDynamoDBClientBuilder.defaultClient();
	private static final Map<String, List<String>> TABLE_COLUMN_MAP = new HashMap<>();
	static {
		TABLE_COLUMN_MAP.put("ClanList", Arrays.asList(
				new String[]{"GuildId", "ClanTag"}));
		TABLE_COLUMN_MAP.put("ClanWarNotifyChannel", Arrays.asList(
				new String[]{"Id", "ChannelId", "ClanTag"}));
	}

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

	public static void initialize() {
		createTable("ClanList", "ChannelId", ScalarAttributeType.S);
		createTable("ClanWarNotifyChannel", "Id", ScalarAttributeType.S);
	}

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

	private static Map<String, String> getStringMap(Map<String, AttributeValue> map) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> getAttributeValue(e.getValue())));
	}

	private static Map<String, AttributeValue> getAttributeMap(Map<String, String> map) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> new AttributeValue().withS(e.getValue())));
	}

	private static Map<String, AttributeValueUpdate> getAttributeUpdateMap(String key, Map<String, String> map) {
		return map.entrySet().stream()
				.filter(e -> !e.getKey().equals(key))
				.filter(entry -> entry.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> new AttributeValueUpdate()
						.withAction(AttributeAction.PUT)
						.withValue(new AttributeValue().withS(e.getValue()))));
	}

	public static synchronized Future<PutItemResult> addRow(String tableName, Map<String, String> map) {
		System.out.println(MessageFormat.format("INSERT INTO {0}", tableName));
		return sDatabaseWorker.submit(() -> sDB.putItem(tableName, getAttributeMap(map)));
	}

	public static synchronized Future<DeleteItemResult> deleteRow(String tableName, String key, String value) {
		System.out.println(MessageFormat.format("DELETE FROM {0} WHERE {1} = ?", tableName, key));
		return sDatabaseWorker.submit(() -> sDB.deleteItem(tableName,
				Collections.singletonMap(key, new AttributeValue().withS(value)), "ALL_OLD"));
	}

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

	public static synchronized List<Map<String, String>> getRows(String tableName) {
		System.out.println(MessageFormat.format("SELECT FROM {0}", tableName));
		ScanResult scanResult = sDB.scan(tableName, TABLE_COLUMN_MAP.get(tableName));
		List<Map<String, String>> result = scanResult.getItems().stream()
				.map(DatabaseHelper::getStringMap)
				.collect(Collectors.toList());

		return result;
	}

	public static synchronized Future<UpdateItemResult> updateRow(String tableName, String key, Map<String, String> map) {
		System.out.println(MessageFormat.format("UPDATE {0} WHERE {1} = ?", tableName, key));
		Map<String, AttributeValue> primaryKeyMap = getAttributeMap(
				Collections.singletonMap(key, map.get(key)));
		Map<String, AttributeValueUpdate> updateMap = getAttributeUpdateMap(key, map);
		return sDatabaseWorker.submit(() -> sDB.updateItem(tableName, primaryKeyMap, updateMap));
	}
}

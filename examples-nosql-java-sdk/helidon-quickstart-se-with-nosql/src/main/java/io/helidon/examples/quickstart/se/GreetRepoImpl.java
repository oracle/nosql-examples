package io.helidon.examples.quickstart.se;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PrepareRequest;
import oracle.nosql.driver.ops.PreparedStatement;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.QueryIterableResult;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.LongValue;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.TimestampValue;
import oracle.nosql.driver.values.StringValue;
import oracle.nosql.driver.ops.TableLimits;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import javax.json.Json;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;



public class GreetRepoImpl implements GreetRepo {
    public static final String KV_JSON = "kv_json_";
    private static final String TABLE_NAME = "GreetTableJSON";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
        + TABLE_NAME + "(id INTEGER, kv_json_ JSON, " +
        "PRIMARY KEY(id))";
    public static final String SELECT_ALL = "select * from " + TABLE_NAME + " t";
    public static final String COUNT_ALL = "select count(*) as cnt from " + TABLE_NAME;
    public static final String DELETE_ALL = "delete from " + TABLE_NAME;

    private final NoSQLHandle client;
    private final Map<String, PreparedStatement> prepMap = new HashMap<>();

    GreetRepoImpl(NoSQLHandle client) {
        this.client = client;
        createTable();
    }

    /**
     * Creates the required table if not previously created.
     */
    private void createTable() {
        TableRequest req = new TableRequest();
        req.setStatement(CREATE_TABLE)
                .setTableLimits(new TableLimits(10, 10, 10));
        client.doTableRequest(req, 20000, 1000);
    }

    public Greet save(Greet greet) {
        PutRequest req = new PutRequest();
        req.setTableName(TABLE_NAME);
        MapValue row = toMapValue(greet);
        req.setValue(row);
        //PutResult res =
        client.put(req);

        return greet;
    }

    public Greet findById(Integer greetId) {
        GetRequest req = new GetRequest();
        req.setTableName(TABLE_NAME);
        MapValue key = new MapValue()
                .put("id", greetId); 
        req.setKey(key);
        GetResult res = client.get(req);
        MapValue value = res.getValue();
        if (value == null) {
            return null;
        }

        Greet result = fromMapValue (value);
        return result;
    }

    public List<Greet> findAll() {

        Map<String, String> pageable = new HashMap<>();
        pageable.put("order", "ORDER BY id DESC");
        pageable.put("limit", "10");
        pageable.put("offset", "0");

        String where = " ";

        Map<String, FieldValue> params = new HashMap<>();
        //params.put("$param", new StringValue(".*" + name + ".*"));

        return  executeQuery(pageable, params, where);

    }


    private List<Greet> executeQuery(Map<String, String > pageable, Map<String, FieldValue> params, String where) {

        String sql = sqlBuilder(pageable, params, where);

        PreparedStatement preparedStatement = ensurePrepared(sql)
                .copyStatement();
        QueryRequest queryRequest = new QueryRequest()
                .setPreparedStatement(preparedStatement);

        params.forEach((k, v) -> preparedStatement.setVariable(k, v));

        List<Greet> listGreet = new ArrayList<Greet>();
        try (QueryIterableResult results = client.queryIterable(queryRequest)) {
                for (MapValue res : results) {
                    Greet result = fromMapValue (res);
                    listGreet.add(result);
                }
        }

        return listGreet;
    }



    private static String sqlBuilder(Map<String, String > pageable, Map<String, FieldValue> params, String where) {

        String sql = SELECT_ALL + (where == null ? "" : where);
        sql = sql + pageable.get("order");
        sql += " LIMIT $kv_limit_ OFFSET $kv_offset_";

        params.put("$kv_limit_",  new LongValue(pageable.get("limit")) );
        params.put("$kv_offset_", new LongValue(pageable.get("offset")) );

        String declare = params.entrySet()
            .stream()
            .map((entry) -> entry.getKey() + " " + entry.getValue().getType().name())
            .collect(Collectors.joining("; ", "DECLARE ", "; "));

        sql = declare + " " + sql;

        return sql;
    }


    // Following methods transform POJO key/entity to/from MapValue
    private MapValue toMapValue(Greet greet) {
       Jsonb jsonb = JsonbBuilder.create();
       String result = jsonb.toJson(greet);
       MapValue row = new MapValue()
                .put("id", greet.getId())
                .putFromJson("kv_json_", result, null);
       return row;
    }


    private Greet fromMapValue(MapValue value) {
        if (value == null) {
            return null;
        }

        Jsonb jsonb = JsonbBuilder.create();

        Greet result = new Greet();
        result = jsonb.fromJson(value.get("kv_json_").toJson(), Greet.class);
        result.setId(value.getInt("id"));
        return result;
    }


    // Cache prepared statements
    private PreparedStatement ensurePrepared(String statement) {
        PreparedStatement preparedStatement = prepMap.get(statement);
        if (preparedStatement == null) {
            synchronized (prepMap) {
                preparedStatement = client.prepare(
                    new PrepareRequest().setStatement(statement))
                    .getPreparedStatement();
                prepMap.put(statement, preparedStatement);
            }
        }
        return preparedStatement;
    }
}

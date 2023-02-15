package com.example;

import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.core.IterableUtil;
import com.oracle.nosql.spring.data.repository.support.NosqlEntityInformation;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PrepareRequest;
import oracle.nosql.driver.ops.PreparedStatement;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.LongValue;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.TimestampValue;

import oracle.nosql.driver.values.StringValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MachineRepoImpl implements MachineRepo {
    public static final String KV_JSON = "kv_json_";
    private static final String TABLE_NAME = "Machine";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
        + TABLE_NAME + "(name String, version String, kv_json_ JSON, " +
        "PRIMARY KEY(SHARD(name), version))";
    public static final String SELECT_ALL = "select * from " + TABLE_NAME + " t";
    public static final String COUNT_ALL = "select count(*) as cnt from " + TABLE_NAME;
    public static final String DELETE_ALL = "delete from " + TABLE_NAME;

    private final NoSQLHandle client;
    private final NosqlDbConfig config;
    private final Map<String, PreparedStatement> prepMap = new HashMap<>();

    MachineRepoImpl(NoSQLHandle client, NosqlDbConfig config) {
        this.client = client;
        this.config = config;
    }

    /**
     * Creates the required table if not previously created.
     */
    public void createTable() {
        TableRequest req = new TableRequest();
        req.setStatement(CREATE_TABLE)
           .setTableLimits(("PROVISIONED".equals(config.getDefaultCapacityMode().toString()))?
                           new TableLimits(config.getDefaultReadUnits(), config.getDefaultWriteUnits(), config.getDefaultStorageGB()):
                           new TableLimits(config.getDefaultStorageGB()));
        client.doTableRequest(req, config.getTableReqTimeout(),
            config.getTableReqPollInterval());
    }

    public Machine save(Machine machine) {
        PutRequest req = new PutRequest();
        req.setTableName(TABLE_NAME);
        MapValue row = toMapValue(machine);
        req.setValue(row);
        //PutResult res =
        client.put(req);

        return machine;
    }

    /**
     * Machine compKey param here is used only for the key fields: ck1, ck2, ck3.
     * Note: compKey must contain complete key.
     * @param machineId
     */
    public void deleteById(MachineId machineId) {
        DeleteRequest req = new DeleteRequest();
        req.setTableName(TABLE_NAME);
        MapValue key = keyToMapValue(machineId);
        req.setKey(key);
        client.delete(req);
    }

    /**
     * Note: compKey must contain complete key.
     * @param machine
     */
    public void delete(Machine machine) {
        if (machine != null) {
            deleteById(machine.getMachineId());
        }
    }

    @Override
    public void deleteAll() {
        PreparedStatement preparedStatement = ensurePrepared(DELETE_ALL);
        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));
        // Make sure to read at least one to execute the query
        qiRes.iterator().hasNext();
    }

    public long count() {
        PreparedStatement preparedStatement = ensurePrepared(COUNT_ALL);
        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        Iterator<MapValue> iterator = qiRes.iterator();
        if (iterator.hasNext()) {
            MapValue resMV = iterator.next();
            return resMV.get("cnt").asLong().getValue();
        }
        throw new RuntimeException("Can't find count of Machine.");
    }

    public Machine findById(MachineId machineId) {
        GetRequest req = new GetRequest();
        req.setTableName(TABLE_NAME);
        MapValue key = keyToMapValue(machineId);
        req.setKey(key);
        GetResult res = client.get(req);
        MapValue value = res.getValue();
        if (value == null) {
            return null;
        }

        Machine result = new Machine();
        result.machineId = machineId;
        // or
        //result.setMachineId(keyFromFieldValue(value));
        dataFromMapValue(value.get(KV_JSON).asMap(), result);
        return result;
    }

    public Iterable<Machine> findAll() {
        PreparedStatement preparedStatement = ensurePrepared(SELECT_ALL);
        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        return IterableUtil.getIterableFromStream(
            IterableUtil.getStreamFromIterable(qiRes)
                .map(mapValue -> fromMapValue(mapValue)));
    }

    @Override
    public Iterable<Machine> findAll(Sort sort) {
        String orderBy = orderBySql(sort);
        String sql = SELECT_ALL + orderBy;
        //System.out.println("sql: " + sql);

        PreparedStatement preparedStatement = ensurePrepared(sql);
        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        return IterableUtil.getIterableFromStream(
            IterableUtil.getStreamFromIterable(qiRes)
                .map(mapValue -> fromMapValue(mapValue)));
    }

    private static String orderBySql(Sort sort) {
        String orderBy = sort.stream()
            .map(f -> ( "t." +
                convertProperty(f.getProperty()) + " " +
                (f.isAscending() ? "ASC" : "DESC")))
            .collect(Collectors.joining(",", " ORDER BY ", ""));
        return orderBy;
    }

    private static String convertProperty(String prop) {
        if ("name".equals(prop) || "version".equals(prop)) {
            return prop;
        }

        return KV_JSON + ".\"" + prop + "\"";
    }

    @Override
    public Page<Machine> findAll(Pageable pageable) {
        Map<String, FieldValue> params = new HashMap<>();
        String sql = limitOffsetSql(pageable, params, null);
        //System.out.println("sql: " + sql);

        final PreparedStatement preparedStatement = ensurePrepared(sql)
            .copyStatement();

        params.forEach((k, v) -> preparedStatement.setVariable(k, v));

        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        List<Machine> result =
            IterableUtil.getStreamFromIterable(qiRes)
                .map(mapValue -> fromMapValue(mapValue))
                .collect(Collectors.toList());

        return new PageImpl<>(result);
    }

    private static String limitOffsetSql(Pageable pageable,
        @NonNull Map<String, FieldValue> params, String where) {

        String sql = SELECT_ALL + (where == null ? "" : where);

        if ( ! (pageable == null || pageable.isUnpaged()) ) {

            sql = sql + orderBySql(pageable.getSort());

            sql += " LIMIT $kv_limit_ OFFSET $kv_offset_";

            params.put("$kv_limit_", new LongValue(pageable.getPageSize()));
            params.put("$kv_offset_", new LongValue(pageable.getOffset()));
        }

        String declare = params.entrySet()
            .stream()
            .map((entry) -> entry.getKey() + " " + entry.getValue().getType().name())
            .collect(Collectors.joining("; ", "DECLARE ", "; "));

        sql = declare + " " + sql;

        return sql;
    }

    public Iterable<Machine> findByMachineIdNameRegexpIgnoreCase(String name) {

        String where = " WHERE regex_like(t.name, $param, 'i') ";
        Map<String, FieldValue> params = new HashMap<>();
        params.put("$param", new StringValue(".*" + name + ".*"));
        String sql = limitOffsetSql(null, params, where);
        //System.out.println("sql: " + sql);

        final PreparedStatement preparedStatement = ensurePrepared(sql)
            .copyStatement();

        params.forEach((k, v) -> preparedStatement.setVariable(k, v));

        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        return IterableUtil.getIterableFromStream(
            IterableUtil.getStreamFromIterable(qiRes)
                .map(mapValue -> fromMapValue(mapValue)));
    }


    @Override
    public Page<Machine> findAllByMachineIdName(String name, Pageable pageable) {
        String where = " WHERE t.name = $name";
        Map<String, FieldValue> params = new HashMap<>();
        params.put("$name", new StringValue(name));
        String sql = limitOffsetSql(pageable, params, where);
        //System.out.println("sql: " + sql);

        final PreparedStatement preparedStatement = ensurePrepared(sql)
            .copyStatement();

        params.forEach((k, v) -> preparedStatement.setVariable(k, v));

        Iterable<MapValue> qiRes = client.queryIterable(
            new QueryRequest().setPreparedStatement(preparedStatement));

        List<Machine> result =
            IterableUtil.getStreamFromIterable(qiRes)
                .map(mapValue -> fromMapValue(mapValue))
                .collect(Collectors.toList());

        return new PageImpl<>(result);
    }

    // Following methods transform POJO key/entity to/from MapValue
    private MapValue toMapValue(Machine machine) {
        MapValue row = keyToMapValue(machine.getMachineId());
        row.put(KV_JSON, dataToMapValue(machine));
        return row;
    }

    private MapValue keyToMapValue(MachineId machineId) {
        if (machineId == null) {
            return null;
        }

        MapValue compositeKey = new MapValue();
        compositeKey.put("name", machineId.getName());
        compositeKey.put("version", machineId.getVersion());
        return compositeKey;
    }

    private MapValue dataToMapValue(Machine machine) {
        if (machine == null) {
            return null;
        }

        MapValue result = new MapValue();

        result.put("name", machine.getName());

        result.put("creationDate",
            new TimestampValue(machine.getCreationDate().getTime()));

        if (machine.getSetting() != null) {
            MapValue settingFV = new MapValue();
            machine.getSetting().forEach((k, v) -> settingFV.put(k, v));
            result.put("setting", settingFV);
        }

        if (machine.getTransitions() != null) {
            ArrayValue transitionsFV = new ArrayValue();
            result.put("transitions", transitionsFV);
            machine.getTransitions().forEach(i -> transitionsFV.add(transitionToMapValue(i)));
        }

        if (machine.getChilds() != null) {
            ArrayValue childsFV = new ArrayValue();
            result.put("childs", childsFV);
            machine.getChilds().forEach(i -> childsFV.add(keyToMapValue(i)));
        }

        return result;
    }

    private MapValue transitionToMapValue(Transition transition) {
        if (transition == null) {
            return null;
        }

        MapValue transitionMV = new MapValue();
        transitionMV.put("source", transition.getSource());
        transitionMV.put("destination", transition.getDestination());
        transitionMV.put("action", transition.getAction());
        return transitionMV;
    }

    private Transition transitionFromFieldValue(FieldValue trFV) {
        if (trFV == null || !trFV.isMap()) {
            return null;
        }

        Transition tr = new Transition();
        tr.setSource(trFV.asMap().getString("source"));
        tr.setDestination(trFV.asMap().getString("destination"));
        tr.setAction(trFV.asMap().getString("action"));
        return tr;
    }

    private MachineId keyFromFieldValue(FieldValue compositeKey) {
        if (compositeKey == null || !compositeKey.isMap()) {
            return null;
        }

        MachineId machineId = new MachineId();
        if (compositeKey.asMap().get("name") != null &&
            compositeKey.asMap().get("name").isString()) {
            machineId.setName(compositeKey.asMap().getString("name"));
        }
        if (compositeKey.asMap().get("version") != null &&
            compositeKey.asMap().get("version").isString()) {
            machineId.setVersion(compositeKey.asMap().getString("version"));
        }

        return machineId;
    }

    private Machine fromMapValue(MapValue value) {
        if (value == null) {
            return null;
        }

        Machine result = new Machine();
        result.setMachineId(keyFromFieldValue(value));
        if (value.get(KV_JSON) != null && value.get(KV_JSON).isMap()) {
            dataFromMapValue(value.get(KV_JSON).asMap(), result);
        }
        return result;
    }

    private void dataFromMapValue(MapValue value, Machine result) {
        result.setName(value.getString("name"));

        FieldValue creationDateFV = value.get("creationDate");
        if ( creationDateFV != null && creationDateFV.isString()) {
            result.setCreationDate(
                new Date(creationDateFV.asTimestamp().getValue().getTime()));
        }

        FieldValue settingFV = value.get("setting");
        if ( settingFV != null && settingFV.isMap()) {
            Map<String, String> setting = new HashMap<>();
            settingFV.asMap().getMap().forEach((k, v) -> setting.put(k, v.getString()));
            result.setSetting(setting);
        }

        FieldValue transitionsFV = value.get("transitions");
        if ( transitionsFV != null && transitionsFV.isArray()) {
            List<Transition> transitions = new ArrayList<>();
            transitionsFV.asArray().forEach(i -> transitions.add(transitionFromFieldValue(i)));
            result.setTransitions(transitions);
        }

        FieldValue childsFV = value.get("childs");
        if ( childsFV != null && childsFV.isArray()) {
            List<MachineId> childs = new ArrayList<>();
            childsFV.asArray().forEach(i -> childs.add(keyFromFieldValue(i)));
            result.setChilds(childs);
        }
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

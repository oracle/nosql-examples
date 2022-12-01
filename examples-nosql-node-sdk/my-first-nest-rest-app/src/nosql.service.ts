import { NoSQLClient, ServiceType, QueryResult, CapacityMode } from 'oracle-nosqldb';

export class NoSQLService {

    connection: any;
    private static instance: NoSQLService;

    private constructor() {
        this.creatDbConnection();
    }

    static async initDb() {
        if (!this.instance) {
            this.instance = new NoSQLService();
        }
        return this.instance;
    }

    static getInstance() {
        return this.instance;
    }

    getConnection() {
        return this.connection;
    }

    static async findAll(tablename, params) {
      let rows = [];
      let statement = `SELECT * FROM ${tablename}`;

      if (params.where )
         statement = statement + " WHERE " + params.where;
      if (params.orderby )
         statement = statement + " ORDER BY " + params.orderby;
      if (params.limit)
         statement = statement + " LIMIT " + params.limit;
      if (params.page && params.limit) {
         let offset = +params.page*+params.limit;
         statement = statement + " OFFSET " + offset;
      }

      for await(const res of this.getInstance().getConnection().queryIterable(statement)) {
        rows.push.apply(rows, res.rows);
      }
      return rows;
    }

    static async findOne (tablename, id) {
        const result = await this.getInstance().getConnection().get(tablename, { id })
        if (result.row)
          return result.row;
        else
          return {}
    }

    static async create (tablename, record) {
        const result = await this.getInstance().getConnection().put(tablename, record );
        return { result: result};
    }

    static async update (tablename, id,  record) {
        const result = await this.getInstance().getConnection().putIfPresent(tablename, Object.assign(record, {id}) );
        return { result: result};
    }

    static async remove (tablename, id) {
        const result = await this.getInstance().getConnection().delete(tablename, { id });
        return { result: result};
    }

    static async createDbTable() {
       const createDDL = 'CREATE TABLE IF NOT EXISTS users (id String, info JSON , PRIMARY KEY (id))';
       // readUnits, writeUnits, storageGB using same values as for Always free
       let resTab = await this.getInstance().getConnection().tableDDL(createDDL, {
         tableLimits: {
            // mode: CapacityMode.ON_DEMAND,
            mode: CapacityMode.PROVISIONED,
            readUnits: 50,
            writeUnits: 50,
            storageGB: 25
         }
         , complete: true
       }) ;
       console.log('  Creating table %s', resTab.tableName);
       console.log('  Table state: %s', resTab.tableState.name);
    }

    private creatDbConnection() {
       switch(process.env.NOSQL_ServiceType) {
       case 'useInstancePrincipal':
           this.connection = new NoSQLClient({
               serviceType: ServiceType.CLOUD,
               region: process.env.NOSQL_REGION ,
               compartment:process.env.NOSQL_COMPID,
               auth: {
                 iam: {
                     useInstancePrincipal: true
                 }
               }
           });
           break;
       case 'useDelegationToken':
           this.connection = new NoSQLClient({
               serviceType: ServiceType.CLOUD,
               region: process.env.NOSQL_REGION ,
               compartment:process.env.NOSQL_COMPID,
               auth: {
                 iam: {
                     useInstancePrincipal: true,
                     delegationTokenProvider: process.env.OCI_DELEGATION_TOKEN_FILE
                 }
               }
           });
           break;
       default:
          // on-premise non-secure configuration or Cloud Simulator
          this.connection = new NoSQLClient({
               serviceType: ServiceType.KVSTORE,
               endpoint: process.env.NOSQL_ENDPOINT + ":" + process.env.NOSQL_PORT
           });
       }
    }

}


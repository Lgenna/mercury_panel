package database;

public class QueryLogDbSchema {
    public static final class QueryLogTable {
        public static final String TABLE_NAME = "querylog_list";

        public static final class Cols {
            public static final String TIME = "TIME";
            public static final String DOMAIN = "DOMAIN";
            public static final String STATUS = "STATUS";
        }
    }
}

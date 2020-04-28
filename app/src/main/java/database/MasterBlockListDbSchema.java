package database;

public class MasterBlockListDbSchema {
    public static final class MasterBlockListTable {
        public static final String TABLE_NAME = "master_block_list_list";

        public static final class Cols {
            static final String BLOCKLIST = "BLOCKLIST";
            public static final String DOMAIN = "DOMAIN";
            static final String STATUS = "STATUS";
        }
    }
}

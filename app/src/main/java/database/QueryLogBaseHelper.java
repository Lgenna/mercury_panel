package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import database.QueryLogDbSchema.QueryLogTable;

public class QueryLogBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "querylog.db";

    public QueryLogBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + QueryLogTable.TABLE_NAME + " ( " +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QueryLogTable.Cols.TIME + ", " +
                QueryLogTable.Cols.DOMAIN + ", " +
                QueryLogTable.Cols.STATUS +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + QueryLogTable.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String time, String domain, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QueryLogTable.Cols.TIME, time);
        contentValues.put(QueryLogTable.Cols.DOMAIN, domain);
        contentValues.put(QueryLogTable.Cols.STATUS, status);
        long result = db.insert(QueryLogTable.TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + QueryLogTable.TABLE_NAME, null);
        return res;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(QueryLogTable.TABLE_NAME, "ID = ?", new String[]{id});
    }


}

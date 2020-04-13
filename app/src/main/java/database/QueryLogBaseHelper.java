package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static database.QueryLogDbSchema.QueryLogTable.Cols.TIME;
import static database.QueryLogDbSchema.QueryLogTable.Cols.DOMAIN;
import static database.QueryLogDbSchema.QueryLogTable.Cols.STATUS;
import static database.QueryLogDbSchema.QueryLogTable.TABLE_NAME;

public class QueryLogBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 3;
    private static final String DATABASE_NAME = "querylog.db";

    public QueryLogBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIME + ", " +
                DOMAIN + ", " +
                STATUS +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(long time, String domain, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TIME, time);
        contentValues.put(DOMAIN, domain);
        contentValues.put(STATUS, status);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

    public Cursor selectData(String column, String element) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + column + " = '" + element + "'", null);
//        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + selectType + " = '" + selectElement + "'", null);

    }

    public Cursor specialSelectData(String column, String operation1, String element1, String operation2, String element2) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + column + " " + operation1 + " " + element1 + " AND " + column + " " + operation2 + " " + element2, null);

    }

    public Cursor moreSpecialSelectData(String column, String operation1, String element1, String operation2, String element2, String element3) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + column + " " + operation1 + " " + element1 + " AND " + column + " " + operation2 + " " + element2 + " AND STATUS = '" + element3 + "'" , null);

    }

    public long countData() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
//        db.close();
        return count;
    }
}

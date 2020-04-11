package database;

import android.b.networkingapplication2.MasterBlocklist;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.List;

import static database.MasterBlockListDbSchema.MasterBlockListTable.TABLE_NAME;
import static database.MasterBlockListDbSchema.MasterBlockListTable.Cols.BLOCKLIST;
import static database.MasterBlockListDbSchema.MasterBlockListTable.Cols.DOMAIN;
import static database.MasterBlockListDbSchema.MasterBlockListTable.Cols.STATUS;

public class MasterBlockListBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "master_block_list.db";

    public MasterBlockListBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BLOCKLIST + ", " +
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

//    public boolean insertData(String blocklist, String domain, int status) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(BLOCKLIST, blocklist);
//        contentValues.put(DOMAIN, domain);
//        contentValues.put(STATUS, status);
//        long result = db.insert(TABLE_NAME, null, contentValues);
//        return result != -1;
//    }

    public boolean insertData(List<MasterBlocklist> listOfEntries) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        ContentValues contentValues = new ContentValues();

        long result = -1;

        try {
            for (MasterBlocklist entry : listOfEntries) {
                contentValues.put(DOMAIN, entry.getDomain());
                contentValues.put(BLOCKLIST, entry.getBlockList());
                contentValues.put(STATUS, entry.getStatus());

                result = db.insertOrThrow(TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
//            db.close();
        }

        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

//    public boolean updateData(String domain, String status, String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DOMAIN, domain);
//        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
//        return true;
//    }

//    public Integer deleteData(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
//    }

    public void deleteData(List<String> idList) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        try {
            for (String id : idList) {
                db.delete(TABLE_NAME, "ID = ?", new String[]{id});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor selectData(String selectElement) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + DOMAIN + " = '" + selectElement + "'", null);

    }

    public long countData() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }
}

package com.lithium.wall4u.Discover;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ProductsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "product.db";
    private static final int DATABASE_VERSION = 1;

    //PRODUCT CACHE TABLE.
    public static final String PRODUCT_CACHE_TABLE_NAME = "product_cache";

    //COLUMNS.
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHOTO_ID = "photo_id";
    private static final String COLUMN_MEDIUM_PIC = "medium_photo";
    private static final String COLUMN_ORIGINAL_PIC = "original_photo";
    private static final String COLUMN_OPEN_IN_PEXELS = "open_in_pexels";
    private static final String COLUMN_PHOTOGRAPHER_NAME = "photographer_name";
    private static final String COLUMN_PHOTOGRAPHER_ID = "photographer_id";
    private static final String COLUMN_PHOTOGRAPHER_URL = "photographer_url";
    private static final String COLUMN_DATE_OF_UPDATE_CACHE = "date_to_update_cache";

    private static String CREATE_PRODUCT_CACHE_TABLE = "CREATE TABLE " + PRODUCT_CACHE_TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PHOTO_ID + " TEXT, " +
            COLUMN_MEDIUM_PIC + " TEXT, " +
            COLUMN_ORIGINAL_PIC + " TEXT, " +
            COLUMN_OPEN_IN_PEXELS + " TEXT, " +
            COLUMN_PHOTOGRAPHER_NAME + " TEXT, " +
            COLUMN_PHOTOGRAPHER_ID + " TEXT, " +
            COLUMN_PHOTOGRAPHER_URL + " TEXT, " +
            COLUMN_DATE_OF_UPDATE_CACHE + " TEXT);";

    public ProductsDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCT_CACHE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCT_CACHE_TABLE_NAME);
        onCreate(db);
    }

    //For adding the products.
    public boolean addProduct(String TABLE_NAME, String photoId, String mediumPic, String originalPic,
                              String openInPexels, String photographerName, String photographerId,
                              String photographerUrl, String date_to_update) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_PHOTO_ID, photoId);
        contentValues.put(COLUMN_MEDIUM_PIC, mediumPic);
        contentValues.put(COLUMN_ORIGINAL_PIC, originalPic);
        contentValues.put(COLUMN_OPEN_IN_PEXELS, openInPexels);
        contentValues.put(COLUMN_PHOTOGRAPHER_NAME, photographerName);
        contentValues.put(COLUMN_PHOTOGRAPHER_ID, photographerId);
        contentValues.put(COLUMN_PHOTOGRAPHER_URL, photographerUrl);

        if (TABLE_NAME.equals(PRODUCT_CACHE_TABLE_NAME)) {
            contentValues.put(COLUMN_DATE_OF_UPDATE_CACHE, date_to_update);
        }

        long result = database.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    //Read All Data.
    public Cursor readAllData(String TABLE_NAME) {
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC";
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = null;
        if (database != null) {
            cursor = database.rawQuery(query, null);
        }
        return cursor;
    }

    //Delete Data.
    public boolean deleteItem (String TABLE_NAME, String photoId) {
        SQLiteDatabase database = this.getWritableDatabase();
        long result = database.delete(TABLE_NAME,
                COLUMN_PHOTO_ID + "=?", new String[]{photoId});
        return result != -1;
    }

    //Delete All Items in table.
    public void deleteAll (String TABLE_NAME) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM "+ TABLE_NAME);
    }

}

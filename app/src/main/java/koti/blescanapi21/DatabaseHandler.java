package koti.blescanapi21;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikes on 21.3.2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ZombiDb";
    private static final String TABLE_NAME = "Zombi";
    private static final String KEY_ID = "id";
    private static final String ADDRESS = "address";
    private static final String RSSI = "rssi";
    private static final String NLOC = "nloc";
    private static final String ELOC = "eloc";
    private static final String USER = "user";

    private static final String[] COLUMNS = { KEY_ID, ADDRESS, RSSI, NLOC, ELOC, USER };


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE Zombi ( "
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, address STRING, rssi STRING,"  +
                " nloc STRING, eloc STRING, user STRING )";

        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public boolean checkNodeExists(String address) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT count(*) FROM " + TABLE_NAME + " WHERE address = '" + address + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        Log.i("nodecount", Integer.toString(count));
        //cursor.close();

        if (count > 0)
            return true;

        return false;
    }

    public boolean higherRSSI(String address, String rssi) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE address = '" + address + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) {
            return false;
        }

        if (cursor.moveToFirst()) {
            int rssiOld = Integer.parseInt(cursor.getString(cursor.getColumnIndex(RSSI)));
            int rssiNew = Integer.parseInt(rssi);
            cursor.close();

            if (rssiNew > rssiOld) {
                Log.i("rssicheck", String.valueOf(rssiNew));
                return true;
            }
            return false;
        }
        return false;
    }

    public void updateNode(String address, String rssi, String nloc, String eloc, String user) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + TABLE_NAME + " SET rssi = '" + rssi + "', nloc = '" + nloc + "', eloc = '" + eloc + "', user = '" + user + "'  WHERE address = '" + address + "'";

        Cursor cursor= db.rawQuery(updateQuery, null);

        cursor.moveToFirst();
        cursor.close();

        Log.i("DatabaseHandler", "Updated node");
    }

    public void addNode(String address, String rssi, String nloc, String eloc, String user) {

        // todo:
        // tarkastaa et onko siellä jo deviceaddress. jos on, niin kattoo mikä rssi ja verrataan,
        // jos lisättävän rssi on pienempi ku kannassa oleva, korvataan, muutoin ohitetaan

        if (!checkNodeExists(address)) {

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            Log.d("DB_IN_ADDRESS", address);
            Log.d("DB_IN_RSSI", rssi);
            Log.d("DB_IN_NLOC", nloc);
            Log.d("DB_IN_ELOC", eloc);
            Log.d("DB_IN_USER", user);

            values.put(ADDRESS, address);
            values.put(RSSI, rssi);
            values.put(NLOC, nloc);
            values.put(ELOC, eloc);
            values.put(USER, user);

            db.insert(TABLE_NAME, null, values);
            db.close();

            Log.i("DatabaseHandler", "Added node");
        }
        else{
            if (higherRSSI(address, rssi)) {
                updateNode(address, rssi, nloc, eloc, user);
                Log.i("Checknodeexists: ", Boolean.toString(checkNodeExists(address)));
            }
        }
    }

    public List<String[]> getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String[]> deviceIdList = new ArrayList<String[]>();

        //List<String> l1 = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY id";
        Cursor cursor = db.rawQuery(selectQuery, null);

        // this is quick fix for bug which crashes if db is empty
        if (cursor.getCount() == 0) {
            return null;
        }

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                String m_id = cursor.getString(cursor.getColumnIndex(KEY_ID));
                String m_address = cursor.getString(cursor.getColumnIndex(ADDRESS));
                String m_rssi = cursor.getString(cursor.getColumnIndex(RSSI));
                String m_nloc = cursor.getString(cursor.getColumnIndex(NLOC));
                String m_eloc = cursor.getString(cursor.getColumnIndex(ELOC));
                String m_user = cursor.getString(cursor.getColumnIndex(USER));

                Log.d("DB_OUT_KEY_ID", m_id);
                Log.d("DB_OUT_ADDRESS", m_address);
                Log.d("DB_OUT_RSSI", m_rssi);
                Log.d("DB_OUT_NLOC", m_nloc);
                Log.d("DB_OUT_ELOC", m_eloc);
                Log.d("DB_OUT_USER", m_user);

                String[] l1 = {m_id, m_nloc, m_eloc};

                deviceIdList.add(l1);
                cursor.moveToNext();
            }
        }

        return deviceIdList;
    }

    public void clearDatabase(String TABLE_NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);
    }

    public boolean isDuplicate(String address){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE  = " + address + " ORDER BY id";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            return true;
        }
        else{
            return false;
        }
    }
}

package koti.blescanapi21;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    private static final String USER = "user";

    private static final String[] COLUMNS = { KEY_ID, ADDRESS, RSSI, NLOC, USER };


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE Zombi ( "
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, address STRING, rssi STRING, nloc STRING, type " +
                "STRING, user STRING )";

        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void addDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ADDRESS, device.getAddress());
        values.put(RSSI, device.getRssi());
        values.put(NLOC, device.getPNloc());
        values.put(USER, device.getUser());

        db.insert(TABLE_NAME,null, values);
        db.close();

        Log.i("DatabaseHandler", "Added migraine");
    }

    public List<Device> getDevices() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Device> migraineList = new ArrayList<Migreeni>();

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
                String m_user = cursor.getString(cursor.getColumnIndex(USER));

                Log.d("KEY_ID", m_id);
                Log.d("ADDRESS", m_address);
                Log.d("RSSI", m_rssi);
                Log.d("NLOC", m_nloc);
                Log.d("USER", m_user);


                migraineList.add(new Device(m_address, m_rssi,
                        m_nloc,
                        m_user);
                cursor.moveToNext();
            }
        }

        return deviceList;
    }

}

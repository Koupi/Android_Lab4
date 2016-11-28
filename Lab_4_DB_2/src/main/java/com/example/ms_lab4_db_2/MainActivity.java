package com.example.ms_lab4_db_2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import java.sql.Timestamp;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    final int DB_VERSION = 2;
    DBHelper dbHelper;
    private String dbName = "classmate_db";
    private String tableName = "classmate";
    private String middlename = "patronymic";
    private String lastname = "lastname";
    private String firstname = "firstname";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(tableName, null, null);
        if(DB_VERSION==1) {
            db.execSQL("INSERT INTO classmate (name, addTime) VALUES ('Andrey Vychev', datetime('now'))");
            db.execSQL("INSERT INTO classmate (name, addTime) VALUES ('Alex Zhidkov', datetime('now'))");
            db.execSQL("INSERT INTO classmate (name, addTime) VALUES ('Katya Iliashevich', datetime('now'))");
            db.execSQL("INSERT INTO classmate (name, addTime) VALUES ('Nastya Kononova', datetime('now'))");
            db.execSQL("INSERT INTO classmate (name, addTime) VALUES ('Alex Kononok', datetime('now'))");
        }
        else if (DB_VERSION==2){
            db.execSQL("INSERT INTO classmate (firstname, patronymic, lastname, addTime) VALUES ('Andrey',' Batkovich', 'Vychev', datetime('now'))");
            db.execSQL("INSERT INTO classmate (firstname, patronymic, lastname, addTime) VALUES ('Alex', 'Gennadievich', 'Zhidkov', datetime('now'))");
            db.execSQL("INSERT INTO classmate (firstname, patronymic, lastname, addTime) VALUES ('Katya', 'Batkovna', 'Iliashevich', datetime('now'))");
            db.execSQL("INSERT INTO classmate (firstname, patronymic, lastname, addTime) VALUES ('Nastya', 'Batkovna', 'Kononova', datetime('now'))");
            db.execSQL("INSERT INTO classmate (firstname, patronymic, lastname, addTime) VALUES ('Alex', 'Batkovich', 'Kononok', datetime('now'))");
        }
        dbHelper.close();
    }

    @Override
    public void onClick(View v) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.button_add:{
                Timestamp date = new Timestamp(new java.util.Date().getTime());
                String name = "Maria Batkovna Lukianova";
                if(DB_VERSION == 1) {
                    cv.put("name", name);
                }
                else if(DB_VERSION == 2){
                    String []nameParts = name.split(" ");
                    cv.put(firstname, nameParts[0]);
                    cv.put(middlename, nameParts[1]);
                    cv.put(lastname, nameParts[2]);
                }
                cv.put("addTime", date.toString());
                long rowID = db.insert(tableName, null, cv);
                Toast toast = Toast.makeText(getApplicationContext(), Long.toString(rowID), Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
            case R.id.button_change:{
                String selectQuery= "SELECT * FROM " + tableName+" ORDER BY id DESC LIMIT 1";
                Cursor cursor = db.rawQuery(selectQuery, null);
                String id = "";
                if(cursor.moveToFirst()) {
                    id = cursor.getString(cursor.getColumnIndex("id"));
                }
                cursor.close();
                String name = "Vlad Batkovich Makarevich";
                if(DB_VERSION==1) {
                    cv.put("name", name);
                }else if(DB_VERSION==2){
                    String []nameParts = name.split(" ");
                    cv.put(firstname, nameParts[0]);
                    cv.put(middlename, nameParts[1]);
                    cv.put(lastname, nameParts[2]);
                }
                db.update(tableName, cv, "id="+id, null);
                break;
            }
            case R.id.button_show: {
                ArrayList<String> values = new ArrayList<>();
                    Cursor c = db.query(tableName, null, null, null, null, null, null);
                    if (c.moveToFirst()) {
                        if(DB_VERSION==1) {
                            int idColIndex = c.getColumnIndex("id");
                            int nameColIndex = c.getColumnIndex("name");
                            int dateColIndex = c.getColumnIndex("addTime");
                            do {
                                values.add("ID = " + c.getInt(idColIndex) +
                                        "\nname = " + c.getString(nameColIndex) +
                                        "\nadded = " + c.getString(dateColIndex));
                            } while (c.moveToNext());
                        }
                        else if(DB_VERSION==2) {
                            int idColIndex = c.getColumnIndex("id");
                            int firtsNameColIndex = c.getColumnIndex(firstname);
                            int lastNameColIndex = c.getColumnIndex(lastname);
                            int middlenameCloumnIndex = c.getColumnIndex(middlename);
                            int dateColIndex = c.getColumnIndex("addTime");
                            do {
                                values.add("ID = " + c.getInt(idColIndex) +
                                        "\nfirstname = " + c.getString(firtsNameColIndex)+
                                        "\nmiddlename = " + c.getString(middlenameCloumnIndex)+
                                        "\nlastname = " + c.getString(lastNameColIndex)+
                                        "\nadded = " + c.getString(dateColIndex));
                            } while (c.moveToNext());
                        }
                    }
                c.close();
                Intent intent = new Intent(this, ShowActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
                break;
            }
        }
        dbHelper.close();
    }

    class DBHelper extends SQLiteOpenHelper {
        Context context;
        public DBHelper(Context context) {
            super(context, dbName, null, DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if(DB_VERSION==1) {
                db.execSQL("CREATE TABLE classmate (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, addTime DATETIME);");
            }
            else if (DB_VERSION==2){
                db.execSQL("CREATE TABLE classmate (id INTEGER PRIMARY KEY AUTOINCREMENT, firstname TEXT, patronymic TEXT, lastname TEXT, addTime DATETIME);");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int
                newVersion) {
            if (oldVersion == 1 && newVersion > 1) {

                db.beginTransaction();
                try {
                    db.execSQL("ALTER TABLE classmate ADD COLUMN lastname TEXT;");
                    db.execSQL("ALTER TABLE classmate ADD COLUMN firstname TEXT;");
                    db.execSQL("ALTER TABLE classmate ADD COLUMN patronymic TEXT;");
                    final Cursor cursor = db.query(tableName, null, null, null, null, null, null);
                    if(cursor != null) {
                        if (cursor.moveToFirst()) {
                            final ContentValues values = new ContentValues();
                            int idColIndex = cursor.getColumnIndex("id");
                            int nameColIndex = cursor.getColumnIndex("name");
                            do {
                                int oldId = cursor.getInt(idColIndex);
                                String name = cursor.getString(nameColIndex);
                                String nameParts[] = name.split(" ");
                                if(nameParts.length>0) {
                                    values.clear();
                                    values.put(firstname, nameParts[0]);
                                    values.put(middlename, nameParts.length>2?nameParts[1]:"");
                                    if(nameParts.length>1) {
                                        if (nameParts.length == 2) {
                                            values.put(lastname, nameParts[1]);
                                        } else {
                                            String newlastName = nameParts[2];
                                            for (int k = 3; k< nameParts.length; k++){
                                                newlastName+= " "+nameParts[k];
                                            }
                                            values.put(lastname, newlastName);
                                        }
                                    }
                                    db.update(tableName, values, "id" + "=?", new String[]{Integer.toString(oldId)});
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                    db.execSQL("CREATE TEMPORARY TABLE classmate_tmp (id INTEGER, firstname TEXT, patronymic TEXT, lastname TEXT, addTime DATETIME);");
                    db.execSQL("insert into classmate_tmp select id, firstname, patronymic, lastname, addTime from classmate;");
                    db.execSQL("DROP TABLE classmate;");
                    db.execSQL("CREATE TABLE classmate (id INTEGER PRIMARY KEY AUTOINCREMENT, firstname TEXT, patronymic TEXT, lastname TEXT, addTime DATETIME);");
                    db.execSQL("INSERT INTO classmate SELECT id, firstname, patronymic, lastname, addTime FROM classmate_tmp;");
                    db.execSQL("DROP TABLE classmate_tmp;");
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
    }
}

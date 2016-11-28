package com.example.ms_lab4_belarus;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DBHelper dbHelper;
    private String dbName = "DB_Belarus";
    private String tableName ="region";
    private Spinner sort_param;
    private Spinner agreg_param;
    private EditText population_param;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        sort_param = (Spinner)findViewById(R.id.spinner_sort) ;
        agreg_param = (Spinner)findViewById(R.id.spinner_agreg);
        population_param = (EditText)findViewById(R.id.editText_filer_population);
        dbHelper.close();
    }
    private void getInOrder(Cursor c, ArrayList<String> values){
        if (c.moveToFirst()) {

            int nameColIndex = c.getColumnIndex("name");
            int areaColIndex = c.getColumnIndex("area");
            int populationColIndex = c.getColumnIndex("population");
            int codeColIndex = c.getColumnIndex("code");
            do{
                values.add("District: " + c.getString(nameColIndex) +
                        "\nRegion: " + c.getString(areaColIndex) +
                        "\nPopulation = " + c.getString(populationColIndex) +
                        "\nCode = " + c.getString(codeColIndex)
                );
            } while (c.moveToNext());
        }
    }
    @Override
    public void onClick(View v) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.button_agreg: {
                ArrayList<String> values = new ArrayList<>();
                long selected = agreg_param.getSelectedItemId();
                String agregParam = null;
                if(selected==0){
                    agregParam = "count(population) as result";
                }
                if(selected==1){
                    agregParam = "max(population) as result";
                }
                if(selected==2){
                    agregParam = "sum(population) as result";
                }

                Cursor c = db.query(tableName,  new String[]{agregParam}, null, null, null, null, null);
                if (c.moveToFirst()) {

                    int nameColIndex = c.getColumnIndex("result");
                    do{
                        values.add("Result: " + c.getString(nameColIndex));
                    } while (c.moveToNext());
                }
                c.close();
                Intent intent = new Intent(this, ShowActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
                break;
            }
            case R.id.button_group: {
                ArrayList<String> values = new ArrayList<>();
                Cursor c = db.query(tableName, new String[]{"area", "max(population) as max_population"}, null, null, "area", null, null);
                if (c.moveToFirst()) {
                    int nameColIndex = c.getColumnIndex("area");
                    int maxPopulation = c.getColumnIndex("max_population");
                    do{
                        values.add("Region: " + c.getString(nameColIndex) + "\nMax population: " + c.getString(maxPopulation));
                    } while (c.moveToNext());
                }
                c.close();
                Intent intent = new Intent(this, ShowActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
                break;
            }
            case R.id.button_sort:{
                ArrayList<String> values = new ArrayList<>();
                long selected = sort_param.getSelectedItemId();
                String sortParam = "population";
                if(selected==0){
                    sortParam = "name";
                }
                if(selected==1){
                    sortParam = "area";
                }
                Cursor c = db.query(tableName, null, null, null, null, null, sortParam);
                getInOrder(c, values);
                c.close();
                Intent intent = new Intent(this, ShowActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
                break;
            }
            case R.id.button_show: {
                ArrayList<String> values = new ArrayList<>();
                Cursor c = db.query(tableName, null, null, null, null, null, null);
                getInOrder(c, values);
                c.close();
                Intent intent = new Intent(this, ShowActivity.class);
                intent.putExtra("values", values);
                startActivity(intent);
                break;
            }
            case R.id.button_filter :{
                try {
                    int population_param_value = Integer.parseInt(population_param.getText().toString());
                    ArrayList<String> values = new ArrayList<>();
                    Cursor c = db.query(tableName, null, "population >= ?", new String[]{Integer.toString(population_param_value)}, null, null, null);
                    getInOrder(c, values);
                    c.close();
                    Intent intent = new Intent(this, ShowActivity.class);
                    intent.putExtra("values", values);
                    startActivity(intent);
                }
                catch (Exception e){

                }
                break;
            }
            case R.id.button_filter_area:{
                try {
                    int population_param_value = Integer.parseInt(population_param.getText().toString());
                    ArrayList<String> values = new ArrayList<>();
                    Cursor c = db.query(tableName, new String[]{"area", "sum(population) as sum_population"}, null, null, "area", "sum(population) >= "+population_param_value, null);
                    if (c.moveToFirst()) {
                        int nameColIndex = c.getColumnIndex("area");
                        int sumPopulation = c.getColumnIndex("sum_population");
                        do{
                            values.add("Region: " + c.getString(nameColIndex) + "\nPopulation: " + c.getString(sumPopulation));
                        } while (c.moveToNext());
                    }
                    c.close();
                    Intent intent = new Intent(this, ShowActivity.class);
                    intent.putExtra("values", values);
                    startActivity(intent);
                }
                catch (Exception e){

                }
                break;
            }
        }
        dbHelper.close();
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, dbName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table region (id integer primary key autoincrement, name text, area text, population integer, code integer);");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Барановичский', 'Брестская', '31886', '163')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Пружанский', 'Брестская', '47910', '1632')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Пинский', 'Брестская', '47110', '165')");

            db.execSQL("insert into region (name, area, population, code) VALUES ('Березенский', 'Минская', '22614', '1715')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Борисовский', 'Минская', '181946', '177')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Вилейский', 'Минская', '48102', '1771')");

            db.execSQL("insert into region (name, area, population, code) VALUES ('Браславский', 'Витебская', '26324', '1643')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Глубокский', 'Витебская', '37712', '1644')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Ушачский', 'Витебская', '13805', '2158')");

            db.execSQL("insert into region (name, area, population, code) VALUES ('Бобруйский', 'Могилевская', '173621', '225')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Хотимский', 'Могилевская', '10977', '2247')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Осиповичский', 'Могилевская', '48291', '2235')");

            db.execSQL("insert into region (name, area, population, code) VALUES ('Брагинский', 'Гомельская', '12128', '2344')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Добрушский', 'Гомельская', '36864', '2333')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Рогачевский', 'Гомельская', '57776', '2339')");

            db.execSQL("insert into region (name, area, population, code) VALUES ('Волковысский', 'Гродненская', '70737', '1512')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Лидский', 'Гродненская', '132114', '154')");
            db.execSQL("insert into region (name, area, population, code) VALUES ('Слонимский', 'Гродненская', '65090', '1562')");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int
                newVersion) {
        }
    }
}

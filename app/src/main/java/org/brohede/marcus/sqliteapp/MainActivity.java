package org.brohede.marcus.sqliteapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ArrayAdapter adapter;
    private List<Mountain> listData = new ArrayList<>();
    MountainReaderDbHelper alice;
    boolean isName = false;
    boolean isHeight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FetchData().execute();

        adapter = new ArrayAdapter(getApplicationContext(),R.layout.list_item_textview,R.id.my_item_textview,listData);

        ListView myListView = (ListView)findViewById(R.id.my_listview);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Mountain m = listData.get(position);
                Toast.makeText(getApplicationContext(),m.info(),Toast.LENGTH_SHORT).show();

            }
        });
        myListView.setAdapter(adapter);

        // DATABASE
        alice = new MountainReaderDbHelper(getApplicationContext()) {
        };
    }

    public void fetchDb() {
        adapter.clear();
        SQLiteDatabase dbRead = alice.getReadableDatabase();
        String[] projection = {
                MountainReaderContract.MountainEntry.COLUMN_NAME_NAME,
                MountainReaderContract.MountainEntry.COLUMN_NAME_LOCATION,
                MountainReaderContract.MountainEntry.COLUMN_NAME_HEIGHT
        };

        String sortOrder =
                MountainReaderContract.MountainEntry.COLUMN_NAME_NAME + " DESC ";
        if(isHeight){
            sortOrder = MountainReaderContract.MountainEntry.COLUMN_NAME_HEIGHT + " DESC ";
        } else if(isName){
                sortOrder = MountainReaderContract.MountainEntry.COLUMN_NAME_NAME + " ASC ";
        }

        Cursor cursor = dbRead.query(
                MountainReaderContract.MountainEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        while (cursor.moveToNext()) {
            String mName = cursor.getString(cursor.getColumnIndexOrThrow(MountainReaderContract.MountainEntry.COLUMN_NAME_NAME));
            String mLocation = cursor.getString(cursor.getColumnIndexOrThrow(MountainReaderContract.MountainEntry.COLUMN_NAME_LOCATION));
            int mHeight = cursor.getInt(cursor.getColumnIndexOrThrow(MountainReaderContract.MountainEntry.COLUMN_NAME_HEIGHT));

            Mountain mberg = new Mountain(mName, mLocation, mHeight);
            adapter.add(mberg);

        }
        cursor.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.byName:
                isName = true;
                isHeight = false;
                fetchDb();
                return true;
            case R.id.byHeight:
                isName = false;
                isHeight = true;
                fetchDb();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private class FetchData extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params) {
            // These two variables need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a Java string.
            String jsonStr = null;

            try {
                // Construct the URL for the Internet service
                URL url = new URL("http://wwwlab.iit.his.se/brom/kurser/mobilprog/dbservice/admin/getdataasjson.php?type=brom");

                // Create the request to the PHP-service, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                return jsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Network error", "Error closing stream", e);
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            //Log.d("alicehej", "data" + o);

            try {
                adapter.clear();
                // Ditt JSON-objekt som Java
                JSONArray json1 = new JSONArray(o);
                 SQLiteDatabase dbWrite = alice.getWritableDatabase();

                for(int i = 0; i < json1.length(); i++) {

                    JSONObject berg = json1.getJSONObject(i);
                    //Log.d("alicehej", "berg" + berg.toString());

                    int ID = berg.getInt("ID");
                    String name = berg.getString("name");
                    String type = berg.getString("type");
                    String company = berg.getString("company");
                    String location = berg.getString("location");
                    String category = berg.getString("category");
                    int size = berg.getInt("size");
                    int cost = berg.getInt("cost");

                    ContentValues values = new ContentValues();
                    values.put(MountainReaderContract.MountainEntry.COLUMN_NAME_NAME, name);
                    values.put(MountainReaderContract.MountainEntry.COLUMN_NAME_LOCATION, location);
                    values.put(MountainReaderContract.MountainEntry.COLUMN_NAME_HEIGHT, size);

                    // Insert the new row, returning the primary key value of the new row
                    //dbWrite.insert(MountainReaderContract.MountainEntry.TABLE_NAME, null, values);
                    dbWrite.insertWithOnConflict(MountainReaderContract.MountainEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                fetchDb();

            } catch (JSONException e) {
                Log.e("brom","E:"+e.getMessage());
            }



        }
    }

    /*
        TODO: Create an App that stores Mountain data in SQLite database

        TODO: Schema for the database must include columns for all member variables in Mountain class
              See: https://developer.android.com/training/data-storage/sqlite.html

        TODO: The Main Activity must have a ListView that displays the names of all the Mountains
              currently in the local SQLite database.

        TODO: In the details activity an ImageView should display the img_url
              See: https://developer.android.com/reference/android/widget/ImageView.html

        TODO: The main activity must have an Options Menu with the following options:
              * "Fetch mountains" - Which fetches mountains from the same Internet service as in
                "Use JSON data over Internet" assignment. Re-use code.
              * "Drop database" - Which drops the local SQLite database

        TODO: All fields in the details activity should be EditText elements

        TODO: The details activity must have a button "Update" that updates the current mountain
              in the local SQLite database with the values from the EditText boxes.
              See: https://developer.android.com/training/data-storage/sqlite.html

        TODO: The details activity must have a button "Delete" that removes the
              current mountain from the local SQLite database
              See: https://developer.android.com/training/data-storage/sqlite.html

        TODO: The SQLite database must not contain any duplicate mountain names

     */
}

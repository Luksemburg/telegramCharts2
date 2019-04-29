package com.code.foo.telegramcharts2;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;

import android.view.MenuItem;
import android.widget.ScrollView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ScrollingActivity extends AppCompatActivity {

    public static Logger mainLog = Logger.getLogger(ScrollingActivity.class.getName());

    String large = "";
    private int graphNumber = 5;
    static int screenWidth;
    static int screenHeight;

    ArrayList<ArrayList<String>> temp_color = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> temp_legend = new ArrayList<ArrayList<String>>();

    String[] temp_types = new String[graphNumber];

    //boolean[] is_stacked = new boolean[graphNumber];
    //boolean[] is_precentage  = new boolean[graphNumber];
    //boolean[] is_y_scaled  = new boolean[graphNumber];

    Reader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        toDisplay to_display = new toDisplay(this);
        //Canvas canvas = new Canvas();
        //display.draw(canvas);

        for(int i = 1; i <= graphNumber; i++) {
            try {
                InputStream input = getAssets().open(String.valueOf(i) + "/overview.json");
                large = readFile(input);
            } catch (Exception e) {
                System.err.println("Cann't see file! #" + i);
            }

            reader = new Reader(large);

            //temp_color.add(new ArrayList<String>());
            temp_color.add(reader.getColor());
            temp_legend.add(reader.getLegends());

            //mainLog.info(large);
            temp_types[i - 1] = reader.getType();

        /*    is_stacked[i - 1] = reader.getStacked();
            is_precentage[i - 1] = reader.getPrecentage();
            is_y_scaled[i - 1] = reader.getYScaled();*/

            toDisplay.setFlags(reader.getStacked(), reader.getPrecentage(), reader.getYScaled(), i - 1);

            toDisplay.setCoordinates(reader.getCoordinatesX(), reader.getMaxX(), reader.getMinX(), reader.getCoordinatesY(),
                    reader.getMaxY(), reader.getMinY(), i - 1);

            //display.setBaseRects(i);
            //display.postInvalidate();
        }

        toDisplay.setTypes(temp_types);
        toDisplay.setLegends(temp_legend);
        toDisplay.setColors(temp_color);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_theme) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String readFile(InputStream is){
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int c;

        try{
            c = is.read();

            while(c != -1){
                outStream.write(c);
                c = is.read();
            }

            is.close();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }

        return outStream.toString();
    }
}

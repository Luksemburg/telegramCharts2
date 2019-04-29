package com.code.foo.telegramcharts2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class toDisplay extends View {

    public static Logger dispLog = Logger.getLogger(toDisplay.class.getName());

    private static int graphNumber = 5;

    private static String BAR = "bar";
    private static String AREA = "area";
    private static String LINE = "line";

    private static String[] types = new String[graphNumber];

    private int stepRects;
    private int back_lines = 6;
    private int padding = 10;
    private int textLittleGap = 3;
    private int textLittleSize = 30;
    private int textLegendSize = 2 * textLittleSize;


    private static int[] countLines = new int[graphNumber];

    private static ArrayList<ArrayList<Integer>> colors = new ArrayList<ArrayList<Integer>>();
    private static ArrayList<ArrayList<String>> legends = new ArrayList<ArrayList<String>>();

    private static ArrayList<ArrayList<ArrayList<Float>>> coordinates = new ArrayList<ArrayList<ArrayList<Float>>>();
    private static ArrayList<ArrayList<Float>> coordX = new ArrayList<ArrayList<Float>>();

    private static  ArrayList<ArrayList<ArrayList<String>>> rawY = new  ArrayList<ArrayList<ArrayList<String>>>();
    private static  ArrayList<ArrayList<Long>> rawX = new  ArrayList<ArrayList<Long>>();

    private static ArrayList<ArrayList<Float>> maximums = new ArrayList<ArrayList<Float>> ();
    private static ArrayList<Integer> min_y = new ArrayList<Integer>();
    private static ArrayList<Integer> max_y = new ArrayList<Integer>();


    private Paint rPaint = new Paint();
    private Paint bPaint = new Paint();
    private Paint prevPaint = new Paint();
    private Paint sidePaint = new Paint();
    private Paint legendPaint = new Paint();
    private Paint textLegendPaint = new Paint();
    private Paint windowPrevPaint = new Paint();
    private Paint datePaint = new Paint();
    private Paint varPaint = new Paint();
    private Paint nightPaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint varPrev = new Paint();


    RectF[] base = new RectF[graphNumber];
    RectF[] prev = new RectF[graphNumber];
    int[] windowPrevLeft = new int[graphNumber];
    int[] windowPrevRight = new int[graphNumber];
    RectF[] windowPrevRect = new RectF[graphNumber];

    int min_dx = 100;

    int left = 10;
    int top;
    int right;
    int bottom;
    float[] x_start = new float[graphNumber];
    int finger = 35;

    boolean is_init = true;

    Rect[][] legendButtons = new Rect[graphNumber][];
    boolean[][] flagLegendBinClicked = new boolean[graphNumber][];

    float[] horiz_max = new float[graphNumber];
    float[] relative_max_y = new float[graphNumber];
    float[] local_max = new float[graphNumber];


    private static boolean[] is_stacked = new boolean[graphNumber];
    private static boolean[] is_precentage = new boolean[graphNumber];
    private static boolean[] is_y_scaled = new boolean[graphNumber];

    float[] compensate_bar;


    public toDisplay(Context contex) {
        super(contex);
    }

    public toDisplay(Context contex, AttributeSet attrs){
        super(contex, attrs);
    }

    public toDisplay(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        //Log.d("DGR","Construct");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //dispLog.info("onDraw!");

        textLegendPaint.setTextSize(textLegendSize);

        legendPaint.setStyle(Paint.Style.FILL);

        top = ScrollingActivity.screenHeight / 30;
        right = ScrollingActivity.screenWidth - left;
        bottom = ScrollingActivity.screenHeight/2;


        float[] scaleXGraph = new float[graphNumber];
        float[] startViewGraph = new float[graphNumber];
        float[] endViewGraph = new float[graphNumber];

        int previewThickness = 3 * top;

        int butSpace = 0;
     //   int topSmall = bottom;
     //   int bottomSmall = topSmall + previewThickness;

        int blok = (ScrollingActivity.screenHeight/2 + 2*previewThickness);


        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setAlpha(26);
        paint.setStrokeWidth(2);

        //cycle graph obj's
        for(int i = 1; i <= graphNumber; i++) {
            //dispLog.info("onDraw!" + stepRects);
            //dispLog.info("TYPE: " + i + " " + types[i - 1]);
            //dispLog.info("is_stacked: " + i + " " + is_stacked[i - 1]);
            //dispLog.info("is_precentage: " + i + " " + is_precentage[i - 1]);
            //dispLog.info("is_y_scaled: " + i + " " + is_y_scaled[i - 1]);


            float[][] fArr = new float[countLines[i - 1]][];
            float[][] fPrew = new float[countLines[i - 1]][];
            float[][] fXPrew = new float[countLines[i - 1]][];


            int dx = windowPrevRight[i - 1] - windowPrevLeft[i - 1];
            if(dx < min_dx){
                windowPrevRight[i - 1] += 100;
                windowPrevLeft[i - 1] -= 100;
            }

            if (windowPrevRight[i - 1] > right) {
                windowPrevRight[i - 1] = right;
                windowPrevLeft[i - 1] = right - dx;
            }
            if (windowPrevLeft[i - 1] < left) {
                windowPrevLeft[i - 1] = left;
                windowPrevRight[i - 1] = left + dx;
            }


            int bottomSmall = bottom + stepRects + previewThickness;
            int topSmall = bottom + stepRects + top;

            base[i - 1] = new RectF();
            prev[i - 1] = new RectF();

            prev[i - 1].set(left, topSmall, right, bottomSmall);
            canvas.drawRect(prev[i - 1], paint);

            //first creation
            if(is_init){
                windowPrevRight[i - 1] = right;
                windowPrevLeft[i - 1] = left + 3 * (right - left) / 5;

                legendButtons[i - 1] = new Rect[countLines[i - 1]];
                flagLegendBinClicked[i - 1] = new boolean[countLines[i - 1]];

                for(int m = 0; m < legendButtons[i - 1].length; m++){
                    legendButtons[i - 1][m] = new Rect();
                }

                horiz_max[i - 1] = 1.0f;
                relative_max_y[i - 1] = -1.0f;
            }


            //count active lines now
            int cActiveLines = 0;
            int[] nowActive = new int[countLines[i - 1]];
            int[] whatNotActive = new int[countLines[i - 1]];

            for(int j = 0; j < countLines[i - 1]; j++){

                nowActive[j] = cActiveLines;

                if(!flagLegendBinClicked[i - 1][j]){
                    cActiveLines++;
                    whatNotActive[j] = 0;
                }else{
                    whatNotActive[j] = 1;
                }
            }


         /*   if(is_stacked[i - 1]) {
                relative_max_y[i - 1] = 0.0f;
            }*/
            //dynamic change Y coord max value
            for(int j = 0; j < maximums.get(i - 1).size(); j++){

                if(!flagLegendBinClicked[i - 1][j]) {

                    //if(!is_stacked[i - 1]) {

                        if (maximums.get(i - 1).get(j) > relative_max_y[i - 1]) {

                            relative_max_y[i - 1] = maximums.get(i - 1).get(j);

                            //horiz_max = maximums.get(i);
                            //dispLog.info("Relative max Y: " + relative_max_y[3]);
                        }
                    //}else{
                    //    relative_max_y[i - 1] += maximums.get(i - 1).get(j);
                    //}
                }
            }


            windowPrevRect[i - 1] = new RectF();
            windowPrevRect[i - 1].set(windowPrevLeft[i - 1], topSmall, windowPrevRight[i - 1], bottomSmall);

            // set scale params by horizontal projaction
            startViewGraph[i - 1] = (float) (windowPrevRect[i - 1].left) / (float) (right - left);
            endViewGraph[i - 1] = (float) (windowPrevRect[i - 1].right) /  (float) (right - left);
            scaleXGraph[i - 1] = (float) (windowPrevRect[i - 1].right - windowPrevRect[i - 1].left) / (right - left);

         /*   for(int z = 0; z < graphNumber; z++){
                dispLog.info("startViewGraph " + z + " = " + startViewGraph[z]);
            }*/
            //dispLog.info("windowPrevLeft[i - 1]" + windowPrevLeft[i - 1]);

            //gray borders
            prevPaint.setAlpha(30);
            windowPrevPaint.setStyle(Paint.Style.FILL);
            windowPrevPaint.setColor(Color.LTGRAY);
            canvas.drawRect(left, prev[i - 1].top, windowPrevRect[i - 1].left, prev[i - 1].bottom, windowPrevPaint);
            canvas.drawRect(windowPrevRect[i - 1].right, prev[i - 1].top, right, prev[i - 1].bottom, windowPrevPaint);

            //baseRect.set(left, topSmall, right, bottomSmall);

            //canvas.drawRect(prev, prevPaint);

            //green window for preview
            windowPrevPaint.setStrokeWidth(3);
            windowPrevPaint.setColor(Color.GREEN);
            windowPrevPaint.setAlpha(60);
            //canvas.drawRect(windowPrevRect[i - 1], windowPrevPaint);

            //scaleXGraph[i - 1] = 1.0f;

            if(endViewGraph[i - 1] > 1){
                endViewGraph[i - 1] = 1.0f;
            }
            if(startViewGraph[i - 1] < 0){
                startViewGraph[i - 1] = 0.0f;
            }


            base[i - 1].set(left, top + stepRects, right,bottom + stepRects);
            canvas.drawRect(base[i - 1], paint);

            //gray lines behind graphs
            for(int j = 1; j <= back_lines; j++) {
                canvas.drawLine(left, stepRects + top + j * (bottom - top) / back_lines, right,
                        stepRects + top + j * (bottom - top) / back_lines, paint);
            }

            //float[][] temp_y_end = new float[countLines[i - 1]][];

            //dispLog.info("countLines --- " + countLines[i - 1]);
            int c_compensate_lines = 0;

            for(int k = 0; k < countLines[i - 1]; k++){
                //x,y,x,y,x,y in the paths
                fArr[k] = new float[coordinates.get(i - 1).get(k).size() + coordX.get(i - 1).size()];
                fPrew[k] = new float[coordinates.get(i - 1).get(k).size()];
                fXPrew[k] = new float[coordX.get(i - 1).size()];
            }


            //lines on graph
            for(int k = 0; k < countLines[i - 1]; k++){

                Path graphPath = new Path();
                Path smallPath = new Path();

                try {
                    if (flagLegendBinClicked[i - 1][k] && flagLegendBinClicked[i - 1][k - 1]) {
                        c_compensate_lines++;
                    }else {

                        if (flagLegendBinClicked[i - 1][k]) {
                            c_compensate_lines = 0;
                        }
                    }

                }catch (RuntimeException re){
                    re.printStackTrace();
                }
                //temp_y_end[k] = new float[coordinates.get(i - 1).get(k).size()];


                //main paint
                varPaint.setColor(colors.get(i - 1).get(k));

                varPrev.setColor(colors.get(i - 1).get(k));
                varPrev.setAlpha(100);

                if( types[i - 1].equals("line") ) {
                    varPaint.setStyle(Paint.Style.STROKE);
                }else{
                    varPaint.setStyle(Paint.Style.FILL);
                }

                varPaint.setStrokeWidth(3);

                if( types[i - 1].equals("bar") ) {
                    varPaint.setStrokeWidth( (( (float)(right - left) )/( (float)coordX.get(i - 1).size()) ) / ((float)scaleXGraph[i - 1]) + 1);
                    //dispLog.info("StrokeWidth: " + i + " = " + (( (float)(right - left) )/( (float)coordX.get(i - 1).size()) ) / ((float)scaleXGraph[i - 1]));
                    varPrev.setStrokeWidth(( (float)(right - left) )/( (float)coordX.get(i - 1).size())  + 1);
                }

               // if(is_init) {
                 //   compensate_bar = new float[fArr[k].length];
                //}

                //prepare to draw paths
                for(int q = 0, s = 0, t = 0; q < fArr[k].length; q++){


                /*    if(k == 0){
                        temp_y_end[k][q] = bottom + stepRects;
                    }*/

                    //x in the paths
                    if(q % 2 == 0){
                        fArr[k][q] = (float) left + ((float) left / (float)scaleXGraph[i - 1] + (float)coordX.get(i - 1).get(q / 2) * ((float) right - (float) left)/ (float)scaleXGraph[i - 1])
                                - ((float)(right - left))*(startViewGraph[i - 1] * coordX.get(i - 1).get(coordX.get(i - 1).size() - 2) / ((float)scaleXGraph[i - 1]));

                        fXPrew[k][t] = ((float) left + coordX.get(i - 1).get(q / 2) * ((float) right - (float) left));
                        //dispLog.info("fXPrew[t] = " + fXPrew[t]);
                        t++;
                        //myLog.info("fArr[j]: " + fArr[j]);

                    }else{
                        //add y to fArr
                        fArr[k][q] += ((float) bottom + ((float) top - (float) bottom) * coordinates.get(i - 1).get(k).get(q / 2) / (horiz_max[i - 1]) + stepRects);
                        fPrew[k][s] = ((float) bottomSmall + ((float) topSmall - (float) bottomSmall) * coordinates.get(i - 1).get(k).get(q / 2) / (relative_max_y[i - 1]));

                        //dispLog.info("fPrew[s] = " + fPrew[s]);
                        s++;

                        //graph with bars
                        if(types[i - 1].equals("bar") /*&& !flagLegendBinClicked[i - 1][k]*/) {

                            try{
                                if(flagLegendBinClicked[i - 1][k - 1]) {

                                    if (k - c_compensate_lines < 2) {

                                        for(int r = k; r < countLines[i - 1]; r++) {
                                            fArr[r][q] += bottom + stepRects - fArr[k - 1][q];
                                        }
                                        //compensate_bar[q] += bottom + stepRects - fArr[k - 1][q];
                                        //canvas.drawLine(fArr[k][q - 1], fArr[k][q], fArr[k][q - 1], bottom + stepRects, varPaint);
                                        //canvas.drawLine(fXPrew[k][t - 1], fPrew[k][s - 1], fXPrew[k][t - 1], bottomSmall, varPrev);
                                    }
                                        else{
                                            for(int r = k; r < countLines[i - 1]; r++) {
                                                fArr[r][q] += fArr[k - 2 - c_compensate_lines][q] - fArr[k - 1][q];
                                            }
                                            //compensate_bar[q] += fArr[k - 2][q] - fArr[k - 1][q];
                                            //canvas.drawLine(fArr[k][q - 1], fArr[k][q], fArr[k][q - 1], fArr[k - 2][q], varPaint);
                                            //canvas.drawLine(fXPrew[k][t - 1], fPrew[k][s - 1], fXPrew[k][t - 1], bottomSmall, varPrev);
                                        }

                                        if(!flagLegendBinClicked[i - 1][k]) {
                                            //dispLog.info("Compensate: " + c_compensate_lines);
                                            canvas.drawLine(fArr[k][q - 1], fArr[k][q], fArr[k][q - 1], fArr[k - 2 - c_compensate_lines][q], varPaint);
                                            canvas.drawLine(fXPrew[k][t - 1], fPrew[k][s - 1], fXPrew[k - 1][t - 1], bottomSmall, varPrev);
                                        }

                                }else {
                                    //dispLog.info("Compensate: " + compensate_bar[q]);

                                    if(!flagLegendBinClicked[i - 1][k]) {
                                        canvas.drawLine(fArr[k][q - 1], fArr[k][q], fArr[k][q - 1], fArr[k - 1][q], varPaint);
                                        canvas.drawLine(fXPrew[k][t - 1], fPrew[k][s - 1], fXPrew[k - 1][t - 1], bottomSmall, varPrev);
                                    }
                                }

                            }catch(RuntimeException re){
                                //dispLog.info("catch!");
                                if(!flagLegendBinClicked[i - 1][k]) {
                                    canvas.drawLine(fArr[k][q - 1], fArr[k][q], fArr[k][q - 1], bottom + stepRects, varPaint);
                                    canvas.drawLine(fXPrew[k][t - 1], fPrew[k][s - 1], fXPrew[k][t - 1], bottomSmall, varPrev);
                                }
                            }

                         /*   finally {
                                if(!flagLegendBinClicked[i - 1][k]) {
                                    if(fArr[k][q] < horiz_max[i - 1]) {
                                        horiz_max[i - 1] = fArr[k][q];
                                    }
                                }
                            }*/

                        }


                        //graph with lines
                        if(types[i - 1].equals("line") || types[i - 1].equals("area")) {
                            if (q != 1) {
                                graphPath.lineTo(fArr[k][q - 1], fArr[k][q]);
                                smallPath.lineTo(fXPrew[k][t - 1], fPrew[k][s - 1]);
                            }
                        }
                    }

                    //start & end of lines
                    if(types[i - 1].equals("line") || types[i - 1].equals("area")) {

                        if (types[i - 1].equals("line")) {
                            if (q == 1 || q == fArr[k].length - 1) {
                                graphPath.moveTo(fArr[k][q - 1], fArr[k][q]);
                                smallPath.moveTo(left - 100, bottomSmall - (bottomSmall - topSmall) / 2);
                                //smallPath.lineTo(fXPrew[l - 1], fPrew[k - 1]);
                            }
                        }

                        if (types[i - 1].equals("area")) {

                                if (q == 1) {
                                    graphPath.moveTo(left, bottom + stepRects);
                                    smallPath.moveTo(left, bottomSmall);
                                }
                                if (q == fArr[k].length - 1) {
                                    graphPath.lineTo(fArr[k][q - 2], bottom + stepRects);
                                    graphPath.lineTo(fArr[k][0], bottom + stepRects);
                                    smallPath.lineTo(right, bottomSmall);
                                    smallPath.lineTo(left, bottomSmall);
                                }

                        }
                    }

                }


                legendPaint.setColor(colors.get(i - 1).get(k));
                textLegendPaint.setColor(Color.WHITE);


                smallPath.close();
                graphPath.close();


                //draw buttons for managing lines
                if(flagLegendBinClicked[i - 1][k]){
                    legendPaint.setColor(Color.LTGRAY);
                    textLegendPaint.setColor(Color.BLACK);

                } else {
                    //draw graphs
                    //if(types[i - 1].equals("line") || types[i - 1].equals("area")) {
                        varPaint.setAlpha(255);
                        canvas.drawPath(graphPath, varPaint);
                        varPaint.setAlpha(100);
                  /*      if(types[i - 1].equals("bar")){
                            varPaint.setStrokeWidth(( (float)(right - left) )/( (float)coordX.get(i - 1).size())  + 1);
                        }*/
                        canvas.drawPath(smallPath, varPaint);
                    //}
                }

                //support up to 7 buttons(for on\of line overview) inclusive
                if(k < 2){
                    legendButtons[i - 1][k].set(padding, 2*top/3 + bottomSmall + padding + (k) * (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap),
                            ScrollingActivity.screenWidth / 3 - padding,
                            2*top/3 + bottomSmall + (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap) * (k + 1));

                    canvas.drawRect(legendButtons[i - 1][k], legendPaint);
                    drawRectText(legends.get(i - 1).get(k), canvas, legendButtons[i - 1][k], textLegendSize, 0, textLegendPaint, 8*padding, 255);

                    butSpace = 2*(2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap);
                }

                if(k >= 2 && k < 4){
                    legendButtons[i - 1][k].set(padding + ScrollingActivity.screenWidth / 3,
                            2*top/3 + bottomSmall + padding + (k - 2) * (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap),
                            2*(ScrollingActivity.screenWidth / 3 - padding),
                            2*top/3 + bottomSmall + (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap) * (k - 1));

                    canvas.drawRect(legendButtons[i - 1][k], legendPaint);
                    drawRectText(legends.get(i - 1).get(k), canvas, legendButtons[i - 1][k], textLegendSize, 0, textLegendPaint, 8*padding, 255);
                }

                if(k >= 4 && k < 6) {
                    legendButtons[i - 1][k].set(padding + 2 * ScrollingActivity.screenWidth / 3,
                            2*top/3 + bottomSmall + padding + (k - 4) * (2 * padding / 3 + 3*textLegendSize/2 + 2 * textLittleGap),
                            ScrollingActivity.screenWidth - padding,
                            2*top/3 + bottomSmall + (2 * padding / 3 + 3*textLegendSize/2 + 2 * textLittleGap) * (k - 3));

                    canvas.drawRect(legendButtons[i - 1][k], legendPaint);
                    drawRectText(legends.get(i - 1).get(k), canvas, legendButtons[i - 1][k], textLegendSize, 0, textLegendPaint, 8*padding, 255);
                }

                if(k == 6){
                    legendButtons[i - 1][k].set(padding, 2*top/3 + bottomSmall + padding + (k - 4) * (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap),
                            ScrollingActivity.screenWidth / 3 - padding,
                            2*top/3 + bottomSmall + (2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap) * (k - 3));

                    canvas.drawRect(legendButtons[i - 1][k], legendPaint);
                    drawRectText(legends.get(i - 1).get(k), canvas, legendButtons[i - 1][k], textLegendSize, 0, textLegendPaint, 8*padding, 255);

                    butSpace = 3*(2*padding/3 + 3*textLegendSize/2 + 2*textLittleGap);
                }
            }


            //horizontal normalization Y
            local_max[i - 1] = -1.0f;
            float stacked_max_buf = 0.0f;
         /*   if(is_stacked[i - 1]) {
                local_max[i - 1] = 0.0f;
            }*/

            for(int j = 0; j < countLines[i - 1]; j++) {



                for (int n = (int) (startViewGraph[i - 1] * coordX.get(i - 1).size()); n < endViewGraph[i - 1] * coordX.get(i - 1).size(); n++) {

                    if(!flagLegendBinClicked[i - 1][j]) {

                        if(!is_stacked[i - 1]) {

                            if (coordinates.get(i - 1).get(j).get(n) > local_max[i - 1]) {
                                local_max[i - 1] = coordinates.get(i - 1).get(j).get(n);
                            }
                        }else{
                            if (coordinates.get(i - 1).get(j).get(n) - stacked_max_buf > local_max[i - 1]) {
                                local_max[i - 1] = coordinates.get(i - 1).get(j).get(n) - stacked_max_buf;
                            }
                        }
                    }

                    if(is_stacked[i - 1] && flagLegendBinClicked[i - 1][j]){
                        if(coordinates.get(i - 1).get(j).get(n) > stacked_max_buf){
                            try{
                                //find maximum
                                stacked_max_buf = coordinates.get(i - 1).get(j).get(n) - coordinates.get(i - 2).get(j).get(n);
                            }catch(RuntimeException re){
                                stacked_max_buf = coordinates.get(i - 1).get(j).get(n);
                            }
                        }
                    }

                    //if(is_stacked[i - 1] && i > 1) {
                    //    local_max[i - 1] += local_max[i - 2];
                    //}
                    //   if(i == 4)
                  //      dispLog.info("n = " + n);
                }

            /*    if(is_stacked[i - 1] && flagLegendBinClicked[i - 1][j]){
                    dispLog.info("local_max: " + local_max[i - 1]);
                    try {
                        local_max[i - 1] -= stacked_max_buf[j] - stacked_max_buf[j - 1];
                    }catch (RuntimeException re){
                        local_max[i - 1] -= stacked_max_buf[j];
                    }
                    dispLog.info("stacked_max_buf: " + stacked_max_buf[j]);
                    //dispLog.info("local_max: " + local_max[i - 1]);
                }*/
            }

         /*   dispLog.info("-----horiz_max: " + horiz_max[3]);
            dispLog.info("local_max: " + local_max[3]);
            dispLog.info("startViewGraph: " + startViewGraph[3]);
            dispLog.info("endViewGraph: " + endViewGraph[3]);*/

            //dispLog.info("-----------");

            if(horiz_max[i - 1] != local_max[i - 1] /*&& !is_init*/) {
                horiz_max[i - 1] = local_max[i - 1];
                //f = true;
                postInvalidate();
            }

            canvas.drawRect(windowPrevRect[i - 1], windowPrevPaint);

            stepRects += blok + butSpace;
        }

        stepRects = 0;
        is_init = false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        //dispLog.info("dispatchTouchEvent!");
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent (MotionEvent event){

        //dispLog.info("Touch!");
        //ScrollingActivity.onTouchEvent(event);

        for(int i = 1; i <= graphNumber; i++) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && prev[i - 1].contains(event.getX(), event.getY())) {
            //dispLog.info("Down!");
                x_start[i - 1] = event.getX();
            }
        }

        //if(event.getAction() == MotionEvent.ACTION_MOVE) {
            //dispLog.info("Move!");

            for (int i = 1; i <= graphNumber; i++) {
                if(event.getAction() == MotionEvent.ACTION_MOVE && (prev[i - 1].contains(event.getX(), event.getY()) /*||
                        prev[i - 1].contains(event.getX() - 40, event.getY())*/ )){

                    if (Math.abs(event.getX() - windowPrevRect[i - 1].right) <= 4 * finger) {
                    if (x_start[i - 1] > event.getX() && windowPrevRight[i - 1] - windowPrevLeft[i - 1] > 6 * finger) {
                        //myLog.info("GET_X > START = " + x_start);
                        windowPrevRight[i - 1] = (int) event.getX() - finger;
                        postInvalidate();
                        //break;
                    }
                    if (x_start[i - 1] < event.getX() && windowPrevRight[i - 1] < right - 1) {
                        //myLog.info("GET_X < START = " + x_start);
                        windowPrevRight[i - 1] = (int) event.getX() + finger;
                        postInvalidate();
                        //break;
                    }
                }

                if (Math.abs(event.getX() - windowPrevRect[i - 1].left) <= 4 * finger) {
                    if (x_start[i - 1] < event.getX() && windowPrevRight[i - 1] - windowPrevLeft[i - 1] > 6 * finger) {
                        windowPrevLeft[i - 1] = (int) event.getX() + finger;
                        postInvalidate();
                        //break;
                    }

                    if (x_start[i - 1] > event.getX() && windowPrevLeft[i - 1] > left + 1) {
                        windowPrevLeft[i - 1] = (int) event.getX() - finger;
                        postInvalidate();
                        //break;
                    }
                }
                //   }

                if (windowPrevRect[i - 1].contains((int) event.getX(), (int) event.getY())) {

                    //dispLog.info("Touch in the windowPrevRect!");

                    int dx = windowPrevRight[i - 1] - windowPrevLeft[i - 1];
                    int[] dPointer = new int[graphNumber];
                    for(int p = 0; p < graphNumber; p++){
                        dPointer[p] = 0;
                    }


                    if (event.getX() <= windowPrevRight[i - 1] - 2 * finger && event.getX() >= windowPrevLeft[i - 1] + 2 * finger) {

                        windowPrevLeft[i - 1] = (int) event.getX() - dx / 2 + dPointer[i - 1];
                        windowPrevRight[i - 1] = (int) event.getX() + dx / 2 + dPointer[i - 1];

                        //dispLog.info("windowPrevLeft" + (i - 1) + " = " + windowPrevLeft[i - 1]);
                        //dispLog.info("windowPrevRight" + (i - 1) + " = " + windowPrevRight[i - 1]);

                    }

                    //dispLog.info("left = " + left);
                    //dispLog.info("right = " + right);


                /*    if (windowPrevRight[i - 1] > right) {
                        windowPrevRight[i - 1] = right;
                        windowPrevLeft[i - 1] = right - dx;

                        return false;
                    }
                    if (windowPrevLeft[i - 1] < left) {
                        windowPrevLeft[i - 1] = left;
                        windowPrevRight[i - 1] = left + dx;

                        return false;
                    }*/

                    postInvalidate();
                    //break;
                }
            }


            for(int j = 0; j < legendButtons[i - 1].length; j++) {
                if (legendButtons[i - 1][j].contains((int) event.getX(), (int) event.getY())){
                    flagLegendBinClicked[i - 1][j] = !flagLegendBinClicked[i - 1][j];
                    //relative_max_y[i - 1] = -1.0f;
                    //dispLog.info("-----horiz_max: " + horiz_max[i - 1]);

                    relative_max_y[i - 1] = -1.0f;

                    postInvalidate();
                    return false;
                }
            }

        }


        return true;
    }


    public static void setFlags(boolean stack, boolean precentage, boolean y_scaled, Integer numCurrentGraph){

        is_stacked[numCurrentGraph] = stack;
        is_precentage[numCurrentGraph] = precentage;
        is_y_scaled[numCurrentGraph] = y_scaled;

    }


    public static void setTypes(String[] str){
        types = Arrays.copyOf(str, str.length);
    }


 /*   public void setBaseRects(int  step){
        stepRects = step * blok;
    }*/

    public static void setLegends(ArrayList<ArrayList<String>> s){
        legends.addAll(s);
    }

    public static void setColors(ArrayList<ArrayList<String>> s){

        for(int i = 0; i < graphNumber; i++) {
            countLines[i] = s.get(i).size();

            colors.add(new ArrayList<Integer>());
            for (int j = 0; j < s.get(i).size(); j++) {
                colors.get(i).add(Color.parseColor(s.get(i).get(j)));
            }
        }
        //myLog.info("setColors " + colors.size());
    }


    public static void setCoordinates(ArrayList<BigInteger> aX, BigInteger bInt, BigInteger minX, ArrayList<ArrayList<Integer>> arr,
                                      Integer maxY, Integer minY, Integer numCurrentGraph){

        //myLog.info("MIN :" + minX);
        //myLog.info("MAX :" + bInt);
        max_y.add(maxY);
        min_y.add(minY);


        rawX.add(new ArrayList<Long>());
        coordX.add(new ArrayList<Float>());
        for(int i = 0; i < aX.size(); i++){
            coordX.get(numCurrentGraph).add((aX.get(i).floatValue() - minX.floatValue()) / (bInt.floatValue() - minX.floatValue()));
            rawX.get(numCurrentGraph).add(aX.get(i).longValue());
            //myLog.info("KO :" + coordX.get(i));
        }


        coordinates.add(new ArrayList<ArrayList<Float>>());
        rawY.add(new ArrayList<ArrayList<String>>());
        for(int i = 0; i < arr.size(); i++){
            float temp = 0.0f;

            coordinates.get(numCurrentGraph).add(new ArrayList<Float>());
            rawY.get(numCurrentGraph).add(new ArrayList<String>());
            for(int j = 0; j < arr.get(i).size(); j++){

                //normalize
                if(!is_stacked[numCurrentGraph] || (i == 0)) {
                    coordinates.get(numCurrentGraph).get(i).add((Float.valueOf(arr.get(i).get(j))) /
                            (Float.valueOf(maxY)));
                }else{
                    coordinates.get(numCurrentGraph).get(i).add( ((Float.valueOf(arr.get(i).get(j))) /            // is_stacked
                            (Float.valueOf(maxY)) ) + coordinates.get(numCurrentGraph).get(i - 1).get(j));
                }

                rawY.get(numCurrentGraph).get(i).add(String.valueOf(arr.get(i).get(j)));

                if(coordinates.get(numCurrentGraph).get(i).get(j) > temp){
                    temp = coordinates.get(numCurrentGraph).get(i).get(j);
                }
            }

            maximums.add(new ArrayList<Float>());
            maximums.get(numCurrentGraph).add(temp);
            //dispLog.info("Max " + i + " :" + maximums.get(i));
        }

    }


    private void drawRectText(String text, Canvas canvas, Rect r, int size, int align, Paint textP, int kantik, int alpha) {

        float temp = 0;
        textP.setTextSize(size);
        textP.setAlpha(alpha);

        if(align == 0) {
            temp = r.exactCenterX();
            textP.setTextAlign(Paint.Align.CENTER);
        }

        if(align > 0){
            temp = r.right;
            textP.setTextAlign(Paint.Align.RIGHT);
        }

        if(align < 0){
            temp = r.left;
            textP.setTextAlign(Paint.Align.LEFT);
        }

        int width = r.width();

        int numOfChars = textP.breakText(text,true,width,null);
        int start = (text.length()-numOfChars)/2;
        canvas.drawText(text,start,start+numOfChars,temp,r.top + 2 * kantik / 3 + padding,textP);
    }

}

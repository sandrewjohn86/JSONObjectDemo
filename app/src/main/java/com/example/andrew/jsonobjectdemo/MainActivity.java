package com.example.andrew.jsonobjectdemo;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    BarChart barChart;
    JSONResponseHandler responseHandler = new JSONResponseHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChart = (BarChart) findViewById(R.id.barChart);
    }

    //call when button is clicked
    public void displayData(View v){
        Log.i("Button", "Clicked");
        new HttpGetTask().execute();
    }

    //a method that returns a List of BarEntry type
    public List<BarEntry> extractPopl(List<String> arrayList){
        List<BarEntry> yVals = new ArrayList<>();
        int j = 0;
        for(int i = 1 ; i < arrayList.size()/2 ; i = i + 2){
            yVals.add(new BarEntry(Float.parseFloat(arrayList.get(i)),j));
            j++;
        }
        return yVals;
    }

    //a method that returns a List of String type
    public List<String> extractDate(List<String> arrayList){
        List<String> xVals = new ArrayList<>();
        for(int i = 0 ; i < arrayList.size()/2 ; i = i + 2){
            xVals.add(arrayList.get(i));
        }
        return xVals;
    }

    //Generics
    //1. Type of reference(s) passed to doInBackground()
    //2. Type of reference passed to onProgressUpdate()
    //3. Type of reference returned by doInBackground()
    //4. value passed to onPostExecute()
    private class HttpGetTask extends AsyncTask<Void,Void,List<String>>{

        private static final String URL ="http://api.worldbank.org/countries/grd/indicators/SP.POP.TOTL?format=json";
        AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

        @Override
        protected List<String> doInBackground(Void... params) {
            HttpGet request = new HttpGet(URL);
            try {
                return mClient.execute(request, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (null != mClient)
                mClient.close();

            List<String> xVals;
            List<BarEntry> yVals;

            xVals = extractDate(result);
            yVals = extractPopl(result);

            //print to logCat
           Log .i("xVals", xVals.toString());
            Log.i("yVals", yVals.toString());

            BarDataSet barDataSet = new BarDataSet(yVals, "Population");

            XAxis xAxis = barChart.getXAxis();
            xAxis.setLabelRotationAngle(-90f);

            BarData theData = new BarData(xVals, barDataSet);
            barChart.setData(theData);
            barChart.setTouchEnabled(true);
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
            barChart.invalidate();

        }
    }

    private class JSONResponseHandler implements ResponseHandler<List<String>> {

        private static final String POPULATION_TAG = "value";
        private static final String DATE_TAG = "date";;

        @Override
        public List<String> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            List<String> result = new ArrayList<String>();
            JSONArray records;

            String JSONResponse = new BasicResponseHandler().handleResponse(response);
            try {
                JSONArray jsonArray = new JSONArray(JSONResponse);
                records = jsonArray.getJSONArray(1);

                for (int i = 0 ; i < records.length() ; i++){
                    JSONObject value = (JSONObject) records.get(i);
                    result.add(value.getString(DATE_TAG));
                    result.add(value.getString(POPULATION_TAG));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}

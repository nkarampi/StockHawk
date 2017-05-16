package com.udacity.stockhawk.details;

import android.content.Intent;
import android.database.Cursor;
import android.os.DropBoxManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class DetailActivity extends AppCompatActivity {

    private TextView tv_stockName;
    //private TextView tv;

    private String symbol;
    private String history;

    private ArrayList<String> prices;
    private ArrayList<Entry> entries;
    private List<String> labels;

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        symbol = intent.getStringExtra("symbol_key");

        prices = new ArrayList<String>();
        entries = new ArrayList<>();
        labels = new ArrayList<String>();

        tv_stockName = (TextView) findViewById(R.id.tv_stock_title);

        String chartDescription = getString(R.string.chart_description,symbol);
        tv_stockName.setContentDescription(chartDescription);

       // tv = (TextView) findViewById(R.id.tv_stock_history);

        tv_stockName.setText(symbol);

        lineChart = (LineChart) findViewById(R.id.lineChart);

        loadGraph();

    }

    private void loadGraph() {

        boolean found = false;
        Cursor cursor = getContentResolver().query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);

        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
                String dbSymbol = cursor.getString(symbolColumn);
                if (symbol.equals(dbSymbol)) {
                    int historyIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
                    //int historyIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE);
                    history = cursor.getString(historyIndex);
                    found = true;
                    break;

                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        /*
        if (found) {
            tv.setText(history);
        }
        else
            tv.setText("Not found");*/


        StringTokenizer st = new StringTokenizer(history, "\n,");
        while (st.hasMoreTokens()) {

            String quoteHistory = st.nextToken();
            long quoteInMillis = Long.parseLong(quoteHistory);
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String dateString = formatter.format(new Date(quoteInMillis));
            labels.add(dateString);

            String quotePrice = st.nextToken();
            prices.add(quotePrice);
        }

       /* for (int i=prices.size()-1; i>=0;i--) {
            String qPrice = prices.get(i);
            float quotePriceFloat = Float.parseFloat(qPrice);
            entries.add(new Entry(i, quotePriceFloat));
        }*/
        Collections.reverse(prices);
        for (int i = 0;i <prices.size()-1;i++){
            String qPrice = prices.get(i);
            float quotePriceFloat = Float.parseFloat(qPrice);
            entries.add(new Entry(i, quotePriceFloat));
        }

        Collections.reverse(labels);

        LineDataSet dataset = new LineDataSet(entries, "Price of Stock");
       // LineDataSet labelsDataset = new LineDataSet(labels, "Days");
        LineData data = new LineData(dataset);
        //dataset.setColors(ColorTemplate.COLORFUL_COLORS);
       // dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);
        //dataset.setColor();
        //lineChart.getAxisRight().setInverted(true);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setTextColor(getResources().getColor(R.color.text_color));
        lineChart.getXAxis().setTextSize(getResources().getDimension(R.dimen.dimen_2sp));

        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_color));
        lineChart.getAxisRight().setTextColor(getResources().getColor(R.color.text_color));
        lineChart.getLegend().setTextColor(getResources().getColor(R.color.text_color));

        lineChart.setData(data);

        lineChart.getDescription().setText("");
        lineChart.setDrawGridBackground(true);
        lineChart.setGridBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //lineChart.setcolor
        //lineChart.setDrawBorders(true);
        //lineChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        //setBorderWidth(float width):
        lineChart.setContentDescription(getString(R.string.linechart_description,symbol));
        lineChart.animateY(1000);



    }

}

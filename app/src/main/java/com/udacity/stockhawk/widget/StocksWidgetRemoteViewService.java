package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Binder;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.details.DetailActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StocksWidgetRemoteViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {
            protected final DecimalFormat dollarFormat=(DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            protected final DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

            private Cursor cursor= null;

            @Override
            public void onCreate() {}

            @Override
            public void onDataSetChanged() {
                if (cursor !=null) cursor.close();

                final long token = Binder.clearCallingIdentity();
                cursor = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(token);
            }

            @Override
            public void onDestroy() {
                if (cursor != null){
                    cursor.close();
                    cursor = null;
                }

            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position)){
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                String symbol =cursor.getString(Contract.Quote.POSITION_SYMBOL);

                String price = dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));

                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                dollarFormatWithPlus.setPositivePrefix("+$");

                if (rawAbsoluteChange > 0) {
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);

                remoteViews.setTextViewText(R.id.widget_symbol,symbol);
                remoteViews.setTextViewText(R.id.widget_price,price);
                remoteViews.setTextViewText(R.id.widget_change,change);

                Context context = getApplicationContext();
                Intent fillIntent = new Intent(context, DetailActivity.class);
                fillIntent.putExtra("symbol_key",symbol);


                remoteViews.setOnClickFillInIntent(R.id.widget_list_item,fillIntent);

                return remoteViews;

            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}

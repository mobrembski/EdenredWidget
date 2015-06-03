package com.mobrembski.edenredwidget;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

class EdenredCommTask extends AsyncTask<Long, Void, Void> {

    private static final String ServerAddress = "http://www.edenred.pl/mobileapp/index.php";
    private static final String RequestSalt = "f4a6?Sta+4";
    private final RemoteViews view;
    private final Context context;
    private final AppWidgetManager manager;
    private final int widgetId;
    private final SharedPreferences sp;
    private final int width;

    public EdenredCommTask(Context ctx, int appWidgetId, int widgetWidth, int alpha) {
        context = ctx;
        width = widgetWidth;
        widgetId = appWidgetId;
        manager = AppWidgetManager.getInstance(context);
        view = MainActivity.generateLayout(ctx, appWidgetId, widgetWidth, alpha);
        sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Long... params) {
        long cardNum = params[0];
        HttpParams basicHttpParams = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(basicHttpParams, 10000);
        view.setImageViewResource(R.id.button1, R.drawable.sync);
        manager.updateAppWidget(widgetId, view);
        HttpClient client = new DefaultHttpClient(basicHttpParams);
        HttpPost post = new HttpPost(ServerAddress);
        long timestamp = System.currentTimeMillis() / 1000;
        try {
            List entityList = new ArrayList();
            entityList.add(new BasicNameValuePair("action", "balance"));
            entityList.add(new BasicNameValuePair("cards", String.valueOf(cardNum)));
            entityList.add(new BasicNameValuePair("timestamp", String.valueOf(timestamp)));
            entityList.add(new BasicNameValuePair("hash", generateHash(timestamp)));
            HttpEntity entity = new UrlEncodedFormEntity(entityList, "utf-8");
            post.setEntity(entity);

            HttpResponse httpResponse = client.execute(post);

            InputStream inputStream = httpResponse.getEntity().getContent();
            String stringResponse = convertInputStreamToString(inputStream);
            float d = (float) parseResponse(stringResponse, cardNum);
            notificationIfNeeded(cardNum, d);
            sp.edit().putFloat(widgetId + "_value", d).commit();
            String valStr = String.valueOf(d);
            if (width > 110)
                valStr += " PLN";
            view.setTextViewText(R.id.card_value, valStr);
            view.setTextColor(R.id.card_value, Color.BLACK);
            view.setTextViewText(R.id.card_id, String.valueOf(cardNum));

        } catch (Exception e) {
            view.setTextViewText(R.id.card_value,
                    context.getResources().getString(R.string.connection_error));
            view.setTextColor(R.id.card_value, Color.RED);
        } finally {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
            view.setTextViewText(R.id.updated_time, sdf.format(c.getTime()));
            view.setImageViewResource(R.id.button1, R.drawable.credit_card_icon);
            manager.updateAppWidget(widgetId, view);
        }
        return null;
    }

    private void notificationIfNeeded(long cardNum, float d) {
        float lastValue = sp.getFloat(widgetId + "_value", 0.0f);
        boolean notifyEnabled = sp.getBoolean(widgetId + "_notify", true);
        if (!notifyEnabled)
            return;
        float diff = d - lastValue;
        if (diff > 0) {
            String notifContent = context.getResources().getString(R.string.notification_content);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.credit_card_icon)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(
                            String.format(notifContent, cardNum, diff, d)))
                    .setContentTitle(context.getResources().getString(R.string.notification_title))
                    .setContentText(context.getResources().getString(R.string.tap_to_see));

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, builder.build());
        }
    }

    private static String generateHash(long timestamp) {
        String timeStr = String.valueOf(timestamp);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        digest.update(RequestSalt.getBytes());
        digest.update(timeStr.getBytes());
        byte messageDigest[] = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest)
            hexString.append(Integer.toHexString(0xFF & aMessageDigest));
        return hexString.toString();
    }

    private static double parseResponse(String response, long cardNum) throws EdenredException {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            Iterator<String> responseKeys = jsonResponse.keys();
            while (responseKeys.hasNext()) {
                String key = responseKeys.next();
                if (key.equals(String.valueOf(cardNum))) {
                    JSONObject arr = jsonResponse.getJSONObject(key);
                    double amount = arr.getDouble("amount");
                    if (amount >= 0)
                        return amount;
                    else
                        throw new EdenredException("ParseError");
                }
            }
            throw new EdenredException("CommError");
        } catch (JSONException ex) {
            throw new EdenredException("CommError");
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}

package com.nest.calamitycontrol;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

/**
 * Created by Rohan on 12/11/2016.
 */

public class HttpCall extends AsyncTask<Void, Void, Void> {

    String urlString;
    View view;

    HttpCall(String url, View view) {
        urlString = url;
        this.view = view;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Snackbar.make(view, "Reported!", LENGTH_SHORT).show();

    }

    @Override
    protected Void doInBackground(Void... voids) {
        URL url;
        try {
            url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("GET");


            OutputStream os = httpURLConnection.getOutputStream();

            BufferedWriter mBufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            mBufferedWriter.flush();
            mBufferedWriter.close();
            os.close();

            httpURLConnection.connect();
            BufferedReader mBufferedInputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inline;
            String Response = "";
            while ((inline = mBufferedInputStream.readLine()) != null) {
                Response += inline;
            }
            mBufferedInputStream.close();
            Log.d("response", Response);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}

package com.example.craig.myapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask
    extends AsyncTask<String, Void, Bitmap>
{
    private ImageView img;

    public DownloadImageTask(ImageView img)
    {
        this.img = img;
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        String url = args[0];
        Bitmap bitmap = null;

        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result)
    {
        img.setImageBitmap(result);
    }
}

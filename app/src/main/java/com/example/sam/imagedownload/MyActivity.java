package com.example.sam.imagedownload;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.google.gson.Gson;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import util.ImageTagger;

public class MyActivity extends Activity {
    AQuery aq;
    double startlat=31.988034;//31.8623;,
    double startlong= 35.898515;//35.800417;
    double finlat=31.999390;//32.118029;,
    double finlong=35.911218;//36.116274;
    int currentDownloadIndex=0;
    ArrayList<Mehrab> mehrabList;
    ArrayList<Mehrab> verMehrabList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mehrabList=new ArrayList<Mehrab>() ;
        verMehrabList=new ArrayList<Mehrab>();

        aq=new AQuery((this));
        setContentView(R.layout.activity_my);
        final EditText tok= (EditText) findViewById(R.id.tokText);
        Button goButton= (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                async_transformer(startlat, startlong,tok.getText().toString());
            }
        });
    }
    private static class GsonTransformer implements Transformer {

        public <T> T transform(String url, Class<T> type, String encoding, byte[] data, AjaxStatus status) {
            Gson g = new Gson();
            return g.fromJson(new String(data), type);
        }
    }
    public void async_transformer( final double lat, final double lon, final String tok){
        GsonTransformer t = new GsonTransformer();
        if(lat<finlat && lon<finlong){
        String url = "http://www.mihrabi.net/api/MosqueService?latitude="+String.valueOf(lat)+"&longitude="+String.valueOf(lon)+"&city=JO-AM&scenario=2&sessionToken="+tok;
        aq.transformer(t).ajax(url, Mehrab[].class,new AjaxCallback<Mehrab[]>(){
            @Override
            public void callback(String url, Mehrab[] object, AjaxStatus status) {
               // Log.d("Status",String.valueOf(status.getCode()));
                if(object!=null) {
                    Log.d("Mehrab still working", object[0].name + " i'm at " + lat + " " + lon);
                    for (Mehrab mehrab : object) {
                        boolean isThere = false;
                        if (mehrabList.size() > 0)
                            for (Mehrab myMehrab : mehrabList)
                                if (myMehrab.id == mehrab.id)
                                    isThere = true;
                        if (!isThere)
                            mehrabList.add((mehrab));

                    }
                }
                if (lat+0.018<finlat && lon<finlong){async_transformer(lat+0.018,lon,tok);}
                else if (lat+0.018>finlat && lon+0.018<finlong ) {async_transformer(startlat,lon+0.018,tok);}
                else if (lat+0.018>finlat && lon+0.018>finlong){for (Mehrab mehrab :mehrabList){
                        System.out.println(String.valueOf(mehrab.id)+" "+mehrab.name+" "+mehrab.latitude+" "+mehrab.longitude+"\n"+mehrab.mosqueGallarieses[0].imageUrl);
                    }
                     downloadFile( mehrabList.get(0));
                }}});}}

    public void downloadFile(final Mehrab thisMehrab) {
        final File ext = Environment.getExternalStorageDirectory();
        File target = new File(ext, "aquery/mhraps/"+currentDownloadIndex+" "+thisMehrab.name+" "+thisMehrab.latitude+" "+thisMehrab.longitude+".jpg");
       String url="http://www.mihrabi.net"+thisMehrab.mosqueGallarieses[0].imageUrl.replace("~","");
if (!url.contains("DefaultMosqueImage")){
        aq.download(url, target, new AjaxCallback<File>(){

            public void callback(String url, File file, AjaxStatus status) {

                if(file != null){
                    verMehrabList.add(thisMehrab);
                    Log.d("image download OK",String.valueOf(status.getCode()));
                    MarkGeoTagImage( file.getPath(),thisMehrab.latitude,thisMehrab.longitude);
                }else{
                   Log.d("image download failed",String.valueOf(status.getCode()));
                }
                currentDownloadIndex++;
                if(currentDownloadIndex<mehrabList.size())
                downloadFile(mehrabList.get(currentDownloadIndex));
                else {
                    for (Mehrab mehrab:verMehrabList){

                    }
                }
            }

        });}
        else {
    Log.d("default image", "next");
    currentDownloadIndex++;
    if(currentDownloadIndex<mehrabList.size())
        downloadFile(mehrabList.get(currentDownloadIndex));

}
    }
    public void MarkGeoTagImage(String imagePath,double lat, double lon)
    {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(lon));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(lon));
            //SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            //exif.setAttribute(ExifInterface.TAG_DATETIME,fmt_Exif.format(new Date()));
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
class GPS {
    private static StringBuilder sb = new StringBuilder(20);
    /**
     * returns ref for latitude which is S or N.
     *
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(final double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    /**
     * returns ref for latitude which is S or N.
     *
     * @param latitude
     * @return S or N
     */
    public static String longitudeRef(final double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }
    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     * 79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     *
     * @param latitude could be longitude.
     * @return
     */
    public static final String convert(double latitude) {
        latitude = Math.abs(latitude);
        final int degree = (int)latitude;
        latitude *= 60;
        latitude -= degree * 60.0d;
        final int minute = (int)latitude;
        latitude *= 60;
        latitude -= minute * 60.0d;
        final int second = (int)(latitude * 1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}

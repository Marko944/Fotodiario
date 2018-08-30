package it.unipr.marco.fotodiario.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.unipr.marco.fotodiario.utility.StorageManager;

public class Picture {
    private String fileName;
    private String desc;
    private String albumParent;
    private Date date;
    private float latitude;
    private float longitude;

    public Picture(Date date, String albumParent) {
        this.date = date;
        this.fileName = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(this.date);
        this.desc = "";
        this.albumParent = albumParent;
        this.latitude = 0F;
        this.longitude = 0F;
    }

    @Override
    public Picture clone() {
        Picture p = new Picture(this.getDate(), this.getAlbumParent());
        p.setDesc(this.desc);
        p.setLatitude(this.latitude);
        p.setLongitude(this.longitude);
        return p;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getAlbumParent() {
        return this.albumParent;
    }

    public void setAlbumParent(String albumParent) { this.albumParent = albumParent; }

    public String getFileName() {
        return this.fileName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

package it.unipr.marco.fotodiario.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Album {
    private String name;
    private String desc;
    private Date date;

    public Album(String name) {
        this.name = name;
        this.desc = "";
    }

    public Album(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public Album(String name, Date date) {
        this.name = name;
        this.desc = "";
        this.date = date;
    }

    public Album(String name, String desc, Date date) {
        this.name = name;
        this.desc = desc;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Album)) {
            return false;
        }
        Album album = (Album) o;
        //Confronta solo il nome, non puoi creare più album con lo stesso nome tanto
        return (this.name.equals(album.getName())) ? true : false;
    }

    @Override
    public Album clone() {
        return new Album(this.name,this.desc,this.date);
    }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

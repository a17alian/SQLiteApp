package org.brohede.marcus.sqliteapp;

/**
 * Created by marcus on 2018-04-25.
 */

public class Mountain {
    // You need to create proper member variables, methods, and constructors

    // These member variables should be used
    private String name;
    private String location;
    private int size;
    private String img_url;
    private String info_url;

    public Mountain (String inName, String inLocation, int inHeight, String inImg_url, String inInfo_url){
        name = inName;
        location = inLocation;
        size = inHeight;
        img_url = inImg_url;
        info_url = inInfo_url;

    }

    public Mountain(String name, String inName, int size){
        this.name = inName;
        location = "";
        size = -1;
    }

    // Metoder
    public String toString(){ return name;}

    public String info(){
        String str = name;
        str+= " is located in ";
        str+= location;
        str+= " and has a height of ";
        str+= Integer.toString(size);
        str+="m. ";

        return str;
    }


}

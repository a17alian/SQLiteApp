package org.brohede.marcus.sqliteapp;

/**
 * Created by marcus on 2018-04-25.
 */

public class Mountain {
    // You need to create proper member variables, methods, and constructors

    // These member variables should be used
    private String name;
    private String location;
    private int height;
    private String img_url;
    private String info_url;

    public Mountain (String inName, String inLocation, int inSize, String inImg_url, String inInfo_url){
        name = inName;
        location = inLocation;
        height = inSize;
        img_url = inImg_url;
        info_url = inInfo_url;

    }

    public Mountain(String inName, String inLocation, int inSize){
        this.name = inName;
        location = inLocation;
        height = inSize;
    }



    // Metoder


    @Override
    public String toString() {
        return name;
    }

    public String info(){
        String str = name;
        str+= " is located in ";
        str+= location;
        str+= " and has a height of ";
        str+= Integer.toString(height);
        str+="m. ";

        return str;
    }


}

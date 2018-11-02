package com.example.lino.asl;

public class Parse extends MainActivity {

    public static String[] Prepare_text(String Input ){
        String parse = Input.toLowerCase();
        return(parse.split("\\s+"));
    }

}

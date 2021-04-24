package com.mycompany.YouDao;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BasicPart{
    public List<String> explains;
    public String phonetic;
    @SerializedName("uk-phonetic")
    public String ukPhonetic;
    @SerializedName("us-phonetic")
    public String usPhonetic;
    @SerializedName("uk-speech")
    public String ukSpeech;
    @SerializedName("us-speech")
    public String usSpeech;
}

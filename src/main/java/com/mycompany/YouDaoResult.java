package com.mycompany;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YouDaoResult extends Result{
    public String errorCode;
    public String query;
    public List<String> translation;
    public List<WebPart> web;
    public BasicPart basic;
    public String l;
    public String dict;
    public String webdict;
    public String tSpeakUrl;
    public String speakUrl;
    public List<String> returnPhrase;

    @Override
    public List<String> getTranslation() {
        return translation;
    }

    @Override
    public List<String> getBasicTranslation() {
        return basic.explains;
    }

    @Override
    public List<String> getWebTranslation() {
        return null;
    }
}

class WebPart{
    public List<String> value;
    public String key;
}

class BasicPart{
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
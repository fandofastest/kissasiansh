package com.kissasian.sh.models;

import java.util.ArrayList;
import java.util.List;

public class EpiModel {
    String seson,epi,streamURL,serverType, imageUrl;
    public String subtitleURL;
    public List<SubtitleModel> listsubtv = new ArrayList<>();

    public List<SubtitleModel> getListsubtv() {
        return listsubtv;
    }

    public void setListsubtv(List<SubtitleModel> listsubtv) {
        this.listsubtv = listsubtv;
    }

    public List<SubtitleModel> listSub = new ArrayList<>();

    public List<SubtitleModel> getListSub() {
        return listSub;
    }

    public void setListSub(List<SubtitleModel> listSub) {
        this.listSub = listSub;
    }

    public String getSubtitleURL() {
        return subtitleURL;
    }

    public void setSubtitleURL(String subtitleURL) {
        this.subtitleURL = subtitleURL;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public String getSeson() {
        return seson;
    }

    public void setSeson(String seson) {
        this.seson = seson;
    }

    public String getEpi() {
        return epi;
    }

    public void setEpi(String epi) {
        this.epi = epi;
    }
}

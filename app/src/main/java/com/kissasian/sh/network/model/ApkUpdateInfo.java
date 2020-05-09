package com.kissasian.sh.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApkUpdateInfo {
    @SerializedName("version_code")
    @Expose
    private String versionCode;
    @SerializedName("version_name")
    @Expose
    private String versionName;
    @SerializedName("whats_new")
    @Expose
    private String whatsNew;
    @SerializedName("apk_url")
    @Expose
    private String apkUrl;
    @SerializedName("is_skipable")
    @Expose
    private boolean isSkipable;
    @SerializedName("status_intersplash")
    @Expose
    private String status_intersplash;

    @SerializedName("popimageurl")
    @Expose
    private String logourl;

    @SerializedName("deskripsipop")
    @Expose
    private String pesan;
    @SerializedName("judulpop")
    @Expose
    private String judul;

    @SerializedName("statuspop")
    @Expose
    private String statuspop;
    @SerializedName("apktv")
    @Expose
    private String apktv;

    public void setStatus_intersplash(String status_intersplash) {
        this.status_intersplash = status_intersplash;
    }

    public String getLogourl() {
        return logourl;
    }

    public void setLogourl(String logourl) {
        this.logourl = logourl;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getStatuspop() {
        return statuspop;
    }

    public void setStatuspop(String statuspop) {
        this.statuspop = statuspop;
    }

    public String getApktv() {
        return apktv;
    }

    public void setApktv(String apktv) {
        this.apktv = apktv;
    }

    public String getStatus_intersplash() {
        return status_intersplash;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public boolean isSkipable() {
        return isSkipable;
    }

    public void setSkipable(boolean skipable) {
        isSkipable = skipable;
    }
}

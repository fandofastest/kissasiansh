package com.kissasian.sh.utils;

import com.kissasian.sh.Config;
import com.kissasian.sh.network.RetrofitClient;

public class ApiResources {
    public static String statuspop,judulpop,apkpop,deskripsipop,popimageurl,apktv,apkupdate;


    public static String CURRENCY; // must be valid currency code
    public static String EXCHSNGE_RATE;
    public static double POUND_TO_NGN_EXCHANGE_RATE = 444.33;

    public static String PAYPAL_CLIENT_ID;

    // Create your Rave live keys from the Rave Dashboard and put them below when you are about to publish on Play Store
    public static String RAVE_PUBLIC_KEY;
    public static String RAVE_ENCRYPTION_KEY;
    public static String statusku,status_intersplash,inter;

    public static String PAY_STACK_PUBLIC_KEY;


    String URL = Config.API_SERVER_URL + RetrofitClient.API_URL_EXTENSION;

    String details = URL+"single_details";

    String login = URL+"login";

    String searchUrl = URL+"search";
    String favoriteUrl = URL+"favorite";

    String getAllReply = URL+"all_replay";
    String termsURL = Config.TERMS_URL;

    public String getTermsURL() {
        return termsURL;
    }

    public String getGetAllReply() {
        return getAllReply;
    }

    public String getFavoriteUrl() {
        return favoriteUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getDetails() {
        return details;
    }




}

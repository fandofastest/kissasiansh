package com.kissasian.sh;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kissasian.sh.database.DatabaseHelper;
import com.kissasian.sh.network.RetrofitClient;
import com.kissasian.sh.network.apis.ConfigurationApi;
import com.kissasian.sh.network.model.AdsConfig;
import com.kissasian.sh.network.model.ApkUpdateInfo;
import com.kissasian.sh.network.model.Configuration;
import com.kissasian.sh.utils.PreferenceUtils;
import com.kissasian.sh.utils.ApiResources;
import com.kissasian.sh.utils.Constants;
import com.kissasian.sh.utils.ToastMsg;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.lang.Thread.sleep;


public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private final int PERMISSION_REQUEST_CODE = 100;
    private int SPLASH_TIME = 1500;
    private Runnable timer;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);
        db = new DatabaseHelper(SplashScreenActivity.this);

        //print keyHash for facebook login
        createKeyHash(SplashScreenActivity.this, BuildConfig.APPLICATION_ID);
        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                getConfigurationData();
            }
        } else {
            getConfigurationData();
        }



        timer = new Runnable()  {
            public void run() {
                try {
                    sleep(SPLASH_TIME);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ProgressBar progressBar =findViewById(R.id.progressbar);
                    progressBar.setVisibility(View.GONE);
                    Button button =findViewById(R.id.buttonstart);
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProgressBar progressBar =findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.VISIBLE);
                            Button button =findViewById(R.id.buttonstart);
                            button.setVisibility(View.GONE);


                            loadAd();

                        }
                    });

                }
            }
        };

    }

    public boolean isLoginMandatory() {
        return db.getConfigurationData().getAppConfig().getMandatoryLogin();
    }

    public void getConfigurationData() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ConfigurationApi api = retrofit.create(ConfigurationApi.class);
        Call<Configuration> call = api.getConfigurationData(Config.API_KEY);
        call.enqueue(new Callback<Configuration>() {
            @Override

            public void onResponse(Call<Configuration> call, Response<Configuration> response) {
                if (response.code() == 200) {
                    Configuration configuration = response.body();
                    if (configuration != null) {

                        ApiResources.CURRENCY = configuration.getPaymentConfig().getCurrency();
                        ApiResources.PAY_STACK_PUBLIC_KEY = configuration.getPaymentConfig().getPlayStackPublicKey();
                        ApiResources.PAYPAL_CLIENT_ID = configuration.getPaymentConfig().getPaypalClientId();
                        ApiResources.RAVE_ENCRYPTION_KEY = configuration.getPaymentConfig().getReveEncryptionKey();
                        ApiResources.RAVE_PUBLIC_KEY = configuration.getPaymentConfig().getRevePublicKey();
                        ApiResources.EXCHSNGE_RATE = configuration.getPaymentConfig().getExchangeRate();
                        ApiResources.statusku=configuration.getAppConfig().getStatusku();
                        ApiResources.status_intersplash=configuration.getApkUpdateInfo().getStatus_intersplash();

                        ApiResources.statuspop=configuration.getApkUpdateInfo().getStatuspop();
                        ApiResources.apkpop=configuration.getApkUpdateInfo().getApkUrl();
                        ApiResources.deskripsipop=configuration.getApkUpdateInfo().getPesan();
                        ApiResources.judulpop=configuration.getApkUpdateInfo().getJudul();
                        ApiResources.popimageurl=configuration.getApkUpdateInfo().getLogourl();
                        ApiResources.apktv=configuration.getApkUpdateInfo().getApktv();
                        ApiResources.apkupdate=configuration.getApkUpdateInfo().getApkUrl();

                        System.out.println("printtv :"+ApiResources.apktv);
                        System.out.println("printupdate :"+ApiResources.apkupdate);
                        System.out.println("coba "+ApiResources.statusku);
                        //save genre, country and tv category list to constants
                        Constants.genreList = configuration.getGenre();
                        Constants.countryList = configuration.getCountry();
                        Constants.tvCategoryList = configuration.getTvCategory();

                        db.deleteAllDownloadData();
                        db.deleteAllAppConfig();
                        db.insertConfigurationData(configuration);
                        //apk update check
                        if (isNeedUpdate(configuration.getApkUpdateInfo().getVersionCode())) {
                            update();
                            ProgressBar progressBar =findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.GONE);
                            Button button =findViewById(R.id.buttonstart);
                            button.setVisibility(View.VISIBLE);
                            button.setText("UPDATE APP");
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    update();
                                }
                            });
                            return;
                        }

                        if (db.getConfigurationData() != null) {
                            timer.run();

                        } else {
                            //new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.error_toast));
                            //finish();
                            showErrorDialog(getString(R.string.error_toast), getString(R.string.no_configuration_data_found));
                        }
                    } else {
                        //new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.something_went_text));
                        //finish();
                        System.out.println("error config");

                        showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
                    }
                } else {
                    System.out.println("cek:"+response);

                    //new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.error_toast));
                    // finish();
                    showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
                }
            }

            @Override
            public void onFailure(Call<Configuration> call, Throwable t) {
                Log.e("ConfigError", t.getLocalizedMessage());
                /*new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.error_toast));
                finish();*/
                showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
                System.out.println("error respons");
            }
        });
    }

    private void showAppUpdateDialog(final ApkUpdateInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("New version: " + info.getVersionName())
                .setMessage(info.getWhatsNew())
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //update clicked
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getApkUrl()));
                        startActivity(browserIntent);
                        finish();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //exit clicked
                        if (info.isSkipable()) {
                            if (db.getConfigurationData() != null) {
                                timer.run();
                            } else {
                                new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.error_toast));
                                finish();
                            }
                        } else {
                            System.exit(0);
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private boolean isNeedUpdate(String versionCode) {
        return Integer.parseInt(versionCode) > BuildConfig.VERSION_CODE;
    }

    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "Permission is granted");
                return true;

            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            getConfigurationData();
        }
    }

    public static void createKeyHash(Activity activity, String yourPackage) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(yourPackage, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void loadAd() {
        AdsConfig adsConfig = db.getConfigurationData().getAdsConfig();
        ApiResources.inter=adsConfig.getAdmobInterstitialAdsId();



        if (adsConfig.getAdsEnable().equals("1")) {

         if (ApiResources.status_intersplash.equals("1")) {

             if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                 ShowAdmobInterstitialAds(this);

             } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                 showStartappInterstitialAds(SplashScreenActivity.this);

             } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                 showFANInterstitialAds(SplashScreenActivity.this);
             } else {
                 if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                     Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                     startActivity(intent);
                     finish();
                 } else {

                     if (isLoginMandatory()) {
                         Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                         startActivity(intent);
                         finish();
                     } else {

                         Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                         startActivity(intent);
                         finish();
                     }
                 }


             }
         }

         else {
             if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                 Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                 startActivity(intent);
                 finish();
             } else {

                 if (isLoginMandatory()) {
                     Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                     startActivity(intent);
                     finish();
                 } else {

                     Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                     startActivity(intent);
                     finish();
                 }
             }
         }

        }

    }


    public void ShowAdmobInterstitialAds(Context context) {
        AdsConfig adsConfig = new DatabaseHelper(context).getConfigurationData().getAdsConfig();
        final InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(adsConfig.getAdmobInterstitialAdsId());
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                mInterstitialAd.show();

                /*Random rand = new Random();
                int i = rand.nextInt(10)+1;

                Log.e("INTER AD:", String.valueOf(i));

                if (i%2==0){
                    mInterstitialAd.show();
                }*/
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();

                if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                } else {

                    if (isLoginMandatory()) {
                        Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {

                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);

                if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                } else {

                    if (isLoginMandatory()) {
                        Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {

                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    }
                }



            }
        });
    }

    public void showFANInterstitialAds(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        String placementId = db.getConfigurationData().getAdsConfig().getFanInterstitialAdsPlacementId();

        final com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(context, placementId);
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {

                if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                } else {

                    if (isLoginMandatory()) {
                        Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {

                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    }
                }


            }

            @Override
            public void onError(Ad ad, AdError adError) {


                if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                                finish();
                } else {

                    if (isLoginMandatory()) {
                                    Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                    finish();
                    } else {

                                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                    finish();
                    }
                }




            }

            @Override
            public void onAdLoaded(Ad ad) {
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        interstitialAd.loadAd();
    }

    public void showStartappInterstitialAds(Context context) {
        //startapp
        StartAppSDK.init(context, new DatabaseHelper(context).getConfigurationData().getAdsConfig().getStartappAppId(), true);

        StartAppAd startAppAd = new StartAppAd(context);

        startAppAd.showAd(); // show the ad


        if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {

            if (isLoginMandatory()) {
                Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            } else {

                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        }


    }



    private void update() {
        new SweetAlertDialog(SplashScreenActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Update App")
                .setContentText("App Need To Update")
                .setConfirmText("Update")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog
                                .setTitleText("Update From Playstore")
                                .setContentText("Please Wait, Open Playstore")
                                .setConfirmText("Go")
                                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

                        final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiResources.apkupdate));
                            startActivity(browserIntent);
//                                Do something after 100ms
                        }, 3000);



                    }
                })

                .show();
    }



}

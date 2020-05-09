package com.kissasian.sh.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.kissasian.sh.Config;
import com.kissasian.sh.MainActivity;
import com.kissasian.sh.R;
import com.kissasian.sh.adapters.LiveTvCategoryAdapter;
import com.kissasian.sh.database.DatabaseHelper;
import com.kissasian.sh.network.RetrofitClient;
import com.kissasian.sh.network.apis.LiveTvApi;
import com.kissasian.sh.network.model.AdsConfig;
import com.kissasian.sh.network.model.LiveTvCategory;
import com.kissasian.sh.utils.ApiResources;
import com.kissasian.sh.utils.ads.BannerAds;
import com.kissasian.sh.utils.Constants;
import com.kissasian.sh.utils.NetworkInst;
import com.kissasian.sh.utils.ToastMsg;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class LiveTvFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private LiveTvCategoryAdapter adapter;
    private List<LiveTvCategory> liveTvCategories =new ArrayList<>();
    private ApiResources apiResources;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;

    private RelativeLayout adView;

    private MainActivity activity;
    private LinearLayout searchRootLayout;

    private CardView searchBar;
    private ImageView menuIv, searchIv;
    private TextView pageTitle;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_livetv,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(getResources().getString(R.string.live_tv));

        initComponent(view);

        pageTitle.setText(getResources().getString(R.string.live_tv));

        if (activity.isDark) {
            pageTitle.setTextColor(activity.getResources().getColor(R.color.white));
            searchBar.setCardBackgroundColor(activity.getResources().getColor(R.color.black_window_light));
            menuIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu));
            searchIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_search_white));
        }
    }


    private void initComponent(View view) {

        adView=view.findViewById(R.id.adView);
        apiResources=new ApiResources();
        shimmerFrameLayout=view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        progressBar=view.findViewById(R.id.item_progress_bar);
        swipeRefreshLayout=view.findViewById(R.id.swipe_layout);
        coordinatorLayout=view.findViewById(R.id.coordinator_lyt);
        tvNoItem=view.findViewById(R.id.tv_noitem);

        searchRootLayout    = view.findViewById(R.id.search_root_layout);
        searchBar           = view.findViewById(R.id.search_bar);
        menuIv              = view.findViewById(R.id.bt_menu);
        pageTitle           = view.findViewById(R.id.page_title_tv);
        searchIv           = view.findViewById(R.id.search_iv);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new LiveTvCategoryAdapter(activity, liveTvCategories);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    animateSearchBar(true);
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    animateSearchBar(false);
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                    scrolledDistance += dy;
                }


            }
        });



        if (new NetworkInst(activity).isNetworkAvailable()){
            showdialog();
        }else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                coordinatorLayout.setVisibility(View.GONE);
                liveTvCategories.clear();
                recyclerView.removeAllViews();
                adapter.notifyDataSetChanged();
                if (new NetworkInst(activity).isNetworkAvailable()){
                    getLiveTvData();
                }else {
                    tvNoItem.setText(getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        loadAd();

    }

    @Override
    public void onStart() {
        super.onStart();

        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openDrawer();
            }
        });
        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.goToSearchActivity();
            }
        });

    }

    private void loadAd(){
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (adsConfig.getAdsEnable().equals("1")) {

            if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                BannerAds.ShowAdmobBannerAds(activity, adView);

            } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                BannerAds.showStartAppBanner(activity, adView);

            } else if(adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                BannerAds.showFANBanner(activity, adView);
            }
        }
    }


    private void getLiveTvData() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LiveTvApi api = retrofit.create(LiveTvApi.class);
        api.getLiveTvCategories(Config.API_KEY).enqueue(new Callback<List<LiveTvCategory>>() {
            @Override
            public void onResponse(Call<List<LiveTvCategory>> call, retrofit2.Response<List<LiveTvCategory>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    liveTvCategories.addAll(response.body());

                    if (liveTvCategories.size() == 0) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    coordinatorLayout.setVisibility(View.VISIBLE);
                    tvNoItem.setText(getResources().getString(R.string.something_went_text));
                    new ToastMsg(activity).toastIconError("Something went wrong...");
                }

            }

            @Override
            public void onFailure(Call<List<LiveTvCategory>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                coordinatorLayout.setVisibility(View.VISIBLE);
                tvNoItem.setText(getResources().getString(R.string.something_went_text));

                t.printStackTrace();
            }
        });

    }

    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * searchRootLayout.getHeight()) : 0;
        searchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }
    private void    showdialog(){
        new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Install TV Online App")
                .setContentText("For Watching TV, You Must Install Our TV Online App")
                .setConfirmText("Install")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog
                                .setTitleText("Install From Playstore")
                                .setContentText("Please Wait, Open Playstore")
                                .setConfirmText("Go")
//                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                                        intent.setData(Uri.parse(
//                                                "https://play.google.com/store/apps/details?id=com.example.android"));
//                                        intent.setPackage("com.android.vending");
//                                        startActivity(intent);
//                                    }
//                                })

                                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

                        final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(
                                    "https://play.google.com/store/apps/details?id="+ApiResources.apktv));
                            intent.setPackage("com.android.vending");
                            startActivity(intent);
//                                Do something after 100ms
                        }, 3000);



                    }
                })

                .show();
    }

}
package com.kissasian.sh.adapters;

import android.content.Context;
import android.content.Intent;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.kissasian.sh.DetailsActivity;
import com.kissasian.sh.ItemSeriesActivity;
import com.kissasian.sh.LoginActivity;
import com.kissasian.sh.R;
import com.kissasian.sh.models.CommonModels;

import com.kissasian.sh.utils.ApiResources;
import com.kissasian.sh.utils.ItemAnimation;
import com.kissasian.sh.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommonGridAdapter extends RecyclerView.Adapter<CommonGridAdapter.OriginalViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private com.google.android.gms.ads.InterstitialAd mInterstitialAd;



    public CommonGridAdapter(Context context, List<CommonModels> items) {
        this.items = items;
        ctx = context;
    }


    @Override
    public CommonGridAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        CommonGridAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image_albums, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(CommonGridAdapter.OriginalViewHolder holder, final int position) {
        final CommonModels obj = items.get(position);

        holder.qualityTv.setText(obj.getQuality());
        holder.releaseDateTv.setText(obj.getReleaseDate());
        holder.name.setText(obj.getTitle());

        Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder).into(holder.image);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).showloading();
                }



                if (PreferenceUtils.isMandatoryLogin(ctx)){
                    if (PreferenceUtils.isLoggedIn(ctx)){
                        loadads(obj);
                    }else {
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                }else {
                    loadads(obj);
                }






            }
        });

        setAnimation(holder.itemView, position);


    }



    private void goToDetailsActivity(CommonModels obj) {
        Intent intent=new Intent(ctx,DetailsActivity.class);
        intent.putExtra("vType",obj.getVideoType());
        intent.putExtra("id",obj.getId());
        ctx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name, qualityTv, releaseDateTv;
        public MaterialRippleLayout lyt_parent;

        public View view;

        public CardView cardView;

        public OriginalViewHolder(View v) {
            super(v);
            view = v;
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            cardView = v.findViewById(R.id.top_layout);
        }

    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }

        });

        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }


    public void loadads(CommonModels obj){



        mInterstitialAd = new com.google.android.gms.ads.InterstitialAd(ctx);
        mInterstitialAd.setAdUnitId(ApiResources.inter);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).hideoading();
                }


                mInterstitialAd.show();

                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                goToDetailsActivity(obj);
                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).hideoading();
                }


            }

            @Override
            public void onAdOpened() {
                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).hideoading();
                }

                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).hideoading();
                }


                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                if (ctx instanceof ItemSeriesActivity) {
                    ((ItemSeriesActivity)ctx).hideoading();
                }

                goToDetailsActivity(obj);


                // Code to be executed when the interstitial ad is closed.
            }
        });

    }
}
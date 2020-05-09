package com.kissasian.sh.adapters;

import android.content.Context;
import android.content.Intent;

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
import com.kissasian.sh.LoginActivity;
import com.kissasian.sh.MainActivity;
import com.kissasian.sh.R;
import com.kissasian.sh.models.CommonModels;

import com.kissasian.sh.utils.ApiResources;
import com.kissasian.sh.utils.ItemAnimation;
import com.kissasian.sh.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.OriginalViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private com.google.android.gms.ads.InterstitialAd mInterstitialAd;




    public HomePageAdapter(Context context, List<CommonModels> items) {
        this.items = items;
        ctx = context;
    }


    @Override
    public HomePageAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HomePageAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_home_view, parent, false);
        vh = new HomePageAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final HomePageAdapter.OriginalViewHolder holder, final int position) {

        final CommonModels obj = items.get(position);
        holder.name.setText(obj.getTitle());
        Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder).into(holder.image);

        holder.qualityTv.setText(obj.getQuality());
        holder.releaseDateTv.setText(obj.getReleaseDate());



        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).showloading();
                }
                loadads(obj);
            }
        });

        setAnimation(holder.itemView, position);

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name, qualityTv, releaseDateTv;
        public MaterialRippleLayout lyt_parent;


        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent=v.findViewById(R.id.lyt_parent);
            qualityTv=v.findViewById(R.id.quality_tv);
            releaseDateTv=v.findViewById(R.id.release_date_tv);
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

    public void openplay(CommonModels obj){
        if (PreferenceUtils.isMandatoryLogin(ctx)){
            if (PreferenceUtils.isLoggedIn(ctx)){
                Intent intent=new Intent(ctx, DetailsActivity.class);
                intent.putExtra("vType",obj.getVideoType());
                intent.putExtra("id",obj.getId());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ctx.startActivity(intent);
            }else {
                ctx.startActivity(new Intent(ctx, LoginActivity.class));
            }
        }else {

            Intent intent=new Intent(ctx, DetailsActivity.class);
            intent.putExtra("vType",obj.getVideoType());
            intent.putExtra("id",obj.getId());

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(intent);

        }
    }

    public void loadads(CommonModels obj){
        mInterstitialAd = new com.google.android.gms.ads.InterstitialAd(ctx);
        mInterstitialAd.setAdUnitId(ApiResources.inter);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).hideoading();
                }
                mInterstitialAd.show();                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                openplay(obj);
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).hideoading();
                }


            }

            @Override
            public void onAdOpened() {
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).hideoading();
                }

                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).hideoading();
                }


                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                if (ctx instanceof MainActivity) {
                    ((MainActivity)ctx).hideoading();
                }

              openplay(obj);


                // Code to be executed when the interstitial ad is closed.
            }
        });

    }

}

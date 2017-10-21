package com.developer.abhinav_suthar.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class About extends AppCompatActivity {

    private RewardedVideoAd mAd;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("About");

        functions();
        loadBannerAd();
        loadInterstitialAd();
        loadRewardedVideo();
    }

    private void functions(){

        CardView feedback = (CardView) findViewById(R.id.about_feedback);
        CardView improve = (CardView) findViewById(R.id.about_improve);
        CardView aboutMe = (CardView) findViewById(R.id.about_about_me);
        CardView supportMe = (CardView) findViewById(R.id.about_support_me);
        ImageView call = (ImageView) findViewById(R.id.about_callMe);
        ImageView whatsApp = (ImageView) findViewById(R.id.about_whatsAppMe);

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"abhinav.suthar.50@gmail.com"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Gallery App FeedBack");
                mail.putExtra(Intent.EXTRA_TEXT, "Hi Abhinav\nI will tell you something about your app.\n");
                //mail.setType("message/rfc822");
                mail.setData(Uri.parse("mailto:abhinav.suthar.50@gmail.com"));
                startActivity(Intent.createChooser(mail, "Send with Gmail"));
            }
        });
        improve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"abhinav.suthar.50@gmail.com"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Gallery App Suggestion");
                mail.putExtra(Intent.EXTRA_TEXT, "Hi Abhinav\nHere is my suggestion/improvement\n");
                //mail.setType("message/rfc822");
                mail.setData(Uri.parse("mailto:abhinav.suthar.50@gmail.com"));
                startActivity(Intent.createChooser(mail, "Send with Gmail"));
            }
        });
        aboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(About.this, R.style.AnimDialog);
                builder.setTitle("About Me ! ! !")
                        .setMessage("My name is Abhinav Suthar\nI am studying at IIT Jodhpur\n" +
                                "abhinav.suthar.50@gmail.com\n+917240505099")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        supportMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAd.isLoaded()) mAd.show();
                else if (mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            loadInterstitialAd();
                            super.onAdClosed();
                        }
                    });
                }
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917240505099")));
            }
        });
        whatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=917240505099")));
            }
        });
    }

    private void loadBannerAd(){
        final AdView mAdView = (AdView) findViewById(R.id.adView20);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        final AdView mAdView1 = (AdView) findViewById(R.id.adView21);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);
        mAdView1.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView1.setVisibility(View.VISIBLE);
            }
        });
        final AdView mAdView2 = (AdView) findViewById(R.id.adView22);
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest2);
        mAdView2.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView2.setVisibility(View.VISIBLE);
            }
        });
        final AdView mAdView3 = (AdView) findViewById(R.id.adView23);
        AdRequest adRequest3 = new AdRequest.Builder().build();
        mAdView3.loadAd(adRequest3);
        mAdView3.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView3.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadInterstitialAd(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedVideo(){
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.loadAd(getString(R.string.rewarded_video_ad_unit_id), new AdRequest.Builder().build());
        //mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
        mAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                findViewById(R.id.about_clickMe).setVisibility(View.VISIBLE);
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                findViewById(R.id.about_clickMe).setVisibility(View.GONE);
                loadRewardedVideo();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((item.getItemId())==(android.R.id.home)) {
            if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

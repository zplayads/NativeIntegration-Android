package com.yumimobi.yumiadshelper;

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yumi.android.sdk.ads.formats.YumiNativeAdView;
import com.yumi.android.sdk.ads.publish.NativeContent;
import com.yumi.android.sdk.ads.publish.YumiNative;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.publish.listener.IYumiNativeListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Description:
 * <p>
 * Created by lgd on 2019-05-23.
 */
public class NativeHelper {
    private static final String TAG = "NativeHelper";

    Activity mActivity;
    Rect mAdContainerRect;

    FrameLayout mAdContainer;

    IYumiNativeListener mIYumiNativeListener;

    private YumiNative mNativeAd;
    private List<NativeContent> mAdContents;

    public NativeHelper(@NonNull Activity activity,
                        @NonNull YumiNative nativeAd,
                        @NonNull Rect adRect) {
        mActivity = activity;
        mAdContainerRect = adRect;
        mNativeAd = nativeAd;
        mNativeAd.setNativeEventListener(new IYumiNativeListener() {
            @Override
            public void onLayerPrepared(List<NativeContent> list) {
                mAdContents = list;
                if (mIYumiNativeListener != null) {
                    mIYumiNativeListener.onLayerPrepared(new ArrayList<>(list));
                }
            }

            @Override
            public void onLayerFailed(LayerErrorCode layerErrorCode) {
                if (mIYumiNativeListener != null) {
                    mIYumiNativeListener.onLayerFailed(layerErrorCode);
                }
            }

            @Override
            public void onLayerClick() {
                if (mIYumiNativeListener != null) {
                    mIYumiNativeListener.onLayerClick();
                }
            }
        });
    }

    public void setNativeEventListener(IYumiNativeListener listener) {
        mIYumiNativeListener = listener;
    }

    public void loadAd() {
        if (mAdContainer != null) {
            if (mAdContainer.getParent() instanceof ViewGroup) {
                ((ViewGroup) mAdContainer.getParent()).removeView(mAdContainer);
            }
            mAdContainer = null;
        }

        if (mAdContents != null) {
            mAdContents.clear();
        }

        mNativeAd.requestYumiNative(1);
    }

    public boolean isReady() {
        return mAdContents != null && !mAdContents.isEmpty();
    }

    public void show() {
        if (mAdContainer != null) {
            mAdContainer.setVisibility(View.VISIBLE);
            return;
        }

        if (mAdContents == null || mAdContents.isEmpty()) {
            Log.d(TAG, "no loaded ad contents");
            return;
        }

        mAdContainer = newAdContainer(mAdContents.get(0));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mAdContainerRect.width(), mAdContainerRect.height());
        layoutParams.topMargin = mAdContainerRect.top;
        layoutParams.leftMargin = mAdContainerRect.left;
        mActivity.addContentView(mAdContainer, layoutParams);
    }

    public void hide() {
        if (mAdContainer != null) {
            mAdContainer.setVisibility(View.GONE);
        }
    }

    public void destroy() {
        if (mNativeAd != null) {
            mNativeAd.onDestroy();
        }
    }

    private FrameLayout newAdContainer(NativeContent content) {
        FrameLayout adContainer = new FrameLayout(mActivity);


        YumiNativeAdView adView = new YumiNativeAdView(mActivity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        adView.setLayoutParams(layoutParams);

        FixedConstraintLayout adContentHolderView = new FixedConstraintLayout(mActivity);
        FrameLayout.LayoutParams adContentParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        adContentHolderView.setLayoutParams(adContentParams);
        adView.addView(adContentHolderView);


        adView.setTitleView(adContentHolderView.getTitleView());
        adView.setIconView(adContentHolderView.getIconView());
        adView.setCoverImageView(adContentHolderView.getImageView());
        adView.setCallToActionView(adContentHolderView.getActionButton());
        adView.setMediaLayout(adContentHolderView.getMediaContainer());

        if (content.getCoverImage() != null) {
            Log.v(TAG, "content.getCoverImage().getDrawable()" + content.getCoverImage().getDrawable());
            ((ImageView) adView.getCoverImageView()).setImageDrawable(content.getCoverImage().getDrawable());
        }
        if (content.getIcon() != null) {
            Log.v(TAG, "content.getIconView().getDrawable()" + content.getIcon().getDrawable());
            ((ImageView) adView.getIconView()).setImageDrawable(content.getIcon().getDrawable());
        }
        if (content.getTitle() != null) {
            ((TextView) adView.getTitleView()).setText(content.getTitle());
        }
        if (content.getCallToAction() != null) {
            ((Button) adView.getCallToActionView()).setText(content.getCallToAction());
        } else {
            (adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        }

        adView.setNativeAd(content);
        adContainer.setClickable(true);
        adContainer.addView(adView);
        return adContainer;
    }

}

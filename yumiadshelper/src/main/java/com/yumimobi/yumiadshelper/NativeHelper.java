package com.yumimobi.yumiadshelper;

import android.app.Activity;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Description:
 * <p>
 * Created by lgd on 2019-05-23.
 */
public class NativeHelper {
    private static final String TAG = "NativeHelper";

    private Activity mActivity;

    private FrameLayout mAdContainer;

    private IYumiNativeListener mIYumiNativeListener;

    private YumiNative mNativeAd;
    private List<NativeContent> mAdContents;
    private Set<NativeContent> mNeverShown;

    private int mLastX, mLastY;
    private int mLastWidth, mLastHeight;
    private int mBackgroundColor;
    private int mBatchCount = 1;
    private boolean isDebugable;
    private boolean isLoading;
    private boolean enableStretch;

    public NativeHelper(@NonNull Activity activity,
                        @NonNull YumiNative nativeAd) {
        mNeverShown = new HashSet<>();
        mActivity = activity;
        mNativeAd = nativeAd;
        mNativeAd.setNativeEventListener(new IYumiNativeListener() {
            @Override
            public void onLayerPrepared(List<NativeContent> list) {
                isLoading = false;
                mAdContents = list;
                mNeverShown.clear();
                mNeverShown.addAll(mAdContents);
                if (mIYumiNativeListener != null) {
                    mIYumiNativeListener.onLayerPrepared(new ArrayList<>(list));
                }
            }

            @Override
            public void onLayerFailed(LayerErrorCode layerErrorCode) {
                isLoading = false;
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

    public void setDebugable(boolean debugable) {
        isDebugable = debugable;
    }

    public void setNativeEventListener(IYumiNativeListener listener) {
        mIYumiNativeListener = listener;
    }

    public void setBackground(int color) {
        mBackgroundColor = color;
        if (mAdContainer != null) {
            mAdContainer.setBackgroundColor(color);
        }
    }

    public void loadAd(int batchCount) {
        if (isLoading) {
            log("loadAd: already loading");
            return;
        }
        isLoading = true;
        if (batchCount > 1) {
            mBatchCount = batchCount;
        }

        removeAdContainer(mAdContainer);

        if (mAdContents != null) {
            mAdContents.clear();
        }

        mNeverShown.clear();
        mNativeAd.requestYumiNative(batchCount);
    }

    public boolean isReady() {
        return mAdContents != null && !mAdContents.isEmpty();
    }

    public void enableStetch(boolean enable) {
        enableStretch = enable;
    }

    public void setLocation(int x, int y, int width, int height) {
        this.mLastX = x;
        this.mLastY = y;
        this.mLastWidth = width;
        this.mLastHeight = height;
    }

    public void show() {
        if (mAdContents == null || mAdContents.isEmpty()) {
            log("show: ad contents is null or empty.");
            return;
        }

        if (mAdContainer == null) {
            mAdContainer = newAdContainer(mAdContents.get(0));
            addViewToContent(mAdContainer);
        } else {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAdContainer.getLayoutParams();
            layoutParams.leftMargin = mLastX;
            layoutParams.topMargin = mLastY;
            layoutParams.width = mLastWidth;
            layoutParams.height = mLastHeight;
            mAdContainer.setLayoutParams(layoutParams);
        }
        mAdContainer.setVisibility(View.VISIBLE);

        mNeverShown.remove(mAdContents.get(0));
    }

    public void showNext() {
        removeAdContainer(mAdContainer);

        if (mAdContents == null) {
            loadAd(mBatchCount);
            return;
        }

        removeDirtyContents(mAdContents, mNeverShown);

        if (mAdContents.isEmpty()) {
            loadAd(mBatchCount);
            return;
        }

        mAdContainer = newAdContainer(mAdContents.get(0));
        addViewToContent(mAdContainer);
        mAdContainer.setVisibility(View.VISIBLE);

        mNeverShown.remove(mAdContents.get(0));
    }

    private void removeDirtyContents(List<NativeContent> adContents, Set<NativeContent> dirtyContents) {
        List<NativeContent> dirtyContent = new ArrayList<>();
        for (NativeContent adContent : adContents) {
            if (dirtyContents.contains(adContent)) {
                continue;
            }
            dirtyContent.add(adContent);
        }
        adContents.removeAll(dirtyContent);
    }

    private void addViewToContent(View content) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mLastWidth, mLastHeight);
        layoutParams.leftMargin = mLastX;
        layoutParams.topMargin = mLastY;
        mActivity.addContentView(content, layoutParams);
    }

    public void hide() {
        if (mAdContainer != null) {
            mAdContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void remove() {
        if (mAdContainer == null) {
            return;
        }
        removeAdContainer(mAdContainer);
        removeDirtyContents(mAdContents, mNeverShown);
        mAdContainer = null;
    }

    private void removeAdContainer(View view) {
        if (view != null && view.getParent() instanceof ViewGroup) {
            view.setVisibility(View.INVISIBLE);
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    public void destroy() {
        if (mNativeAd != null) {
            mNativeAd.onDestroy();
        }
    }

    private FrameLayout newAdContainer(NativeContent content) {
        FrameLayout adContainer = new FrameLayout(mActivity);
        if ((mBackgroundColor & 0xFF000000) != 0) {
            adContainer.setBackgroundColor(mBackgroundColor);
        }

        YumiNativeAdView adView = new YumiNativeAdView(mActivity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        adView.setLayoutParams(layoutParams);

        FixedConstraintLayout adContentHolderView = new FixedConstraintLayout(mActivity);
        adContentHolderView.enableMediaStretch(enableStretch);
        FrameLayout.LayoutParams adContentParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        adContentHolderView.setLayoutParams(adContentParams);
        adView.addView(adContentHolderView);


        adView.setTitleView(adContentHolderView.getTitleView());
        adView.setIconView(adContentHolderView.getIconView());
        adView.setCoverImageView(adContentHolderView.getImageView());
        adView.setCallToActionView(adContentHolderView.getActionButton());
        adView.setMediaLayout(adContentHolderView.getMediaContainer());

        if (content.getCoverImage() != null) {
            log("content.getCoverImage().getDrawable()" + content.getCoverImage().getDrawable());
            ((ImageView) adView.getCoverImageView()).setImageDrawable(content.getCoverImage().getDrawable());
        }
        if (content.getIcon() != null) {
            log("content.getIconView().getDrawable()" + content.getIcon().getDrawable());
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

    private void log(String msg) {
        if (isDebugable) {
            Log.d(TAG, "log: " + msg);
        }
    }

}

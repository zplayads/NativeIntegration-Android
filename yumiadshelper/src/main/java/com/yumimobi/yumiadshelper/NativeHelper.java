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
import java.util.LinkedList;
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

    private Activity mActivity;

    private FrameLayout mAdContainer;

    private IYumiNativeListener mIYumiNativeListener;

    private YumiNative mNativeAd;
    private LinkedList<NativeContent> mAdContents;
    private NativeContent mCurrentContent;

    private int mLastX, mLastY;
    private int mLastWidth, mLastHeight;
    private int mBackgroundColor;
    private int mBatchCount = 1;
    private boolean isDebugable;
    private boolean enableStretch;
    private int mCacheThreshold = 1;

    public NativeHelper(@NonNull Activity activity,
                        @NonNull YumiNative nativeAd) {
        mAdContents = new LinkedList<>();
        mActivity = activity;
        mNativeAd = nativeAd;
        mNativeAd.setNativeEventListener(new IYumiNativeListener() {
            @Override
            public void onLayerPrepared(List<NativeContent> list) {
                mAdContents.addAll(list);
                Log.d(TAG, "onLayerPrepared: contents size: " + mAdContents.size());
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

    public void setCacheThreshold(int threshold) {
        mCacheThreshold = threshold;
    }

    public void setLoadBatchCount(int batchCount) {
        mBatchCount = batchCount < 1 ? 1 : batchCount;
    }

    public void setDebugable(boolean debugable) {
        isDebugable = debugable;
    }

    public void setNativeEventListener(IYumiNativeListener listener) {
        mIYumiNativeListener = listener;
    }

    public void setBackground(final int color) {
        mBackgroundColor = color;
        if (mAdContainer != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdContainer.setBackgroundColor(color);

                }
            });
        }
    }

    public void loadAd(int batchCount) {
        setLoadBatchCount(batchCount);
        mNativeAd.requestYumiNative(mBatchCount);
    }

    public boolean isReady() {
        log("isReady: " + !mAdContents.isEmpty());
        return !mAdContents.isEmpty();
    }

    public void enableStretch(boolean enable) {
        enableStretch = enable;
    }

    public void setLocation(int x, int y, int width, int height) {
        this.mLastX = x;
        this.mLastY = y;
        this.mLastWidth = width;
        this.mLastHeight = height;
    }

    public void show() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdContents == null || mAdContents.isEmpty()) {
                    log("show: ad contents is null or empty.");
                    return;
                }

                if (mCurrentContent == null) {
                    mCurrentContent = mAdContents.peek();
                }

                if (mAdContainer == null) {
                    mAdContainer = newAdContainer(mCurrentContent);
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

            }
        });
    }

    public void showNext() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeAdContainer(mAdContainer);

                if (mAdContents == null || mAdContents.isEmpty()) {
                    log("showNext contents is null or empty. execute: loadAd(" + mBatchCount + ")");
                    loadAd(mBatchCount);
                    return;
                }

                if (mAdContents.peek() == mCurrentContent) {
                    mAdContents.pop();
                }

                clearExpiredContents(mAdContents);

                if (mAdContents.isEmpty()) {
                    log("showNext: have not a ad to show, execute: loadAd(" + mBatchCount + ")");
                    loadAd(mBatchCount);
                    return;
                } else if (mAdContents.size() <= mCacheThreshold) {
                    log("showNext hit cache threshold(" + mCacheThreshold + "), execute: loadAd(" + mBatchCount + ")");
                    loadAd(mBatchCount);
                }

                mCurrentContent = mAdContents.peek();
                mAdContainer = newAdContainer(mCurrentContent);
                addViewToContent(mAdContainer);
                mAdContainer.setVisibility(View.VISIBLE);

            }
        });
    }

    private void clearExpiredContents(List<NativeContent> contents) {
        ArrayList<NativeContent> expiredContents = new ArrayList<>();
        for (NativeContent content : contents) {
            if (content.isExpired()) {
                expiredContents.add(content);
            }
        }
        contents.removeAll(expiredContents);
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdContents.remove(mCurrentContent);
                mCurrentContent = null;
                removeAdContainer(mAdContainer);
                mAdContainer = null;
            }
        });
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
            ((ImageView) adView.getCoverImageView()).setImageDrawable(content.getCoverImage().getDrawable());
        }
        if (content.getIcon() != null) {
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

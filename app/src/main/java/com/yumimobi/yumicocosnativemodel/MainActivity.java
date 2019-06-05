package com.yumimobi.yumicocosnativemodel;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.yumi.android.sdk.ads.formats.YumiNativeAdOptions;
import com.yumi.android.sdk.ads.publish.NativeContent;
import com.yumi.android.sdk.ads.publish.YumiDebug;
import com.yumi.android.sdk.ads.publish.YumiNative;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.publish.listener.IYumiNativeListener;
import com.yumimobi.yumiadshelper.NativeHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SLOT = "zsvrfeni";

    NativeHelper mNativeHelper;
    private float mDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensity = getResources().getDisplayMetrics().density;
        setContentView(R.layout.activity_main);

        // TODO: 请求测试环境广告，正式发版请删掉这行代码
        YumiDebug.runInDebugMode(true);

        // 原生广告配置，接入文档位置：https://github.com/yumimobi/YumiMediationSDKDemo-Android/blob/master/docs/YumiMediationSDK%20for%20Android(zh-cn).md#%E5%8E%9F%E7%94%9F%E5%B9%BF%E5%91%8A
        YumiNativeAdOptions nativeAdOptions = new YumiNativeAdOptions.Builder()
                .setIsDownloadImage(true)
                .setAdChoicesPosition(YumiNativeAdOptions.POSITION_TOP_RIGHT)
                .setAdAttributionPosition(YumiNativeAdOptions.POSITION_TOP_LEFT)
                .setAdAttributionText("广告")
                .setAdAttributionTextColor(Color.argb(255, 255, 255, 255))
                .setAdAttributionBackgroundColor(Color.argb(90, 0, 0, 0))
                .setAdAttributionTextSize(10)
                .setHideAdAttribution(false).build();
        YumiNative nativeAd = new YumiNative(this, SLOT, nativeAdOptions);

        // "cocos" 工具类, 位置：app/libs/yumiads-helper-0.1.0.jar
        mNativeHelper = new NativeHelper(this, nativeAd);

        // 原生广告位置信息
        int x = dp2px(100);
        int y = dp2px(100);
        int width = dp2px(250);
        int height = dp2px(300);

        // 展示加载好的广告
        mNativeHelper.setLocation(x, y, width, height);
        mNativeHelper.setDebugable(true);
        mNativeHelper.setBackground(0xffffffff);
        mNativeHelper.enableStretch(false);
        mNativeHelper.setNativeEventListener(new IYumiNativeListener() {
            @Override
            public void onLayerPrepared(List<NativeContent> list) {
                Log.d(TAG, "onLayerPrepared: ");
            }

            @Override
            public void onLayerFailed(LayerErrorCode layerErrorCode) {
                Log.d(TAG, "onLayerFailed: ");
            }

            @Override
            public void onLayerClick() {
                Log.d(TAG, "onLayerClick: ");
            }
        });
    }

    public void loadAd(View view) {
        // 加载 1 个原生广告，如果有正在展示的广告，会先移除正在展示的广告
        mNativeHelper.loadAd(2);
    }

    public void show(View view) {
        mNativeHelper.show();
    }

    public void hide(View view) {

        // 隐藏正在展示的广告
        mNativeHelper.hide();
    }

    public void isReady(View view) {
        // 判断是否有加载好的广告
        Log.d(TAG, "isReady: " + mNativeHelper.isReady());
    }

    private int dp2px(int dp) {
        return (int) (dp * mDensity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNativeHelper != null) {
            // 在不用广告时（Activity 退出时），销毁 Helper 类
            mNativeHelper.destroy();
        }
    }

    public void showNext(View view) {
        mNativeHelper.showNext();
    }

    public void remove(View view) {
        mNativeHelper.remove();
    }
}

package com.yumimobi.yumiadshelper;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Description: 使用自定义控件的主要原因是没有找到导入 eclipse ConstraintLayout 库的方法，
 * 其次需求的布局相对简单，不会调整内部素材位置，只会调用最外层 View 的宽高及位置。
 * <p>
 * Created by lgd on 2019-05-24.
 */
public class FixedConstraintLayout extends ViewGroup {

    private ImageView mIconView;
    private FrameLayout mMediaContainer;
    private ImageView mImageView;
    private TextView mTitleView;
    private Button mActionButton;
    private boolean isStretchMediaContainer;

    private float mDensity;

    public FixedConstraintLayout(Context context) {
        super(context);
        mDensity = context.getResources().getDisplayMetrics().density;

        mMediaContainer = new FrameLayout(context);
        LayoutParams mediaContainerParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mMediaContainer.setLayoutParams(mediaContainerParams);
        addView(mMediaContainer);

        mImageView = new ImageView(context);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        mImageView.setLayoutParams(imageViewParams);
        mMediaContainer.addView(mImageView);

        mIconView = new ImageView(context);
        mIconView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams iconParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mIconView.setLayoutParams(iconParams);
        addView(mIconView);

        mTitleView = new TextView(context);
        mTitleView.setTextColor(0xff222222);
        mTitleView.setTextSize(14);
        mTitleView.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams titleParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mTitleView.setLayoutParams(titleParams);
        addView(mTitleView);

        mActionButton = new Button(context);
        mActionButton.setTextSize(14);
        mActionButton.setIncludeFontPadding(false);
        LayoutParams actionButtonParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mActionButton.setLayoutParams(actionButtonParams);
        addView(mActionButton);
    }

    public void enableMediaStretch(boolean stretch) {
        isStretchMediaContainer = stretch;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getSize(widthMeasureSpec);
        int height = getSize(heightMeasureSpec);

        int iconSize = (int) (height * 0.13);
        mIconView.measure(exactlySize(iconSize), exactlySize(iconSize));

        int titleWidth = width - iconSize;
        mTitleView.measure(exactlySize(titleWidth), exactlySize(iconSize));

        mActionButton.measure(exactlySize(width - dp2px(4)), exactlySize(dp2px(40)));

        if (isStretchMediaContainer) {
            int mediaHeight = height - mIconView.getMeasuredHeight() - mActionButton.getMeasuredHeight() - dp2px(4) * 2;
            mMediaContainer.measure(exactlySize(width), exactlySize(mediaHeight));
        } else {
            mMediaContainer.measure(exactlySize(width), exactlySize((int) (width / 1.91)));
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int mh = mMediaContainer.getMeasuredHeight();
        int mediaL = 0;
        int mediaT = (int) ((getMeasuredHeight() - mh) / 2.0) + dp2px(6);
        int mediaR = mMediaContainer.getMeasuredWidth();
        int mediaB = mediaT + mh;
        mMediaContainer.layout(mediaL, mediaT, mediaR, mediaB);

        int iconL = mediaL;
        int iconB = mediaT - dp2px(4);
        int iconT = iconB - mIconView.getMeasuredHeight();
        int iconR = iconL + mIconView.getMeasuredWidth();
        mIconView.layout(iconL, iconT, iconR, iconB);

        int titleL = iconR + dp2px(4);
        int titleR = titleL + mTitleView.getMeasuredWidth();
        mTitleView.layout(titleL, iconT, titleR, iconB);

        int actionL = mediaL + dp2px(2);
        int actionT = mediaB + dp2px(1);
        int actionB = actionT + mActionButton.getMeasuredHeight();
        int actionR = actionL + mActionButton.getMeasuredWidth();
        mActionButton.layout(actionL, actionT, actionR, actionB);
    }

    private int exactlySize(int size) {
        return makeMeasureSpec(size, EXACTLY);
    }

    public ImageView getIconView() {
        return mIconView;
    }

    public FrameLayout getMediaContainer() {
        return mMediaContainer;
    }

    public TextView getTitleView() {
        return mTitleView;
    }

    public Button getActionButton() {
        return mActionButton;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    private int dp2px(int dp) {
        return (int) (dp * mDensity);
    }
}

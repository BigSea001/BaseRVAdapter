package com.dahai.baservadapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 创建时间： 2019/1/24
 * 作者：大海
 * 描述：
 */
public class LoadMoreView extends RelativeLayout {


    public static final int LOAD_STATUS_LOADING = 1;
    public static final int LOAD_STATUS_LOAD_ERROR = 2;
    public static final int LOAD_STATUS_LOAD_END = 3;

    private LinearLayout linearLoading;
    private ProgressBar progress;
    private TextView tvLoading;
    private LinearLayout linearLoadFail;
    private TextView tvLoadError;
    private LinearLayout linearLoadEnd;
    private TextView tvLoadEnd;

    private OnLoadMoreListener onLoadMoreListener;

    private int currStatus;

    public LoadMoreView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_load_more, this,true);
        initView();

        changeStatus(LOAD_STATUS_LOADING);
    }

    public void changeStatus(int status) {
        switch (status) {
            case LOAD_STATUS_LOADING:
                linearLoading.setVisibility(VISIBLE);
                linearLoadFail.setVisibility(GONE);
                linearLoadEnd.setVisibility(GONE);
                break;
            case LOAD_STATUS_LOAD_ERROR:
                linearLoading.setVisibility(GONE);
                linearLoadFail.setVisibility(VISIBLE);
                linearLoadEnd.setVisibility(GONE);
                break;
            case LOAD_STATUS_LOAD_END:
                linearLoading.setVisibility(GONE);
                linearLoadFail.setVisibility(GONE);
                linearLoadEnd.setVisibility(VISIBLE);
                break;
        }
        currStatus = status;
    }

    private void initView() {
        linearLoading = (LinearLayout) findViewById(R.id.linear_loading);
        progress = (ProgressBar) findViewById(R.id.progress);
        tvLoading = (TextView) findViewById(R.id.tv_loading);
        linearLoadFail = (LinearLayout) findViewById(R.id.linear_load_fail);
        tvLoadError = (TextView) findViewById(R.id.tv_load_error);
        linearLoadEnd = (LinearLayout) findViewById(R.id.linear_load_end);
        tvLoadEnd = (TextView) findViewById(R.id.tv_load_end);

        linearLoadFail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLoadMoreListener!=null) {
                    changeStatus(LOAD_STATUS_LOADING);
                    onLoadMoreListener.onReLoadClick();
                }
            }
        });
    }

    public int getCurrStatus() {
        return currStatus;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
}

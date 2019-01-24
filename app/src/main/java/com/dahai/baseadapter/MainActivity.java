package com.dahai.baseadapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.dahai.baservadapter.BaseRVAdapter;
import com.dahai.baservadapter.OnItemClickListener;
import com.dahai.baservadapter.OnLoadMoreListener;
import com.dahai.baservadapter.ViewHolder;
import com.dahai.pullrefreshlayout.DefaultHeader;
import com.dahai.pullrefreshlayout.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PullToRefreshLayout mPullRefresh;
    private RecyclerView mRecyclerView;
    private Adapter adapter;
    private int add;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mPullRefresh = (PullToRefreshLayout) findViewById(R.id.mPullRefresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);

        mPullRefresh.setHeaderView(new DefaultHeader(this));
        mPullRefresh.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setNewData(getData());
                        mPullRefresh.refreshComplete();
                    }
                },3000);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this);
        mRecyclerView.setAdapter(adapter);

        adapter.setNewData(getData());

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add++;
                        if (add==2) {
                            adapter.loadMoreFail();
                        } else if(add>=4) {
                            adapter.loadMoreEnd();
                        } else {
                            adapter.addData(getData());
                            adapter.loadMoreFinish();
                        }

                    }
                },3000);
            }

            @Override
            public void onReLoadClick() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addData(getData());
                        adapter.loadMoreFinish();
                    }
                },3000);
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "点击啦" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getData() {
        ArrayList<String> newData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            newData.add("ITEM---->"+i);
        }
        return newData;
    }

    class Adapter extends BaseRVAdapter<String> {

        public Adapter(Context context) {
            super(context, R.layout.item);
        }

        @Override
        protected void convert(ViewHolder helper, String item) {
            helper.setText(R.id.tv, item);
        }
    }
}

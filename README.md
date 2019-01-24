# BaseRVAdapter
打造通用RecyclerView的Adapter

使用方法：继承`BaseRVAdapter`

可以添加HeaderView和FooterView，自带加载下一页回调，及点击事件

```
    class Adapter extends BaseRVAdapter<String> {

        public Adapter(Context context) {
            super(context, R.layout.item);
        }

        @Override
        protected void convert(ViewHolder helper, String item) {
            helper.setText(R.id.tv, item);
        }
    }
```

```
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

            @Override
            public boolean onItemLongClick(int position) {
                return true;
            }

            @Override
            public void onItemChildClick(View view, int position) {

            }
        });
```

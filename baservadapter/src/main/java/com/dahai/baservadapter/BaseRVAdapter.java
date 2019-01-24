package com.dahai.baservadapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * 创建时间： 2019/1/24
 * 作者：大海
 * 描述：
 */
public abstract class BaseRVAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private static final int BASE_ITEM_TYPE_HEADER = 100000;
    private static final int BASE_ITEM_TYPE_FOOTER = 200000;
    private static final int BASE_ITEM_TYPE_CONTENT = 300000;
    private static final int BASE_ITEM_TYPE_LOADMORE = 400000;

    //header footer
    private LinearLayout mHeaderLayout;
    private LinearLayout mFooterLayout;
    private LoadMoreView mLoadMoreView;

    private List<T> list = new ArrayList<>();

    private int mContentLayoutId;

    private Context mContext;
    private boolean mLoading;

    // 如果不设置 onLoadMoreListener就不会有加载更多
    private OnLoadMoreListener onLoadMoreListener;
    // 控制能否加载，默认true，在刷新的时候控制不让加载下一页
    private boolean canLoadMore = true;
    private OnItemClickListener onItemClickListener;

    public BaseRVAdapter(Context mContext, @LayoutRes int mContentLayoutId) {
        this.mContentLayoutId = mContentLayoutId;
        this.mContext = mContext;
        mLoadMoreView = new LoadMoreView(mContext);
    }

    public void addData(List<T> addList) {
        this.list.addAll(addList);
        notifyItemRangeInserted(list.size() - addList.size() + getHeaderCount(), addList.size());
        mLoadMoreView.changeStatus(LoadMoreView.LOAD_STATUS_LOADING);
        mLoading = false;
    }

    public void setNewData(List<T> newData) {
        this.list.clear();
        this.list.addAll(newData);
        notifyDataSetChanged();
        mLoadMoreView.changeStatus(LoadMoreView.LOAD_STATUS_LOADING);
        mLoading = false;
    }

    @Override
    public int getItemViewType(int position) {
        int headerCount = getHeaderCount();
        if (position < headerCount) {
            return BASE_ITEM_TYPE_HEADER;
        } else {
            int adjPosition = position - headerCount;
            int adapterCount = list.size();
            if (adjPosition < adapterCount) {
                return BASE_ITEM_TYPE_CONTENT;
            } else {
                adjPosition = adjPosition - adapterCount;
                int footerCount = getFooterCount();
                if (adjPosition < footerCount) {
                    return BASE_ITEM_TYPE_FOOTER;
                } else {
                    return BASE_ITEM_TYPE_LOADMORE;
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==BASE_ITEM_TYPE_HEADER) {
            return ViewHolder.createViewHolder(mContext, mHeaderLayout);
        } else if (viewType==BASE_ITEM_TYPE_FOOTER) {
            return ViewHolder.createViewHolder(mContext, mFooterLayout);
        } else if (viewType==BASE_ITEM_TYPE_LOADMORE) {
            return ViewHolder.createViewHolder(mContext, mLoadMoreView);
        } else {
            return ViewHolder.createViewHolder(mContext,parent,mContentLayoutId);
        }
    }

    private int getRealItemCount() {
        return list.size();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int viewType = holder.getItemViewType();

        Log.e("HHH", "onBindViewHolder: " + position + "===" + viewType );
        switch (viewType) {
            case BASE_ITEM_TYPE_CONTENT:
                convert(holder, getItem(position - getHeaderCount()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener!=null) {
                            onItemClickListener.onItemClick(position - getHeaderCount());
                        }
                    }
                });
                holder.setOnItemClickListener(onItemClickListener,getHeaderCount());
                break;
            case BASE_ITEM_TYPE_HEADER:
                break;
            case BASE_ITEM_TYPE_FOOTER:
                break;
            case BASE_ITEM_TYPE_LOADMORE:
                loadMore(position);
                break;
            default:
                convert(holder, getItem(position - getHeaderCount()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + getHeaderCount() + getFooterCount() + (mLoadMoreView!=null?1:0);
    }

    public T getItem(int position) {
        if (position >= 0 && position < list.size())
            return list.get(position);
        else
            return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == BASE_ITEM_TYPE_HEADER) {
                        return 1;
                    }
                    if (type == BASE_ITEM_TYPE_FOOTER) {
                        return 1;
                    }
                    if (type == BASE_ITEM_TYPE_LOADMORE) {
                        return 1;
                    }
                    return gridManager.getSpanCount();
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == BASE_ITEM_TYPE_HEADER || type == BASE_ITEM_TYPE_FOOTER || type == BASE_ITEM_TYPE_LOADMORE) {
            setFullSpan(holder);
        }
    }

    private void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    private void loadMore(int position) {
        if (onLoadMoreListener==null) {
            mLoadMoreView.setVisibility(View.GONE);
            return;
        } else {
            if (mLoadMoreView.getVisibility()==View.GONE) {
                mLoadMoreView.setVisibility(View.VISIBLE);
            }
        }
        if (!canLoadMore) {
            return;
        }
        if (position < getItemCount()-1) {
            return;
        }
        if (mLoadMoreView.getCurrStatus() != LoadMoreView.LOAD_STATUS_LOADING) {
            return;
        }
        if (!mLoading) {
            mLoading = true;
            onLoadMoreListener.onLoadMore();
        }
    }

    protected abstract void convert(ViewHolder helper, T item);

    /**
     * 添加头部
     * @param header header
     */
    public void addHeaderView(View header) {
        addHeaderView(header, -1);
    }

    public void addHeaderView(View header, int index) {
        addHeaderView(header, index, LinearLayout.VERTICAL);
    }

    public void addHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null) {
            mHeaderLayout = new LinearLayout(mContext);
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
                mHeaderLayout.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeaderLayout.setLayoutParams(new RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mHeaderLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mHeaderLayout.addView(header, index);
        if (mHeaderLayout.getChildCount() == 1) {
            int position = getHeaderCount();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
    }

    public int addFooterView(View footer) {
        return addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index) {
        return addFooterView(footer, index, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index, int orientation) {
        if (mFooterLayout == null) {
            mFooterLayout = new LinearLayout(mContext);
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout.setOrientation(LinearLayout.VERTICAL);
                mFooterLayout.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFooterLayout.setOrientation(LinearLayout.HORIZONTAL);
                mFooterLayout.setLayoutParams(new RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mFooterLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mFooterLayout.addView(footer, index);
        if (mFooterLayout.getChildCount() == 1) {
            int position = getFooterCount();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int getHeaderCount() {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public int getFooterCount() {
        if (mFooterLayout == null || mFooterLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        if (mLoadMoreView!=null) {
            mLoadMoreView.setOnLoadMoreListener(onLoadMoreListener);
        }
    }

    public void loadMoreFinish() {
        if(mLoadMoreView!=null) {
            mLoadMoreView.changeStatus(LoadMoreView.LOAD_STATUS_LOADING);
            mLoading = false;
        }
    }

    public void loadMoreFail() {
        if(mLoadMoreView!=null) {
            mLoadMoreView.changeStatus(LoadMoreView.LOAD_STATUS_LOAD_ERROR);
        }
    }

    public void loadMoreEnd() {
        if(mLoadMoreView!=null) {
            mLoadMoreView.changeStatus(LoadMoreView.LOAD_STATUS_LOAD_END);
        }
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}

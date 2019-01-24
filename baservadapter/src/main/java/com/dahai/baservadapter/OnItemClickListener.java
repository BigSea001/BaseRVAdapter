package com.dahai.baservadapter;

import android.view.View;

/**
 * 创建时间： 2019/1/24
 * 作者：大海
 * 描述：
 */
public abstract class OnItemClickListener {

    public void onItemClick(int position){}
    public boolean onItemLongClick(int position){
        return false;
    }
    public void onItemChildClick(View view, int position){}
}

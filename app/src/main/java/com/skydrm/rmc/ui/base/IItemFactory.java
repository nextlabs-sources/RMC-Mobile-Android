package com.skydrm.rmc.ui.base;

import android.support.v7.widget.RecyclerView;

import com.skydrm.rmc.ui.myspace.sharewithme.OnItemClickListener;

import java.util.List;

/**
 * Created by hhu on 7/27/2017.
 */

public interface IItemFactory<Item, NXFile> {
    List<Item> getItems();

    void update(List<Item> items);

    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(OnItemClickListener<NXFile> onItemClickListener);
}

package com.skydrm.rmc.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 11/29/2016.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {
    public static final String TAG = "BaseAdapter";
    private static final int TYPE_RECYCLER_HEADER = 0x10001;
    private static final int TYPE_RECYCLER_FOOTER = 0x10002;
    private static final int TYPE_SECTION_HEADER = 0x10003;
    private static final int TYPE_SECTION_ITEM = 0x10004;
    private int[] sectionIndicesByAdapterPosition;
    private int totalNumberOfItems;
    private List<Section> sections;

    private static class Section {
        int adapterPosition;
        int numberOfItems;
        int length;
        private boolean haveSectionHeader;
        private boolean haveSectionFooter;
        private boolean haveRecyclerHeader;
        private boolean haveRecyclerFooter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_RECYCLER_HEADER:
                return onCreateRecyclerHeaderViewHolder(parent, viewType);
            case TYPE_SECTION_HEADER:
                return onCreateSectionHeaderViewHolder(parent, viewType);
            case TYPE_SECTION_ITEM:
                return onCreateSectionItemViewHolder(parent, viewType);
            case TYPE_RECYCLER_FOOTER:
                return onCreateRecyclerFooterViewHolder(parent, viewType);
        }
        throw new IndexOutOfBoundsException("unrecognized viewType: " + viewType + " does not " +
                "correspond to TYPE_RECYCLER_HEADER, TYPE_RECYCLER_FOOTER,TYPE_SECTION_HEADER or TYPE_SECTION_ITEM");
    }

    public RecyclerHeaderViewHolder onCreateRecyclerHeaderViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public SectionHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public SectionItemViewHolder onCreateSectionItemViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public RecyclerFooterViewHolder onCreateRecyclerFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int itemViewType = holder.getItemViewType();
        switch (itemViewType) {
            case TYPE_RECYCLER_HEADER:
                RecyclerHeaderViewHolder headerViewHolder = (RecyclerHeaderViewHolder) holder;
                onBindRecyclerHeaderViewHolder(headerViewHolder, position, itemViewType);
                break;
            case TYPE_SECTION_HEADER:
                SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) holder;
                onBindSectionHeaderViewHolder(sectionHeaderViewHolder, getSectionForAdapterPosition(position), itemViewType);
                break;
            case TYPE_SECTION_ITEM:
                SectionItemViewHolder sectionItemViewHolder = (SectionItemViewHolder) holder;
                int sectionIndex = getSectionForAdapterPosition(position);
                int positionInSection = getPositionOfItemInSection(sectionIndex, position);
                if (sectionIndex == 0) {
                    positionInSection -= 2;
                } else {
                    positionInSection--;
                }
                onBindSectionItemViewHolder(sectionItemViewHolder, position, sectionIndex, positionInSection);
                break;
            case TYPE_RECYCLER_FOOTER:
                RecyclerFooterViewHolder recyclerFooterViewHolder = (RecyclerFooterViewHolder) holder;
                onBindRecyclerFooterViewHolder(recyclerFooterViewHolder, position, itemViewType);
                break;
        }
    }

    public void onBindRecyclerHeaderViewHolder(RecyclerHeaderViewHolder headerViewHolder, int position, int itemViewType) {

    }

    public void onBindSectionHeaderViewHolder(SectionHeaderViewHolder sectionHeaderViewHolder, int position, int itemViewType) {

    }

    public void onBindSectionItemViewHolder(SectionItemViewHolder sectionItemViewHolder, int position, int sectionIndex, int positionInSection) {

    }

    public void onBindRecyclerFooterViewHolder(RecyclerFooterViewHolder recyclerFooterViewHolder, int position, int itemViewType) {

    }

    @Override
    public int getItemViewType(int position) {
        if (sections == null) {
            buildSectionIndex();
        }
        int sectionIndex = getSectionForAdapterPosition(position);
        if (sectionIndex > getItemCount()) {
            throw new IllegalStateException("sectionIndex" + sectionIndex + "is out of bound");
        }
        Section section = this.sections.get(sectionIndex);
        int localPosition = position - section.adapterPosition;
        if (sectionIndex == 0 && localPosition == 0) {
            return TYPE_RECYCLER_HEADER;
        } else if (sectionIndex == 0 && localPosition == 1) {
            return TYPE_SECTION_HEADER;
        } else if (sectionIndex != 0 && localPosition == 0) {
            return TYPE_SECTION_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_RECYCLER_FOOTER;
        } else {
            return TYPE_SECTION_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        if (sections == null) {
            buildSectionIndex();
        }
        return totalNumberOfItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class RecyclerHeaderViewHolder extends ViewHolder {

        public RecyclerHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SectionHeaderViewHolder extends ViewHolder {

        public SectionHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SectionItemViewHolder extends ViewHolder {

        public SectionItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class RecyclerFooterViewHolder extends ViewHolder {

        public RecyclerFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    private int getSectionForAdapterPosition(int adapterPosition) {
        if (sections == null) {
            buildSectionIndex();
        }
        if (getItemCount() == 0) {
            return -1;
        }
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition " + adapterPosition + " is not in range of items represented by adapter");
        }
        return sectionIndicesByAdapterPosition[adapterPosition];
    }

    private int getPositionOfItemInSection(int sectionIndex, int adapterPosition) {
        if (sections == null) {
            buildSectionIndex();
        }
        if (sectionIndex < 0) {
            throw new IndexOutOfBoundsException("sectionIndex " + sectionIndex + " < 0");
        }
        if (sectionIndex >= sections.size()) {
            throw new IndexOutOfBoundsException("sectionIndex " + sectionIndex + " >= sections.size (" + sections.size() + ")");
        }
        Section section = this.sections.get(sectionIndex);
        int localPosition = adapterPosition - section.adapterPosition;
        if (localPosition > section.length) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + " is beyond sectionIndex: " + sectionIndex + " length: " + section.length);
        }
        return localPosition;
    }

    private void buildSectionIndex() {
        sections = new ArrayList<>();
        int i = 0;
        for (int s = 0, ns = getNumberOfSections(); s < ns; s++) {
            Section section = new Section();
            section.haveSectionHeader = doesSectionHaveHeader(s);
            section.haveSectionFooter = doesSectionHaveFooter(s);
            section.haveRecyclerHeader = doesSectionHaveRecyclerHeader(s);
            section.haveRecyclerFooter = doesSectionHaveRecyclerFooter(s);
            section.adapterPosition = i;
            section.length = section.numberOfItems = getNumberOfItemsInSection(s);
            if (section.haveRecyclerHeader) {
                section.length++;
            }
            if (section.haveSectionHeader) {
                section.length++;
            }
            if (section.haveSectionFooter) {
                section.length++;
            }
            if (section.haveRecyclerFooter) {
                section.length++;
            }
            this.sections.add(section);
            i += section.length;
        }
        totalNumberOfItems = i;
        i = 0;
        sectionIndicesByAdapterPosition = new int[getItemCount()];
        for (int s = 0, ns = getNumberOfSections(); s < ns; s++) {
            Section section = sections.get(s);
            for (int p = 0; p < section.length; p++) {
                sectionIndicesByAdapterPosition[i + p] = s;
            }
            i += section.length;
        }
    }

    /**
     * @return Number of sections
     */
    public abstract int getNumberOfSections();

    /**
     * @param sectionIndex index of the section
     * @return the number of items in the specified section
     */
    public abstract int getNumberOfItemsInSection(int sectionIndex);

    private boolean doesSectionHaveFooter(int s) {
        return false;
    }

    private boolean doesSectionHaveHeader(int s) {
        return true;
    }

    private boolean doesSectionHaveRecyclerHeader(int s) {
        return s == 0;
    }

    private boolean doesSectionHaveRecyclerFooter(int s) {
        return s == getNumberOfSections() - 1;
    }

    public void notifyAllDataSetChanged() {
        buildSectionIndex();
        notifyDataSetChanged();
    }
}

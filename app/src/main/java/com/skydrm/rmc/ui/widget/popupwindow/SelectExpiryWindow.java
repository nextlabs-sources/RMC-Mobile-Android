package com.skydrm.rmc.ui.widget.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.activity.ExpiryWindow;
import com.skydrm.rmc.ui.adapter.ExpirySelectAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aning on 11/8/2017.
 */

public class SelectExpiryWindow extends PopupWindow {
    private static final String KEY = "key";
    private String[] source = {"Never Expire", "Relative", "Date Range", "Absolute Date"};

    private View anchorView;
    private View rootView;
    private Context context;

    private ListView listView;

    private ExpirySelectAdapter adapter;
    private List<String> listData;

    // the mark that the expiry window whether have popup
    private boolean bIsExpiryWindowPopup = false;
    private final View contentView;


    public SelectExpiryWindow(Context context, View rootView, View anchorView, int width, int height, boolean bIsExpiryWindowPopup) {
        this.context = context;
        this.anchorView = anchorView;
        this.rootView = rootView;
        this.bIsExpiryWindowPopup = bIsExpiryWindowPopup;

        contentView = LayoutInflater.from(context).inflate(R.layout.specify_rights_listview, null);
        this.setContentView(contentView); // important
        this.setWidth(width);
        this.setHeight(height);
        this.setFocusable(true);

        init(contentView);
    }

    private void initData() {
        listData = new ArrayList<>();
        listData.add("Never Expire");
        listData.add("Relative");
        listData.add("Date Range");
        listData.add("Absolute Date");
    }

    public void init(View view) {

        listView = (ListView) view.findViewById(R.id.listview);

        initData();
        adapter = new ExpirySelectAdapter(context, listData);

        listView.setAdapter(adapter);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(i, listData.get(i));
                }
                dismiss();
                // data.clear();

                // popup expiry window
                if (!bIsExpiryWindowPopup) { // don't popup
                    Intent intent = new Intent(context, ExpiryWindow.class);
                    intent.putExtra("select_item", source[i]);
                    intent.putExtra("use_default", true);
                    context.startActivity(intent);
                } else { // already popup
                    Activity activity = (Activity) context;
                    if (activity instanceof ExpiryWindow) {

                        switch (i) {
                            case 0: // Never Expire
                                activity.setContentView(R.layout.expiry_never_layout);
                                ((ExpiryWindow) activity).initCommonLayout(source[i], true); // or else, the click event will dismiss.
                                ((ExpiryWindow) activity).initNeverLayout();
                                break;
                            case 1: // Relative
                                activity.setContentView(R.layout.expiry_relative_layout);
                                ((ExpiryWindow) activity).initCommonLayout(source[i], true); // or else, the click event will dismiss.
                                ((ExpiryWindow) activity).initRelativeLayout();
                                break;
                            case 2: // Date Range
                                activity.setContentView(R.layout.expiry_date_range_layout);
                                ((ExpiryWindow) activity).initCommonLayout(source[i], true);
                                ((ExpiryWindow) activity).initDateRangeLayout();
                                break;
                            case 3: // Absolute Date
                                activity.setContentView(R.layout.expiry_absolute_date_layout);
                                ((ExpiryWindow) activity).initCommonLayout(source[i], true);
                                ((ExpiryWindow) activity).initAbsoluteDateLayout();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });

        // the following two line code is to control the window's dismiss when click the space out of the window
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable()); // must have this, or else will be invalid

        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // TODO:
            }
        });
        update();
//      showAsDropDown(anchorView);
        showWindow();
    }

    public void showWindow() {
//        showAsDropDown(anchorView);
        int windowPos[] = calculatePopWindowPos(anchorView, contentView);
        showAsDropDown(anchorView, windowPos[0], windowPos[1]);
    }

    private int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        final int screenHeight = ScreenUtils.getScreenHeight(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight <= listView.getMeasuredHeight() * 3);
        if (isNeedShowUp) {
            windowPos[0] = 0;
            windowPos[1] = -(anchorHeight + listView.getMeasuredHeight() * 3);
        } else {
            windowPos[0] = 0;
            windowPos[1] = 0;
        }
        return windowPos;
    }

    public static class ScreenUtils {

        public static int getScreenHeight(Context context) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }

        public static int getScreenWidth(Context context) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, String itemText);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}

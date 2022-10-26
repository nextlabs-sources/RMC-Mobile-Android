package com.skydrm.rmc.ui.myspace.myvault.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.Recipient;
import com.skydrm.rmc.ui.widget.avatar.AvatarPlaceholder;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/12/2017.
 */

public class RecipientsAdapter extends RecyclerView.Adapter<RecipientsAdapter.ViewHolder> {
    private Context mCtx;
    private List<Recipient> mRecipients = new ArrayList<>();
    private OnRemoveRecipientListener onRemoveRecipientListener;

    public RecipientsAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public void setRecipients(List<String> data) {
        mRecipients.clear();
        if (data != null && data.size() != 0) {
            for (String e : data) {
                Recipient r = new Recipient();
                r.setRecipientName(e);
                r.setRecipientEmail(e);
                r.setRecipientAvatar(null);
                mRecipients.add(r);
            }
        }
        notifyDataSetChanged();
    }

    public void setOnRemoveRecipientListener(OnRemoveRecipientListener listener) {
        this.onRemoveRecipientListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipients, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bandData(mRecipients.get(position));
    }

    @Override
    public int getItemCount() {
        return mRecipients.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private AvatarView av;
        private TextView tvName;
        private TextView tvEmail;
        private Button btRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            av = itemView.findViewById(R.id.user_avatar);
            tvName = itemView.findViewById(R.id.recipient_name);
            tvEmail = itemView.findViewById(R.id.recipient_email);
            btRemove = itemView.findViewById(R.id.remove_recipients);
            btRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    if (onRemoveRecipientListener != null) {
                        onRemoveRecipientListener.onRemoveRecipient(mRecipients.get(pos), pos);
                    }
                }
            });
        }

        void bandData(final Recipient r) {
            Bitmap b = r.getRecipientAvatar();
            if (b != null) {
                av.setImageBitmap(b);
            } else {
                av.setImageDrawable(new AvatarPlaceholder(mCtx,
                        r.getRecipientEmail(), 30, "skydrm.com", " "));
            }
            tvName.setText(r.getRecipientName());
            tvEmail.setText(r.getRecipientEmail());
        }
    }

    public interface OnRemoveRecipientListener {
        void onRemoveRecipient(Recipient email, int position);
    }
}

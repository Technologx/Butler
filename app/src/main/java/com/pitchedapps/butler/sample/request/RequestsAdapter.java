package com.pitchedapps.butler.sample.request;

import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.pitchedapps.butler.iconrequest.App;
import com.pitchedapps.butler.iconrequest.IconRequest;
import com.pitchedapps.butler.sample.R;

import java.util.ArrayList;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsHolder> {

    @Nullable
    public ArrayList<App> getApps() {
        if (IconRequest.get() != null)
            return IconRequest.get().getApps();
        return null;
    }

    @Override
    public RequestsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_to_request, parent, false);
        return new RequestsHolder(view);
    }

    @Override
    public int getItemCount() {
        return getApps() != null ? getApps().size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RequestsHolder holder, int position) {
        //noinspection ConstantConditions
        final App app = getApps().get(holder.getAdapterPosition());
        app.loadIcon(holder.imgIcon, Priority.NORMAL);
        final IconRequest ir = IconRequest.get();
        holder.setupItem(ir, app);
    }

    public void unselectAllApps() {
        IconRequest r = IconRequest.get();
        if (r != null) {
            r.unselectAllApps();
            notifyDataSetChanged();
        }
    }

    public class RequestsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgIcon;
        private TextView txtName;
        private AppCompatCheckBox checkBox;

        public RequestsHolder(View v) {
            super(v);
            imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
            txtName = (TextView) v.findViewById(R.id.txtName);
            checkBox = (AppCompatCheckBox) v.findViewById(R.id.chkSelected);
            v.setOnClickListener(this);
        }

        public void setupItem(IconRequest ir, App app) {
            txtName.setText(app.getName());
            checkBox.setChecked(ir.isAppSelected(app));
        }

        @Override
        public void onClick(View view) {
            final IconRequest ir = IconRequest.get();
            if (ir != null && ir.getApps() != null) {
                final App app = ir.getApps().get(getAdapterPosition());
                ir.toggleAppSelected(app);
                checkBox.setChecked(ir.isAppSelected(app));
            }
        }

    }

}
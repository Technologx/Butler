package com.pitchedapps.butler.request;

import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pitchedapps.butler.R;
import com.pitchedapps.butler.library.icon.request.App;
import com.pitchedapps.butler.library.icon.request.IconRequest;

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
        return new RequestsHolder(view, viewType);
    }

    @Override
    public int getItemCount() {
        return getApps() != null ? getApps().size() : 0;
    }

    @Override
    public int getItemViewType (int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RequestsHolder holder, int position) {
        //noinspection ConstantConditions
        final App app = getApps().get(position);
        app.loadIcon(holder.imgIcon);

        holder.txtName.setText(app.getName());
        final IconRequest ir = IconRequest.get();
        holder.itemView.setActivated(ir != null && ir.isAppSelected(app));
    }

    public class RequestsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imgIcon;
        final TextView txtName;
        final AppCompatCheckBox checkBox;
        final int position;

        public RequestsHolder(View v, int i) {
            super(v);
            imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
            txtName = (TextView) v.findViewById(R.id.txtName);
            checkBox = (AppCompatCheckBox) v.findViewById(R.id.chkSelected);
            position = i;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final IconRequest ir = IconRequest.get();
            if (ir != null && ir.getApps() != null) {
                final App app = ir.getApps().get(position);
                ir.toggleAppSelected(app);
                checkBox.setChecked(ir.isAppSelected(app));
            }
        }

    }

}
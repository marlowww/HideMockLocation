package com.github.marlowww.hidemocklocation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;


interface OnAppCheckChangedListener {
    void appsItemCheckChanged(AppItem item);
}


public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    @BindString(R.string.whitelist)
    String whitelist;
    @BindString(R.string.blacklist)
    String blacklist;

    private AppItem[] apps;
    private List<OnAppCheckChangedListener> listeners = new ArrayList<>();

    public void setOnCheckChangedListener(OnAppCheckChangedListener toAdd) {
        listeners.add(toAdd);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_icon)
        ImageView itemIcon;
        @BindView(R.id.item_title)
        TextView itemTitle;
        @BindView(R.id.item_checkBox)
        CheckBox itemCheckBox;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public AppsAdapter(AppItem[] apps) {
        this.apps = apps;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item_view, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemTitle.setText(apps[position].getName());
        holder.itemIcon.setImageDrawable(apps[position].getIcon());
        holder.itemCheckBox.setChecked(apps[position].isChecked());
        holder.itemCheckBox.setTag(apps[position]);

        holder.itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                AppItem item = (AppItem) cb.getTag();
                item.setChecked(cb.isChecked());

                for (OnAppCheckChangedListener l : listeners)
                    l.appsItemCheckChanged(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        return apps.length;
    }

    public AppItem[] getApps() {
        return apps;
    }

    public Set<String> getCheckedAppsPackageName() {
        Set<String> checkedApps = new HashSet<>();

        for (AppItem item : apps) {
            if (item.isChecked())
                checkedApps.add(item.getPackageName());
        }
        return checkedApps;
    }

}


package com.github.marlowww.hidemocklocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @BindView(R.id.toolbar)
    Toolbar toolbarView;
    @BindView(R.id.app_list)
    RecyclerView appListView;
    @BindView(R.id.list_type)
    SwitchCompat listSwitch;
    @BindView(R.id.xposed_disabled)
    TextView xposedDisabledView;
    @BindView(R.id.xposed_disabled_sub)
    TextView xposedDisabledSubView;
    @BindView(R.id.switch_layout)
    RelativeLayout switchLayout;
    @BindView(R.id.apps_count)
    TextView appsCountView;

    @BindString(R.string.whitelist)
    String whitelistStr;
    @BindString(R.string.blacklist)
    String blacklistStr;
    @BindString(R.string.donate_url)
    String donateUrlStr;
    @BindColor(R.color.colorAccent)
    int accentColor;
    @BindColor(R.color.colorPrimaryDark)
    int primaryDarkColor;
    @BindColor(R.color.colorDark)
    int darkColor;

    public static SharedPreferences prefs;

    private AppsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load settings
        prefs = getSharedPreferences(Common.PACKAGE_PREFERENCES, MODE_WORLD_READABLE);

        final Common.ListType listType = getListType();
        final Set<String> checkedApps = prefs.getStringSet(listType.toString(), new HashSet<String>());


        // Get information about apps
        final PackageManager pm = getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> infoList = new ArrayList<>(pm.queryIntentActivities(mainIntent, 0));
        Collections.sort(infoList, new ResolveInfo.DisplayNameComparator(pm));

        final ArrayList<AppItem> apps = new ArrayList<>(infoList.size());

        int checked = 0; // Checked apps counter
        for (ResolveInfo info : infoList) {
            if (info.activityInfo != null) {
                final CharSequence label = info.loadLabel(pm);
                final Drawable icon = info.loadIcon(pm);

                if (checkedApps.contains(info.activityInfo.packageName)) {
                    apps.add(new AppItem(label, icon, info.activityInfo.packageName, true));
                    checked++;
                } else
                    apps.add(new AppItem(label, icon, info.activityInfo.packageName));
            }
        }

        // End of splash screen
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbarView);

        enableColorStatusBar();

        setListSwitch(listType);

        adapter = new AppsAdapter(apps);
        adapter.setOnCheckChangedListener(new OnAppCheckChangedListener() {
            @Override
            public void appsItemCheckChanged(AppItem item) {
                Common.ListType listType = getListType();
                Set<String> checkedApps = adapter.getCheckedAppsPackageNames();

                appsCountView.setText(getString(R.string.checked, checkedApps.size()));

                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet(listType.toString(), checkedApps);
                editor.apply();
            }
        });

        appListView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        appListView.setLayoutManager(layoutManager);
        appListView.setAdapter(adapter);


        appsCountView.setText(getString(R.string.checked, checked));
        appsCountView.setTextColor(listType.equals(Common.ListType.WHITELIST)
                ? accentColor : darkColor);
    }

    // Return true if XposedModule is enabled (self hook)
    @SuppressWarnings("all")
    private boolean isModuleEnabled() {
        // Using just "return false;" doesn't work on all devices. ART can optimize this,
        // by placing inline "false" directly in code, which prevents Xposed self-hook from working.
        return Boolean.valueOf(false);
    }

    private Common.ListType getListType() {
        String listTypeStr = prefs.getString(Common.PREF_LIST_TYPE,
                Common.ListType.BLACKLIST.toString());
        return listTypeStr.equals(Common.ListType.BLACKLIST.toString())
                ? Common.ListType.BLACKLIST : Common.ListType.WHITELIST;
    }

    private void setListSwitch(Common.ListType listType) {
        listSwitch.setText(listType.equals(Common.ListType.WHITELIST) ? whitelistStr : blacklistStr);
        listSwitch.setTextColor(listType.equals(Common.ListType.WHITELIST) ? accentColor : darkColor);
        listSwitch.setChecked(listType.equals(Common.ListType.WHITELIST));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isModuleEnabled()) {
            appListView.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            xposedDisabledView.setVisibility(View.VISIBLE);
            xposedDisabledSubView.setVisibility(View.VISIBLE);
        }
        else {
            appListView.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            xposedDisabledView.setVisibility(View.GONE);
            xposedDisabledSubView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        File prefsDir = new File(getApplicationInfo().dataDir, "shared_prefs");
        File prefsFile = new File(prefsDir, Common.PACKAGE_PREFERENCES + ".xml");
        if (prefsFile.exists()) {
            prefsFile.setReadable(true, false);
        }
    }

    private void enableColorStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(primaryDarkColor);
        }
    }

    @OnCheckedChanged(R.id.list_type)
    public void changeListType(boolean isChecked) {
        if (adapter != null) {
            Common.ListType listType = isChecked ? Common.ListType.WHITELIST : Common.ListType.BLACKLIST;
            setListSwitch(listType);
            appsCountView.setTextColor(listType.equals(Common.ListType.WHITELIST) ? accentColor : darkColor);

            List<AppItem> apps = adapter.getApps();
            List<AppItem> allApps = adapter.getAllApps();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Common.PREF_LIST_TYPE, listType.toString());
            editor.apply();

            Set<String> checkedApps = prefs.getStringSet(listType.toString(), new HashSet<String>());

            // Count again to prevent situation when in prefs are uninstalled apps
            // Change all apps in adapter
            int checked = 0;
            for (AppItem app : allApps) {
                if (checkedApps.contains(app.getPackageName())) {
                    app.setChecked(true);
                    checked++;
                } else app.setChecked(false);
            }
            // Change currently viewed apps
            for (AppItem app : apps) {
                if (checkedApps.contains(app.getPackageName())) {
                    app.setChecked(true);
                } else app.setChecked(false);
            }
            adapter.notifyDataSetChanged();
            appsCountView.setText(getString(R.string.checked, checked));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Resize searchView
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {;
                adapter.filter(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_donate:
                Uri uri = Uri.parse(donateUrlStr);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

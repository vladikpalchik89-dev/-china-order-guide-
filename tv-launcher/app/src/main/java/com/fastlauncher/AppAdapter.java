package com.fastlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppAdapter extends BaseAdapter {
    public static class AppInfo {
        public final String label;
        public final String pkg;
        public boolean favorite;

        AppInfo(String label, String pkg) {
            this.label = label;
            this.pkg = pkg;
        }
    }

    private static class Holder {
        ImageView icon;
        TextView label;
        TextView star;
    }

    private final Context ctx;
    private final PackageManager pm;
    private final LayoutInflater inflater;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final LruCache<String, Drawable> icons = new LruCache<>(160);
    private final SharedPreferences prefs;
    private final Drawable placeholder;
    private final List<AppInfo> all = new ArrayList<>();
    private final List<AppInfo> shown = new ArrayList<>();

    private String query = "";
    private boolean favoritesOnly = false;

    AppAdapter(Context c) {
        ctx = c;
        pm = c.getPackageManager();
        inflater = LayoutInflater.from(c);
        prefs = c.getSharedPreferences("launcher", Context.MODE_PRIVATE);
        placeholder = c.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        loadApps();
    }

    private Set<String> favorites() {
        return new HashSet<>(prefs.getStringSet("favorites", new HashSet<String>()));
    }

    private void loadApps() {
        Set<String> fav = favorites();
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolve = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo r : resolve) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(ctx.getPackageName())) continue;
            String label = r.loadLabel(pm).toString();
            AppInfo app = new AppInfo(label, pkg);
            app.favorite = fav.contains(pkg);
            all.add(app);
        }
        Collections.sort(all, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        apply("", favoritesOnly);
    }

    void apply(String q, boolean favOnly) {
        query = q == null ? "" : q;
        favoritesOnly = favOnly;
        shown.clear();
        String needle = query.toLowerCase(Locale.getDefault());
        for (AppInfo a : all) {
            if (favoritesOnly && !a.favorite) continue;
            if (needle.length() > 0 && !a.label.toLowerCase(Locale.getDefault()).contains(needle)
                    && !a.pkg.toLowerCase(Locale.getDefault()).contains(needle)) continue;
            shown.add(a);
        }
        if (needle.length() == 0) {
            Collections.sort(shown, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo a, AppInfo b) {
                    if (a.favorite != b.favorite) return a.favorite ? -1 : 1;
                    return a.label.compareToIgnoreCase(b.label);
                }
            });
        }
        notifyDataSetChanged();
    }

    boolean isFavoritesOnly() {
        return favoritesOnly;
    }

    void toggleFavorite(int position) {
        if (position < 0 || position >= shown.size()) return;
        AppInfo a = shown.get(position);
        a.favorite = !a.favorite;
        Set<String> fav = favorites();
        if (a.favorite) fav.add(a.pkg); else fav.remove(a.pkg);
        prefs.edit().putStringSet("favorites", fav).apply();
        apply(query, favoritesOnly);
    }

    @Override
    public int getCount() {
        return shown.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return shown.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder h;
        if (v == null) {
            v = inflater.inflate(R.layout.item_app, parent, false);
            h = new Holder();
            h.icon = (ImageView) v.findViewById(R.id.icon);
            h.label = (TextView) v.findViewById(R.id.label);
            h.star = (TextView) v.findViewById(R.id.star);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        final AppInfo app = getItem(position);
        h.label.setText(app.label);
        h.star.setVisibility(app.favorite ? View.VISIBLE : View.GONE);
        h.icon.setTag(app.pkg);

        Drawable cached = icons.get(app.pkg);
        if (cached != null) {
            h.icon.setImageDrawable(cached);
        } else {
            h.icon.setImageDrawable(placeholder);
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    Drawable d = null;
                    try {
                        Intent launch = pm.getLaunchIntentForPackage(app.pkg);
                        if (launch != null) {
                            ResolveInfo ri = pm.resolveActivity(launch, 0);
                            if (ri != null) d = ri.loadIcon(pm);
                        }
                    } catch (Exception ignored) {
                    }
                    if (d != null) icons.put(app.pkg, d);
                    final Drawable result = d;
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (app.pkg.equals(h.icon.getTag())) {
                                h.icon.setImageDrawable(result != null ? result : placeholder);
                            }
                        }
                    });
                }
            });
        }
        return v;
    }
}

package com.github.marlowww.hidemocklocation;

import android.os.Build;


public class Common {

    public enum ListType {
        BLACKLIST("blacklist"),
        WHITELIST("whitelist");

        private final String text;

        ListType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final String PACKAGE_NAME = Common.class.getPackage().getName();
    public static final String ACTIVITY_NAME = Common.class.getPackage().getName() + ".MainActivity";
    public static final String PACKAGE_PREFERENCES = PACKAGE_NAME + "_preferences";

    public static final String PREF_LIST_TYPE = "list_type";

    public static final int SDK = Build.VERSION.SDK_INT;
    public static final boolean JB_MR2_NEWER = SDK >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final String INTENT_APPS_LIST = "apps_resolve_info_list";

}

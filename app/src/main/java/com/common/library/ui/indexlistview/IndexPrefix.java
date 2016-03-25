package com.common.library.ui.indexlistview;

import com.common.library.orm.sqlite.BaseTable;

/**
 * Created by zf08526 on 2015/4/28.
 */
@SuppressWarnings("serial")
public class IndexPrefix extends BaseTable{
    public static final String FIELD_PREFIX_TYPE = "prefix_type";
    public static final String FIELD_EXTRA_VALUE = "extra_value";
    public static final String FIELD_IS_TITLE_ITEM = "is_title";

    public static final String PREFIX_TYPE_CURRENT_CITY = "当前";
    public static final String PREFIX_TYPE_HISTORY_CITY = "历史";
    public static final String PREFIX_TYPE_HOT_CITY = "热门";
}

package com.common.library.ui.indexlistview;

import java.util.ArrayList;
import java.util.List;

import com.common.library.orm.sqlite.BaseTable;
import com.common.library.orm.sqlite.DbUtils;

/**
 * Arguments required by
 * {@link com.tongcheng.android.library.sdk.indexlistview.IndexListViewActivity}
 * and supported by sub-class of
 * {@link com.tongcheng.android.library.sdk.indexlistview.IndexListViewActivity}
 *
 * @author zf08526
 */
public class Arguments {
    private DbUtils dbUtils;
    private Class<? extends BaseTable> tableClass;
    private String pinyinColumnName;
    private String pyColumnName;
    private String dataColumnName;
    private List<String> currentCity;
    private List<String> historyCity;
    private List<String> hotCity;

    public DbUtils getDbUtils() {
        return dbUtils;
    }

    public void setDbUtils(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public Class<? extends BaseTable> getTableClass() {
        return tableClass;
    }

    public void setTableClass(Class<? extends BaseTable> tableClass) {
        this.tableClass = tableClass;
    }

    public String getPinyinColumnName() {
        return pinyinColumnName;
    }

    public void setPinyinColumnName(String pinyinColumnName) {
        this.pinyinColumnName = pinyinColumnName;
    }

    public String getPyColumnName() {
        return pyColumnName;
    }

    public void setPyColumnName(String pyColumnName) {
        this.pyColumnName = pyColumnName;
    }

    public String getDataColumnName() {
        return dataColumnName;
    }

    public void setDataColumnName(String dataColumnName) {
        this.dataColumnName = dataColumnName;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = new ArrayList<String>();
        this.currentCity.add(currentCity);
    }

    public void setCurrentCity(List<String> currentCity) {
        this.currentCity = currentCity;
    }

    public List<String> getCurrentCity() {
        return currentCity;
    }

    public List<String> getHistoryCity() {
        return historyCity;
    }

    public void setHistoryCity(List<String> historyCity) {
        this.historyCity = historyCity;
    }

    public List<String> getHotCity() {
        return hotCity;
    }

    public void setHotCity(List<String> hotCity) {
        this.hotCity = hotCity;
    }
}

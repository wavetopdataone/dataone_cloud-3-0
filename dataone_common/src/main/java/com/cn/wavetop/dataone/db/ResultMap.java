package com.cn.wavetop.dataone.db;



import com.cn.wavetop.dataone.util.StringFormat;

import java.util.*;

public class ResultMap {
    List _list = new ArrayList();

    public List getList() {
        return _list;
    }

    public void setList(List list) {
        this._list = list;
    }

    //int totalSize = 0;

    public void addResult(Map map){
        _list.add(map);
    }

    public void remove(int i){
        this._list.remove(i);
    }
    @Override
    public String toString() {
        return this._list.toString();
    }

    public String getResult(int i, String name){
        if(i>=_list.size())
            return "";
        Map map = (Map)_list.get(i);
        String value = (String)map.get(name.toUpperCase());
        if(value == null)
            value = "";
        return value;
    }

    public String get(int i,String name){
        return getResult(i,name);
    }

    public Map get(int i){
        return (Map) this._list.get(i);
    }


    public String get(int i,String name,String defaultValue){
        String value = getResult(i,name);
        if(StringFormat.isNullOrBlank(value))
            value = defaultValue;
        return value;
    }

    public int size(){
        return _list.size();
    }

    public void clear(){
        for(int i=0;i<_list.size();i++){
            Map map = (Map)_list.get(i);
            map.clear();
        }
        _list.clear();
    }

    public static Map<String,String> cloneMap(Map<String,String> map){
        Set keySet = map.keySet();
        Map<String,String> map2 = new HashMap<String,String>();
        for(Iterator<String> iter = keySet.iterator(); iter.hasNext();){
            String name = iter.next();
            String value = (String)map.get(name);
            map2.put(name, value);
        }
        return map2;
    }

    /**
     * @return Returns the totalSize.
     */
//	public int getTotalSize() {
//		return totalSize;
//	}

    /**
     * @param totalSize The totalSize to set.
     */
//	public void setTotalSize(int totalSize) {
//		this.totalSize = totalSize;
//	}

}

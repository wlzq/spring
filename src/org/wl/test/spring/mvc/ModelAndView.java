package org.wl.test.spring.mvc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wl
 * @date 2018/12/31 21:32
 * @description
 */
public class ModelAndView {

    private String viewName;
    private Map<String, Object> data = new HashMap<>();

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public void addAttribute(String name, Object value){
        data.put(name, value);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

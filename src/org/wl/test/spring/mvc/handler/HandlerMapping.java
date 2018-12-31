package org.wl.test.spring.mvc.handler;

import java.lang.reflect.Method;

/**
 * @author wl
 * @date 2018/12/31 9:27
 * @description
 */
public class HandlerMapping {

    private String url;
    private Method method;
    private Object controllerInstance;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    public void setControllerInstance(Object controllerInstance) {
        this.controllerInstance = controllerInstance;
    }
}

package org.wl.test.spring.mvc.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author wl
 * @date 2018/12/31 9:27
 * @description
 */
public class HandlerAdapter {

    private Map<Integer, String> paramMap;

    public HandlerAdapter(Map<Integer, String> paramMap){
        this.paramMap = paramMap;
    }

    public Object handler(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) throws Exception {
        Method method = handlerMapping.getMethod();
        Object classInstance = handlerMapping.getControllerInstance();

        int paramNum = method.getParameterCount();
        Object[] paramObj = new Object[paramNum];
        for(int i=0; i<paramNum; i++){
            String paramName = paramMap.get(i);
            if(paramName.equals(HttpServletRequest.class.getName())){
                paramObj[i] = request;
            }else if(paramName.equals(HttpServletResponse.class.getName())){
                paramObj[i] = response;
            } else {
                paramObj[i] = request.getParameter(paramName);
            }
        }
        Object result = method.invoke(classInstance, paramObj);
        return result;
    }


    public Map<Integer, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<Integer, String> paramMap) {
        this.paramMap = paramMap;
    }
}

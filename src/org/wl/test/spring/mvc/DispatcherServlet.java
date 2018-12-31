package org.wl.test.spring.mvc;

import org.wl.test.spring.ioc.ApplicationContext;
import org.wl.test.spring.mvc.annotation.RequestMapping;
import org.wl.test.spring.mvc.annotation.RequestParam;
import org.wl.test.spring.mvc.handler.HandlerAdapter;
import org.wl.test.spring.mvc.handler.HandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wl
 * @date 2018/12/29 23:11
 * @description mvc 模拟实现
 */
public class DispatcherServlet extends HttpServlet {

    private List<HandlerMapping> handlerMappingList = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping, HandlerAdapter> adapterMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // web.xml 配置核心servlet 获取配置的信息
        String configFile = config.getInitParameter("contextConfigLocation");
        //定义一个当前上下文对象，实现基础包的扫描、IOC、DI
        ApplicationContext context = new ApplicationContext(configFile.replace("classpath:", ""));
        //获取扫描到的有Controller注解的类
        List<Object> controllerList = context.getControllerList();
        //初始化HandlerMapping
        initHandlerMapping(controllerList);
        //初始化HandlerAdapter
        initHandlerAdapter();
    }


    private void initHandlerAdapter() {
        if (handlerMappingList.size() == 0) {
            return;
        }

        handlerMappingList.forEach(handlerMapping -> {
            Method method = handlerMapping.getMethod();
            //方法的参数  <参数索引，参数名字>
            Map<Integer, String> paramMap = new HashMap<>();

            //使用了注解参数
            Annotation[][] annos = method.getParameterAnnotations();
            if(annos.length > 0){
                for(int i=0; i<annos.length; i++){
                    for(Annotation anno : annos[i]){
                        if(anno instanceof RequestParam){
                            RequestParam requestParam = (RequestParam) anno;
                            String paramName = requestParam.value();

                            paramMap.put(i, paramName);
                        }
                    }
                }
            }
            //直接用的servlet参数，如HttpServletRequest
            Class<?>[] paramTypes = method.getParameterTypes();
            if(paramTypes.length > 0){
                for(int i=0; i<paramTypes.length; i++){
                    Class<?> typeClass = paramTypes[i];
                    if (typeClass == HttpServletRequest.class || typeClass == HttpServletResponse.class) {
                        String paramName = typeClass.getName();

                        paramMap.put(i, paramName);
                    }
                }
            }

            HandlerAdapter handlerAdapter = new HandlerAdapter(paramMap);
            adapterMap.put(handlerMapping, handlerAdapter);
        });
    }

    /**
     * 完成请求方法与请求处理实例的映射关系
     * @param controllerList
     */
    private void initHandlerMapping(List<Object> controllerList) {
        if(controllerList.size() == 0){
            return;
        }

        controllerList.forEach(controllerObj -> {
            //类上的请求路径
            String classRequestUrl = "";
            if (controllerObj.getClass().isAnnotationPresent(RequestMapping.class)) {
                RequestMapping classRequestMapping = controllerObj.getClass().getAnnotation(RequestMapping.class);
                if(classRequestMapping != null){
                    classRequestUrl += urlHandler(classRequestMapping.value());
                }
            }
            //方法上的请求路径
            Method[] methods = controllerObj.getClass().getDeclaredMethods();
            if(methods.length > 0){
                for(int i=0; i<methods.length; i++){
                    String methodRequestUrl = "";
                    Method method = methods[i];
                    //必须是public修饰的方法
                    if(method.getModifiers() == Modifier.PUBLIC){
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                            if(methodRequestMapping != null){
                                methodRequestUrl += urlHandler(methodRequestMapping.value());
                            }

                            String requestUrl = classRequestUrl + methodRequestUrl;

                            HandlerMapping handlerMapping = new HandlerMapping();
                            handlerMapping.setMethod(method);
                            handlerMapping.setUrl(requestUrl);

                            handlerMapping.setControllerInstance(controllerObj);
                            handlerMappingList.add(handlerMapping);
                        }
                    }

                }
            }

        });

    }

    /**
     * url处理
     * @param url
     * @return
     */
    public String urlHandler( String url){
        if(!url.startsWith("/")){
            url = "/" + url;
        }
        if(url.endsWith("/")){
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req, resp);
    }

    /**
     * 请求处理
     * @param req
     * @param resp
     */
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String contextUrl = req.getContextPath();
        String requestUrl = req.getRequestURI();

        String url = requestUrl.replace(contextUrl, "");
        HandlerMapping handlerMapping = null;
        for(int i=0; i<handlerMappingList.size(); i++){
            if(url.equals(handlerMappingList.get(i).getUrl())){
                handlerMapping = handlerMappingList.get(i);
                break;
            }
        }
        if(handlerMapping == null){
            resp.getWriter().write("404, 未知的请求！");
        }else{
            HandlerAdapter adapter = adapterMap.get(handlerMapping);
            try {
                Object result = adapter.handler(req, resp, handlerMapping);

                viewResolve(req, resp, result);
            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("500, 服务器发生异常！");
            }
        }

    }

    /**
     * 视图解析 返回
     * @param result
     */
    private void viewResolve(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception{
        if (result.getClass() == ModelAndView.class) {
            ModelAndView mv = (ModelAndView) result;
            String view = mv.getViewName();
            Map<String, Object> dataMap = mv.getData();
            if(dataMap.size() > 0){
                for(String key : dataMap.keySet()){
                    request.setAttribute(key, dataMap.get(key));
                }
            }
            request.getRequestDispatcher(view).forward(request, response);
        }
    }

}

package org.wl.test.spring.ioc;

import org.wl.test.demo.controller.UserController;
import org.wl.test.spring.ioc.annotation.Autowired;
import org.wl.test.spring.ioc.annotation.Controller;
import org.wl.test.spring.ioc.annotation.Qualifier;
import org.wl.test.spring.ioc.annotation.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * @author wl
 * @date 2018/12/22 11:13
 * @description
 */
public class ApplicationContext {

    /**
     * 配置文件
     */
    private static String PROPERTIES_FILE = "";

    /**
     * 初始化一个集合，存放扫描到的class对象
     */
    private List<Class<?>> classList = Collections.synchronizedList(new ArrayList<>());

    /**
     * 初始化map 存放别名与对象实例
     */
    private Map<String, Object> aliasInstanceMap = new HashMap<>();

    public ApplicationContext(String fileName) {
        PROPERTIES_FILE = fileName;
        try {
            String basePackage = getBasePackage(PROPERTIES_FILE);

            buildAliasInstanceMap(basePackage);

            doAutowired();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 完成别名与实例的映射
     */
    public void buildAliasInstanceMap(String basePackage) throws Exception {

        scanClasses(basePackage);

        if(classList.size() == 0){return;}

        for(Class<?> clazz : classList){
            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)
                || clazz.isAnnotationPresent(Autowired.class)) {
                String alias = getAlias(clazz);
                Object obj = aliasInstanceMap.get(alias);

                //如果别名实例映射关系已经存在，则给出提示
                if(obj != null){
                    throw new Exception("alias is exist!");
                }else{
                    aliasInstanceMap.put(alias, clazz.newInstance());
                }
            }
        }

        System.out.println(aliasInstanceMap);
    }

    /**
     * 属性对象的注入
     */
    public void doAutowired(){
        if (aliasInstanceMap.size() == 0) {
            return;
        }

        aliasInstanceMap.forEach((k, v)->{

            Field[] fields = v.getClass().getDeclaredFields();

            for(Field field : fields){
                if (field.isAnnotationPresent(Autowired.class)) {
                    String alias = "";

                    Autowired autowired = field.getAnnotation(Autowired.class);
                    if(autowired != null){
                        //注入的对象是接口时，由于不知道接口有几个实现类，所以就必须在Autowired或者Qualifier上指定要注解的具体的实现类
                        if(!"".equals(autowired.value())){
                            alias = autowired.value();
                        }else{
                            Qualifier qualifier = field.getAnnotation(Qualifier.class);
                            if(qualifier != null){
                                alias = qualifier.value();
                            }
                        }
                    }

                    if ("".equals(alias)) {
                        alias = getAlias(field.getType());
                    }

                    Object instance = null;
                    if(!"".equals(alias)){
                        instance = aliasInstanceMap.get(alias);
                    }

                    field.setAccessible(true);

                    try {
                        field.set(v, instance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    /**
     * 获取对象的别名，如果注解中配置了别名，别使用配置的别名，否则默认使用类名首字母小写
     * @param clazz
     * @return
     */
    public String getAlias(Class<?> clazz){
        String alias = "";
        Controller controller = clazz.getAnnotation(Controller.class);
        if(controller != null){
            alias = controller.value();
        }
        Service service = clazz.getAnnotation(Service.class);
        if (service != null) {
            alias = service.value();
        }
        Autowired autowired = clazz.getAnnotation(Autowired.class);
        if(autowired != null){
            alias = autowired.value();
        }

        //注解中没有配置别名
        if("".equals(alias)){
            String simpleName = clazz.getSimpleName();
            alias = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        }
        return alias;
    }

    /**
     * 跟据基础包名读取包及子包中的类对象
     * @param basePackage
     */
    public void scanClasses(String basePackage){
        if(basePackage == null || "".equals(basePackage)){return;}

        doScan(basePackage);
        System.out.println(classList);
    }

    private void doScan(String basePackage) {
        String path = basePackage.replaceAll("\\.","/");
        URL url = this.getClass().getClassLoader().getResource(path);
        File file = new File(url.getFile());
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File childFile) {
                String fileName = childFile.getName();
                if(childFile.isDirectory()){
                    //当前文件是目录，递归 扫描下级子目录下的class文件
                    doScan(basePackage + "." + fileName);
                }else{
                    if(fileName.endsWith(".class")){
                        String className = basePackage + "." + fileName.replace(".class", "");
                        try {
                            Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
                            classList.add(clazz);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 从配置的属性文件中读取要扫描的包
     * @return
     */
    public String getBasePackage(String fileName) throws IOException {
        String basePackage;
        Properties prop = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        prop.load(in);
        basePackage = prop.getProperty("basePackage");
        return basePackage;
    }

    /**
     * 根据beanName 获取
     * @param beanName
     * @return
     */
    public Object getBean(String beanName){
        return aliasInstanceMap.get(beanName);
    }

    /**
     * 获取所有标注了controller的注解
     * @return
     */
    public List<Object> getControllerList(){
        List<Object> controllerList = new ArrayList<>();
        if(aliasInstanceMap.size() > 0) {
            aliasInstanceMap.values().forEach(obj -> {
                if(obj.getClass().isAnnotationPresent(Controller.class)){
                    controllerList.add(obj);
                }
            });
        }
        return controllerList;
    }

    public static void main(String[] args) throws Exception {
        String fileName = "application.properties";
        ApplicationContext context = new ApplicationContext(fileName);
        String basePackage = context.getBasePackage(PROPERTIES_FILE);

        context.buildAliasInstanceMap(basePackage);

        context.doAutowired();
        //测试
        UserController controller = (UserController) context.getBean("userController");
        controller.save();
    }


}

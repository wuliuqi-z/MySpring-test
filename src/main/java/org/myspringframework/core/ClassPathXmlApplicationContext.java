package org.myspringframework.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathXmlApplicationContext implements ApplicationContext{
//    private  String configLocation;
//    public ClassPathXmlApplicationContext() {
//
//    }
    private Map<String,Object> singletonObjects=new HashMap<>();

    private static final Logger logger= LoggerFactory.getLogger(ClassPathXmlApplicationContext.class);

    /**
     * 解析spring的配置文件，然后初始化所有的bean对象
     * 注意文件应该放到类路径下
     * @param configLocation
     */
    public ClassPathXmlApplicationContext(String configLocation) {
        try {
            //解析配置文件，然后实例化bean，将bean存放到集合中
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
            //这个是dom4j解析xml文件的核心对象
            SAXReader reader=new SAXReader();
            Document document=reader.read(is);
            List<Node> nodes = document.selectNodes("//bean");
            nodes.forEach(node -> {
                try {
                    //                为了使用element接口中更加丰富的方法
                    Element element=(Element) node;
                    String id = element.attributeValue("id");
                    String className = element.attributeValue("class");
                    logger.info("bean name="+id);
                    logger.info("beanClassName="+className);
//                通过反射机制创建对象，将其放到map集合中提前曝光
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                    Object beanObj = declaredConstructor.newInstance();//调用无参数构造方法实例化对象
//                    将bean曝光，加入map集合
                    singletonObjects.put(id,beanObj);
//                    记录日志，已经曝光
                    logger.info(singletonObjects.toString());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
//            再次重新把所有的bean标签都遍历一次，这次主要给对象的属性辅助
            nodes.forEach(node -> {
                Element beanElt=(Element) node;
                String id = beanElt.attributeValue("id");
                String className = beanElt.attributeValue("class");
//                获取该bean标签下所有的属性标签
                List<Element> properties = beanElt.elements("property");
                properties.forEach(property->{
                    try {
                        String name = property.attributeValue("name");
                        String value = property.attributeValue("value");
                        String ref = property.attributeValue("ref");
                        logger.info(name+":"+value);
//                    获取set方法名
                        String setMethodName="set"+name.toUpperCase().charAt(0)+name.substring(1);
                        Class<?> clazz = Class.forName(className);
                        Field declaredField = clazz.getDeclaredField(name);
                        Class<?> type = declaredField.getType();
                        String typeName=type.getSimpleName();
//                        你得加上参数类型，才能得到你需要的方法，不然的话，它会认为你没有参数，就调没有参数的那个方法，结果没有
                        Method declaredMethod = clazz.getDeclaredMethod(setMethodName,type);
                        Object actualObject=null;
//                        给属性赋值
                        if(value!=null){
//                            mySpring框架申明一下，我们只支持这些类型为简单类型 byte short int long float double boolean char以及他们的包装类，以及String
                            switch (typeName){
                                case "int" :actualObject=Integer.parseInt(value);
                                break;
                            }
                            declaredMethod.invoke(singletonObjects.get(id),actualObject);
                        }else if(ref!=null){
                            declaredMethod.invoke(singletonObjects.get(id),singletonObjects.get(ref));
                        }

                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

            });
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}

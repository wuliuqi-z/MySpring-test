package org.myspringframework.core;

/**
 * MySpring框架应用上下文接口
 */
public interface ApplicationContext {
    /**
     * 根据bean的名称获取bean对象
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}

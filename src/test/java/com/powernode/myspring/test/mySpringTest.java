package com.powernode.myspring.test;

import com.powernode.myspring.bean.UserService;
import org.junit.Test;
import org.myspringframework.core.ApplicationContext;
import org.myspringframework.core.ClassPathXmlApplicationContext;

public class mySpringTest {
    @Test
    public void testMySpring(){
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("spring.xml");
        Object userService = applicationContext.getBean("userService");
        ((UserService) userService).save();
    }
}

package com.atguigu.gmall0401.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Target ,修饰注解注在哪儿：这里是在method上
@Target(ElementType.METHOD)

@Retention(RetentionPolicy.RUNTIME)    // source  : override    class    runtime
public @interface LoginRequire {

	//定义参数
    boolean autoRedirect() default true;
}


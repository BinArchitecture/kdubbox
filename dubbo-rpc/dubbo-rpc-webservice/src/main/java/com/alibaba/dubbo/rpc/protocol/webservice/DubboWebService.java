package com.alibaba.dubbo.rpc.protocol.webservice;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DubboWebService
{
  public abstract String parameters() default "";
  public abstract String usrname() default "";
  public abstract String passwd() default "";
  public abstract String type() default "simple";
  public abstract boolean needLog() default false;
}
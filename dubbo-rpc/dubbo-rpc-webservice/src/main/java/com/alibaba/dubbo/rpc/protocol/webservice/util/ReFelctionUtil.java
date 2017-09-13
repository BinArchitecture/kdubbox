/*
 * [y] hybris Platform
 * 
 * Copyright (c) 2000-2015 hybris AG
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package com.alibaba.dubbo.rpc.protocol.webservice.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 *
 */
public class ReFelctionUtil
{
	@SuppressWarnings("unchecked")
	public static <T> T getDynamicObj(Class<?> clazz,String name,Object instance) throws Exception  {
		try {
			Field ff = clazz.getDeclaredField(name);
			ff.setAccessible(true);
			return (T)ff.get(instance);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw e;
		}
	}
	
	public static void setFinalValue(Object obj,Field field, Object newValue) throws Exception {
	      try {
			field.setAccessible(true);
			  Field modifiersField = Field.class.getDeclaredField("modifiers");
			  modifiersField.setAccessible(true);
			  modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			  field.set(obj, newValue);
			  modifiersField.setAccessible(false);  
			  field.setAccessible(false);
		} catch (SecurityException | NoSuchFieldException
				| IllegalArgumentException | IllegalAccessException e) {
			throw e;
		}  
	   }
	
	
	public static Object dynamicInvokePriMethods(Class<?> clazz,final String tbName, final Class<?>[] types,Object instance, final Object... args) throws Exception
	{
		try {
			Method method = clazz.getDeclaredMethod(tbName, types);
			method.setAccessible(true);
			return method.invoke(instance, args);
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw e;
		}
	}
}

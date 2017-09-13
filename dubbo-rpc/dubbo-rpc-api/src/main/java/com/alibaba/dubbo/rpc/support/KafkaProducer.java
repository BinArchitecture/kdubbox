package com.alibaba.dubbo.rpc.support;

public interface KafkaProducer<T> {
	public boolean sendMsg(final T t,String... key) throws Exception;
}

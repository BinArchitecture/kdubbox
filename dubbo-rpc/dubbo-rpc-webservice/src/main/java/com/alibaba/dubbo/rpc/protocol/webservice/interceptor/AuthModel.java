package com.alibaba.dubbo.rpc.protocol.webservice.interceptor;

public class AuthModel {
private String usrname;
private String passwd;
public AuthModel(String usrname, String passwd) {
	this.usrname=usrname;
	this.passwd=passwd;
}
public String getUsrname() {
	return usrname;
}
public void setUsrname(String usrname) {
	this.usrname = usrname;
}
public String getPasswd() {
	return passwd;
}
public void setPasswd(String passwd) {
	this.passwd = passwd;
}
}

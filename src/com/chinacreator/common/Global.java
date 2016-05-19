package com.chinacreator.common;

import java.util.Map;

import com.chinacreator.service.DataOprService;

public class Global {
	private static Map<String,String> conf=DataOprService.getInstance().getProp();
	//配置文件中监听的端口字段
	public static final String PORT="port";
	//配置文件中发送箱路径字段
	public static final String SEND_PATH="sendPath";
	//配置文件中收件箱路径字段
	public static final String RECEIVE_PATH="receivePath";
	//配置文件中程序主路径字段
	public static final String MAIN_PATH="mainPath";
	//配置文件中编码格式字段
	public static final String CHAR_FORMAT=conf.get("charFormat")==null?"GBK":conf.get("charFormat");
	
}

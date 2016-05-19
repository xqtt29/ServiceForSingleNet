package com.chinacreator.controller;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import com.chinacreator.common.Global;
import com.chinacreator.service.DataOprService;

/**
 * @Description
 * 服务端入口类
 * 
 * @Author qiang.zhu
 * @Datetime 2016年5月10日 上午9:37:05
 * @Version
 * @Copyright (c) 2013 湖南科创信息技术股份有限公司
 */
public class ServerTcpListener {

	//获取配置文件
	private static Map<String,String> conf=DataOprService.getInstance().getProp();
	
    public static void main(String[] args) {
        try {
            final ServerSocket receiveService = new ServerSocket(Integer.parseInt(conf.get(Global.PORT)==null?"8888":conf.get(Global.PORT).toString()));
			while (true) {
				try {
					final Socket socket = receiveService.accept();
					new Thread(new Runnable() {
						public void run() {
							OperatorService.getInstance().func(socket);
						}
					}).start();;
				} catch (Exception e) {
					DataOprService.getInstance().insertLog("main:while:ex:"+e.getStackTrace().toString(),conf.get(Global.MAIN_PATH));
				}
			}
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("main:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    
}


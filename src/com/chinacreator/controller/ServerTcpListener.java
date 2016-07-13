package com.chinacreator.controller;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import com.chinacreator.common.Global;
import com.chinacreator.service.DataOprService;
import com.chinacreator.service.LanSendService;

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
			Global.list=new ArrayList<Map<String,Object>>();
        	Global.lSend = new LanSendService(Global.list);
        	Global.lSend.join();     //加入组播，并创建线程侦听
        	new Thread(new Runnable() {
				public void run() {
					while(true){
						try {
				        	Global.lSend.sendGetUserMsg();    //广播信息，寻找上线主机交换信息
							Thread.currentThread().sleep(5*60000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();;
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


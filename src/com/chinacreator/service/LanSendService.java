package com.chinacreator.service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanSendService {
	//本机IP
	private String localIp;
	//本机主机名称
	private String localName;
	//广播IP
	private static final String BROADCAST_IP = "230.0.0.1";
	//广播port
	private static final int BROADCAST_INT_PORT = 40000;
	//用于接收广播信息
	MulticastSocket broadSocket;
	//广播地址
	InetAddress broadAddress;
	//数据流套接字,用于发送信息
	DatagramSocket sender;
	//
	private List<Map<String,Object>> list;
	
	public LanSendService(List<Map<String,Object>> list) {
		try {
			localIp=InetAddress.getLocalHost().getHostAddress();
			localName=InetAddress.getLocalHost().getHostName();
			broadSocket=new MulticastSocket(BROADCAST_INT_PORT); 
			broadAddress = InetAddress.getByName(BROADCAST_IP);
			sender = new DatagramSocket();
			this.list=list;
		} catch (Exception e) {
			System.out.println("*****lanSendService初始化失败*****"+e.toString());
		}
	}
	
	public void join() { 
		try{
			broadSocket.joinGroup(broadAddress);//加入到组播地址
			new Thread(new Runnable() {
				@Override
				public void run() {
					DatagramPacket inPacket;
					String[] message;
					while(true){
						try {
							inPacket=new DatagramPacket(new byte[1024], 1024);
							broadSocket.receive(inPacket);     //接收广播信息并将信息封装到inPacket中
							message=new String(inPacket.getData(),0,inPacket.getLength()).split("@");  //获取信息，并切割头部，判断是何种信息（find--上线，retn--回答，offl--下线）
							if(message[1].equals(localIp)){
								continue;  //忽略自身
							}
							if(message[0].equals("find")){   //如果是请求信息
								System.out.println("新上线主机："+" ip："+message[1]+" 主机："+message[2]);
								returnUserMsg(message[1]);
								if(!checkIpExists(message[1])){
									Map<String,Object> map=new HashMap<String,Object>();
							    	map.put("IP", message[1]);
							    	map.put("HOSTNAME", message[2]);
							    	list.add(map);
								}
							}
						    else if(message[0].equals("retn")){    //如果是返回信息
						    	System.out.println("找到新主机："+" ip："+message[1]+" 主机："+message[2]);
						    	if(!checkIpExists(message[1])){
									Map<String,Object> map=new HashMap<String,Object>();
							    	map.put("IP", message[1]);
							    	map.put("HOSTNAME", message[2]);
							    	list.add(map);
								}
						    }
						    else if(message[0].equals("offl")){    //如果是离线信息
						    	System.out.println("主机下线："+" ip："+message[1]+" 主机："+message[2]);
						    	returnUserMsg(message[1]);
								for(Map<String,Object> map : list){
									if(message[1].equals(map.get("IP").toString())){
										list.remove(map);
									}
								}
						    }
						} catch (Exception e) {
							System.out.println("线程出错 "+e);
						}
					}
				}
			}).start(); //新建一个线程，用于循环侦听端口信息
		}catch (Exception e) {
			System.out.println("*****加入组播失败*****");
		}
		  
	}
	public void returnUserMsg(String ip){
		byte[] b=new byte[1024];
		DatagramPacket packet;
		try {
			b=("retn@"+localIp+"@"+localName).getBytes();
			packet = new DatagramPacket(b,b.length,InetAddress.getByName(ip),BROADCAST_INT_PORT);
			sender.send(packet);
			System.out.print("发送信息成功！");
		} catch (Exception e) {
			System.out.println("*****发送返还信息失败*****");
		}
	}
	public void sendGetUserMsg() {
		byte[] b=new byte[1024];
        DatagramPacket packet;  //数据包，相当于集装箱，封装信息
        try{
        	b = ("find@"+localIp+"@"+localName).getBytes();  
        	packet = new DatagramPacket(b, b.length, broadAddress, BROADCAST_INT_PORT); //广播信息到指定端口
        	sender.send(packet);
        	System.out.println("*****已发送请求*****");       
        }catch (Exception e) {
        	System.out.println("*****查找出错*****");
        }
    }
	public void offLine(){
		byte[] b=new byte[1024];
		DatagramPacket packet;
		try {
			b=("offl@"+localIp+"@"+localName).getBytes();
			packet = new DatagramPacket(b,b.length,broadAddress,BROADCAST_INT_PORT);
			sender.send(packet);
			System.out.println("*****已离线*****");
		} catch (Exception e) {
			System.out.println("*****离线异常*****");
		}
	}
	public static void main(String[] args) {
		LanSendService lSend;
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		try {
		    lSend=new LanSendService(list);
		    lSend.join();     //加入组播，并创建线程侦听
		    lSend.sendGetUserMsg();    //广播信息，寻找上线主机交换信息
		    Thread.sleep(3000);  //程序睡眠3秒
		    lSend.offLine(); //广播下线通知
		} catch (Exception e) {
			System.out.println("*****获取本地用户信息出错*****");
		}
	}
	private boolean checkIpExists(String ip){
		boolean flag=false;
		if(ip==null||ip.length()==0){
			return flag;
		}
		for(Map<String,Object> map : list){
			if(ip.equals(map.get("IP"))){
				flag=true;
				break;
			}
		}
		return flag;
	}
}

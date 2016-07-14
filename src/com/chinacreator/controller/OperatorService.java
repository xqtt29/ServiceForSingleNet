package com.chinacreator.controller;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import com.chinacreator.common.Global;
import com.chinacreator.service.DataOprService;

public class OperatorService {
	//获取配置文件
	private static Map<String,String> conf=DataOprService.getInstance().getProp();
	
	private OperatorService(){
		
	}
	private static OperatorService operatorService;
	
	public static OperatorService getInstance(){
		return operatorService==null?new OperatorService():operatorService;
	}
	
	/**
	 * @Description
	 * 摆渡客户端操作指令
	 * 客户端socket信息流中第1个字节是操作指令
	 * @Author qiang.zhu
	 * @param socket
	 * @return
	 */
    public void func(Socket socket){
        DataInputStream dis = null;
        FileOutputStream fos = null;
        //重新获取配置文件，保持修改路径时实时生效
        conf=DataOprService.getInstance().getProp();
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                byte[] fileTypes=new byte[1];
                dis.read(fileTypes, 0, 1);
                String fileType=new String(fileTypes,Global.CHAR_FORMAT);
                DataOprService.getInstance().insertLog("请求客户端:"+socket.getRemoteSocketAddress().toString()+"  "+fileType,conf.get(Global.MAIN_PATH));
                //客户端发起传送文件
                if("1".equals(fileType)){
                	receiveFile(socket);
                //客户端发起查询文件
                }else if("2".equals(fileType)){
                	checkFile(socket);
                //客户端发起下载文件
                }else if("3".equals(fileType)){
                	sendFile(socket);
                //客户端发送粘贴板内容
                }else if("4".equals(fileType)){
                	receiveStr(socket);
                //客户端发送粘贴板内容
	            }else if("5".equals(fileType)){
	            	sendStr(socket);
	            //客户端发起转发请求
	            }else if("6".equals(fileType)){
	            	reSendFile(socket);
	            //查询在线服务端
	            }else if("7".equals(fileType)){
                	queryService(socket);
                //上传文件
	            }else if("8".equals(fileType)){
	            	receiveFileMutil(socket);
	            //上传文件
	            }else if("9".equals(fileType)){
	            	queryUnSendFile(socket);
	            //上传文件
	            }else if("0".equals(fileType)){
	            	deleteTempFile(socket);
                }
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("func:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    
    /**
	 * @Description
	 * 客户端上传文件处理类
	 * 输入socket信息流：前10个字节是上传文件的名称长度，后面接着文件信息流
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void receiveFile(Socket socket) {
        DataInputStream dis = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                //客户端socket信息流中第1个字节是上传文件名称的长度
                byte[] fileTypes=new byte[1];
                dis.read(fileTypes, 0, 1);
                String fileType=new String(fileTypes,Global.CHAR_FORMAT);
                //客户端socket信息流中第3到12个字节是上传文件名称的长度
                byte[] fileLens=new byte[10];
                dis.read(fileLens, 0, 10);
                String fileLen=new String(fileLens,Global.CHAR_FORMAT);
                //第12个字节后面接着就是文件名称，后面就是文件信息字节流
                byte[] fileNames=new byte[Integer.parseInt(fileLen)];
                dis.read(fileNames, 0, fileNames.length);
                String fileName=new String(fileNames,Global.CHAR_FORMAT);
                String filePath=conf.get(Global.RECEIVE_PATH)==null?(System.getProperty("user.dir")+File.separator+"收件箱"):conf.get(Global.RECEIVE_PATH);
                File dir=new File(filePath);
                if(!dir.exists())
                	dir.mkdirs();
                File f=new File(filePath+File.separator+fileName);
                if(fileType.equals("2")){
                	File ff=new File(filePath+File.separator+fileName.substring(0, fileName.lastIndexOf(File.separator)));
                	/*
                	String sfilePath=fileName.substring(0, fileName.indexOf(File.separator));
                	if(new File(filePath+File.separator+sfilePath).exists()){
                		DataOprService.getInstance().insertLog("该文件夹【"+sfilePath+"】本地已存在！",conf.get(Global.MAIN_PATH));
                    	return;
                	}
                	*/
                	if(!ff.exists())
                		ff.mkdirs();
                }else{
	                if(f.exists()){
	                	DataOprService.getInstance().insertLog("该文件【"+fileName+"】本地已存在！",conf.get(Global.MAIN_PATH));
	                	dos.write("1".getBytes(Global.CHAR_FORMAT), 0, "1".getBytes(Global.CHAR_FORMAT).length);
	                	dos.flush();
	                	return;
	                }
                }
                dos.write("0".getBytes(Global.CHAR_FORMAT), 0, "0".getBytes(Global.CHAR_FORMAT).length);
            	dos.flush();
                fos = new FileOutputStream(f);
                byte[] inputByte = new byte[102400];
                int length = 0;
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (dos != null)
                	dos.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("receive:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    /**
	 * @Description
	 * 客户端上传粘贴板内容
	 * 输入socket信息流：前10个字节是上传内容长度
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void receiveStr(Socket socket) {
        DataInputStream dis = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                //客户端socket信息流中第1到10个字节是上传文件名称的长度
                byte[] strLens=new byte[10];
                dis.read(strLens, 0, 10);
                //显示进度用
                //int strLen=Integer.parseInt(new String(strLens,Global.CHAR_FORMAT));
                //第11个字节后面接着就是粘贴板内容
                byte[] inputByte=new byte[102400];
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                int length=0;
                while ((length=dis.read(inputByte, 0, inputByte.length)) > 0) {
                	baos.write(inputByte, 0, length);
                }
                DataOprService.getInstance().setClipboardInfo("string", new String(baos.toByteArray(),Global.CHAR_FORMAT));
                
            } finally {
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("receive:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }/**
	 * @Description
	 * 客户端上传粘贴板内容
	 * 输入socket信息流：前10个字节是上传内容长度
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void sendStr(Socket socket) {
    	DataOutputStream dos = null;
    	Map<String,Object> map=DataOprService.getInstance().getClipboardInfo();
        try {
            try {
            	if(map.get("string")!=null){
    				String data=map.get("string").toString();
                    String len="";
					len = DataOprService.getInstance().initData(data.getBytes(Global.CHAR_FORMAT).length, 10);
	                dos = new DataOutputStream(socket.getOutputStream());
	                dos.write((len+data).getBytes(Global.CHAR_FORMAT), 0, (len+data).getBytes(Global.CHAR_FORMAT).length);
	                dos.flush();
            	}
            } finally {
                if (dos != null)
                    dos.close();
                if (socket != null)
                	socket.close();
            }
        }catch (Exception e) {
        	DataOprService.getInstance().insertLog("receive:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    /**
	 * @Description
	 * 客户端查询文件处理类
	 * 输出socket信息流：前10个字节是文件长度，后面接着文件绝对路径信息流，多个文件依此循环拼接
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void checkFile(Socket socket) {
        DataOutputStream dos = null;
        FileInputStream fis = null;
        try {
            try {
            	dos = new DataOutputStream(socket.getOutputStream());
                File filePath = new File(conf.get(Global.SEND_PATH)==null?(System.getProperty("user.dir")+File.separator+"发件箱"):conf.get(Global.SEND_PATH));
                File[] files=filePath.listFiles();
                StringBuffer sb=new StringBuffer();
                sb.append(DataOprService.getInstance().initData(files==null?0:files.length,4));
                if(files!=null)
                for(File file : files){
                	sb.append(file.isDirectory()?"2":"1");
                	String fileName=file.getAbsoluteFile().toString();
                    sb.append(DataOprService.getInstance().initData(fileName.getBytes(Global.CHAR_FORMAT).length,10));
                    sb.append(fileName);
                }
	            dos.write(sb.toString().getBytes(Global.CHAR_FORMAT), 0, sb.toString().getBytes(Global.CHAR_FORMAT).length);
	            dos.flush();
            } finally {
                if (fis != null)
                	fis.close();
                if (dos != null)
                	dos.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("checkFile:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }

    /**
	 * @Description
	 * 客户端下载文件处理类
	 * 输入socket信息流：前10个字节是下载文件的绝对路径字节长度，后面接着绝对路径信息流
	 * 输出socket信息流：前10个字节是文件长度，后面接着文件信息流
	 * @Author qiang.zhu
	 * @param socket
	 */ 
    public void sendFile(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                //客户端socket信息流中第1个字节是要下载的文件名称长度
                byte[] fileTypes=new byte[1];
                dis.read(fileTypes, 0, 1);
                String fileType=new String(fileTypes,Global.CHAR_FORMAT);
              //客户端socket信息流中第3到12个字节是要下载的文件名称长度
                byte[] fileLens=new byte[10];
                dis.read(fileLens, 0, 10);
                String fileLen=new String(fileLens,Global.CHAR_FORMAT);
                //第12个字节后面接着就是服务器上的文件绝对路径
                byte[] fileNames=new byte[Integer.parseInt(fileLen)];
                dis.read(fileNames, 0, fileNames.length);
                String filePath=new String(fileNames,Global.CHAR_FORMAT);
                if("2".equals(fileType)){
                	String[] temp=filePath.split("\\".equals(File.separator)?"\\\\":File.separator);
                	Map<String,Object> map = new HashMap<String,Object>();
                	DataOprService.getInstance().getDirectoryInfo(map,filePath);
                	String fileCount=map.get("fileCount")==null?"0":map.get("fileCount").toString();
                	String fileCountLen=DataOprService.getInstance().initData(Long.parseLong(fileCount), 4);
                	String fileSize=map.get("fileSize")==null?"0":map.get("fileSize").toString();
                	String fileSizeLen=DataOprService.getInstance().initData(Long.parseLong(fileSize), 10);
                	String head=fileCountLen+fileSizeLen;
	                dos.write(head.getBytes(Global.CHAR_FORMAT),0,head.getBytes(Global.CHAR_FORMAT).length);
	                dos.flush();
                	sendDirectory(dos,filePath,temp[temp.length-1]);
                }else{
	                dos.write("1".getBytes(Global.CHAR_FORMAT),0,1);
	                dos.flush();
	                File file=new File(filePath);
	                //返回给客户端的信息流中，前10个字节是文件大小
	                byte[] fileSize=DataOprService.getInstance().initData(file.length(), 10).getBytes(Global.CHAR_FORMAT);
	                dos.write(fileSize,0,fileSize.length);
	                dos.flush();
	                fis = new FileInputStream(file);
	            	byte[] sendBytes = new byte[102400];
	                int length = 0;
	                while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
	                    dos.write(sendBytes, 0, length);
	                    dos.flush();
	                }
                }
            } finally {
                if (fis != null)
                    fis.close();
                if (dis != null)
                    dis.close();
                if (dos != null)
                    dos.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	DataOprService.getInstance().insertLog("sendFile:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    
    /**
	 * @Description
	 * 遍历文件夹，发送所有文件
	 * @Author qiang.zhu
	 * @param dos
	 * @param filePath
	 * @param fileName
	 */
    private void sendDirectory(DataOutputStream dos,String filePath,String fileName) throws Exception{
    	File file=new File(filePath);
    	for(File f : file.listFiles()){
	        if(f.isDirectory()){
	        	sendDirectory(dos,f.getAbsolutePath(),fileName+File.separator+f.getName());
	        }
	        if(f.isDirectory()){
	        	continue;
	        }
	        //返回给客户端的信息流中，前10个字节是文件名称
	        String fileNameLen=DataOprService.getInstance().initData((fileName+File.separator+f.getName()).getBytes(Global.CHAR_FORMAT).length, 10);
	        String fileLen=DataOprService.getInstance().initData(f.length(), 10);
	        String head=fileNameLen+fileName+File.separator+f.getName()+fileLen;
	        dos.write(head.getBytes(Global.CHAR_FORMAT),0,head.getBytes(Global.CHAR_FORMAT).length);
	        dos.flush();
	        FileInputStream fis = new FileInputStream(f);
	        byte[] sendBytes = new byte[102400];
	        int length = 0;
	        while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
	            dos.write(sendBytes, 0, length);
	            dos.flush();
	        }
	        if(fis!=null){
	        	fis.close();
	        }
	    }
    }
    
    public void reSendFile(Socket resSocket){
    	int length = 0;
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream resDis = null;
        FileInputStream fis = null;
        try {
            try {
            	resDis = new DataInputStream(resSocket.getInputStream());
            	int ipLen=resDis.readInt();
            	byte[] ips= new byte[ipLen];
            	resDis.read(ips);
            	String ip=new String(ips,Global.CHAR_FORMAT);
            	int port=resDis.readInt();
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip,port),
                               3 * 1000);
                dos = new DataOutputStream(socket.getOutputStream());
                byte[] sendBytes = new byte[102400];
                while ((length = resDis.read(sendBytes, 0, sendBytes.length)) > 0) {
                    dos.write(sendBytes, 0, length);
                    dos.flush();
                }
            } finally {
                if (resDis != null)
                	resDis.close();
                if (dos != null)
                    dos.close();
                if (fis != null)
                    fis.close();
                if (socket != null)
                    socket.close();
            }
        }catch (Exception e) {
        	e.printStackTrace();
        }
    }
    /**
	 * @Description
	 * 客户端查询文件处理类
	 * 输出socket信息流：前10个字节是文件长度，后面接着文件绝对路径信息流，多个文件依此循环拼接
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void queryService(Socket socket) {
        DataOutputStream dos = null;
        try {
            try {
            	dos = new DataOutputStream(socket.getOutputStream());
            	dos.writeInt(Global.list.size());
    		    for(Map<String,Object> map : Global.list){
    		    	String ip=map.get("IP")==null?"":map.get("IP").toString();
    		    	String hostName=map.get("HOSTNAME")==null?"":map.get("HOSTNAME").toString();
		            dos.writeInt(ip.getBytes(Global.CHAR_FORMAT).length);
		            dos.write(ip.getBytes(Global.CHAR_FORMAT));
		            dos.writeInt(hostName.getBytes(Global.CHAR_FORMAT).length);
		            dos.write(hostName.getBytes(Global.CHAR_FORMAT));
    		    }
	            dos.flush();
            } finally {
                if (dos != null)
                	dos.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("queryService:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    /**
	 * @Description
	 * 客户端上传文件处理类
	 * 输入socket信息流：前10个字节是上传文件的名称长度，后面接着文件信息流
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void receiveFileMutil(Socket socket) {
        DataInputStream dis = null;
		RandomAccessFile raf = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                long finalStartNum=dis.readLong();
                long startNum=dis.readLong();
            	int fileNameLenth=dis.readInt();
            	byte[] fileNames=new byte[fileNameLenth];
            	dis.read(fileNames);
            	String fileName=new String(fileNames,Global.CHAR_FORMAT);
            	Map<String,Object> map=Global.sendingFileInfo.get(fileName);
            	if(map==null){
            		map=new Hashtable<String,Object>();
            	}
            	String filePath=conf.get(Global.RECEIVE_PATH)==null?(System.getProperty("user.dir")+File.separator+"收件箱"):conf.get(Global.RECEIVE_PATH);
                File dir=new File(filePath);
                if(!dir.exists())
                	dir.mkdirs();
                File file=new File(filePath+File.separator+fileName);
            	raf=new RandomAccessFile(file,"rwd");
            	raf.seek(startNum);
            	int length=0;
            	byte[] bytes = new byte[102400];
            	long sendedTotal=0;
            	while((length=dis.read(bytes,0,bytes.length))>0){
            		raf.write(bytes, 0, length);
            		sendedTotal+=length;
            		map.put(String.valueOf(finalStartNum), sendedTotal);
            	}
            } finally {
                if (raf != null)
                	raf.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("receiveFileMutil:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    /**
	 * @Description
	 * 客户端上传文件处理类
	 * 输入socket信息流：前10个字节是上传文件的名称长度，后面接着文件信息流
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void queryUnSendFile(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                long startNum=dis.readLong();
            	int fileNameLenth=dis.readInt();
            	byte[] fileNames=new byte[fileNameLenth];
            	dis.read(fileNames);
            	String fileName=new String(fileNames,Global.CHAR_FORMAT);
                if(Global.sendingFileInfo.get(fileName)!=null&&Global.sendingFileInfo.get(fileName).get(String.valueOf(startNum))!=null){
                	dos.writeInt(200);
                	dos.writeLong(Long.parseLong(Global.sendingFileInfo.get(fileName).get(String.valueOf(startNum)).toString()));
                }else{
                	dos.writeInt(-1);
                }
                dos.flush();
            } finally {
                if (dos != null)
                	dos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("queryUnSendFile:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
    /**
	 * @Description
	 * 客户端上传文件处理类
	 * 输入socket信息流：前10个字节是上传文件的名称长度，后面接着文件信息流
	 * @Author qiang.zhu
	 * @param socket
	 */
    public void deleteTempFile(Socket socket) {
        DataInputStream dis = null;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
            	int fileNameLenth=dis.readInt();
            	byte[] fileNames=new byte[fileNameLenth];
            	dis.read(fileNames);
            	String fileName=new String(fileNames,Global.CHAR_FORMAT);
            	Global.sendingFileInfo.remove(fileName);
            } finally {
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("deleteTempFile:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }
}

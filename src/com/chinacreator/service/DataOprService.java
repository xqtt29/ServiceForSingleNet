package com.chinacreator.service;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Description
 * 公共方法操作类
 * 
 * @Author qiang.zhu
 * @Datetime 2016年5月10日 上午9:31:29
 * @Version
 * @Copyright (c) 2013 湖南科创信息技术股份有限公司
 */
public class DataOprService {
	
	private DataOprService(){
		
	}
	
	private static DataOprService dataOprService;
	
	public static DataOprService getInstance(){
		return dataOprService==null?new DataOprService():dataOprService;
	}
	
	/**
	 * @Description
	 * 获取定长信息(不足以0补齐)
	 * @Author qiang.zhu
	 * @param data
	 * @param size
	 * @return String
	 */
	public String initData(long data,int size){
		StringBuffer sb=new StringBuffer();
		int len=size-String.valueOf(data).length();
		for(int i=0;i<len;i++){
			sb.append("0");
		}
		sb.append(data);
		return sb.toString();
	}
	
	/**
	 * @Description
	 * 获取配置文件
	 * @Author qiang.zhu
	 * @return Map
	 */
	public Map<String, String> getProp(){
    	Map<String,String> m = new HashMap<String,String>();
    	Properties p = new Properties();
    	try {
    		InputStreamReader isr = new InputStreamReader(new FileInputStream(System.getProperty("user.dir")+File.separator + "conf.properties"), "gbk");
    		p.load(isr);
    		for (Iterator localIterator = p.keySet().iterator(); localIterator.hasNext(); ) { 
    			Object temp = localIterator.next();
    			m.put((String)temp, p.getProperty((String)temp).trim());
    		}
    		isr.close();
    	} catch (Exception e) {
    	}
    	return m;
    }
	
	/**
	 * @Description
	 * 记录日志
	 * @Author qiang.zhu
	 * @param content
	 * @param path
	 * @return
	 */
	public void insertLog(String content,String path){
		FileOutputStream fos=null;
		if(path==null||path.length()==0){
			path=System.getProperty("user.dir");
		}
		try{
			try{
				fos=new FileOutputStream(new File(path+File.separator+"log.txt"),true);
				fos.write(("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"] "+content).getBytes());
				fos.write("\r\n".getBytes());
				fos.flush();
	        }finally{
	        	fos.close();
	        }
		}catch(Exception e){
			
		}
	}
	/**
	 * @Description
	 * 获取文件(重名时自动加上后缀(*))功能暂未完成
	 * @Author qiang.zhu
	 * @param filePath
	 * @return File
	 */
	public File getFile(String filePath){
		File f=new File(filePath);
		if(f.exists()){
			int len=filePath.lastIndexOf(".");
			f=getFile(filePath.substring(0, len)+"(1)"+filePath.substring(len));
		}
		return f;
	}
	/**
	 * @Description
	 * 获取文件夹信息（文件夹个数和文件总大小）
	 * @Author qiang.zhu
	 * @param filePath
	 * @return File
	 */
	public void getDirectoryInfo(Map<String,Object> map,String filePath){
    	int fileCount=0;
    	long fileSize=0;
    	File file = new File(filePath);
    	for(File f : file.listFiles()){
    		if(f.isDirectory()){
    			getDirectoryInfo(map,f.getAbsolutePath());
    		}else{
    			fileCount+=1;
    			fileSize+=f.length();
    		}
    	}
    	map.put("fileCount", Integer.parseInt(map.get("fileCount")==null?"0":map.get("fileCount").toString())+fileCount);
    	map.put("fileSize", Long.parseLong(map.get("fileSize")==null?"0":map.get("fileSize").toString())+fileSize);
    }
	/**
	 * @Description
	 * 设置粘贴板内容
	 * @Author qiang.zhu
	 * @param model
	 * @return List
	 */
	public void setClipboardInfo(String type,final Object obj){
		if("file".equals(type)){
			Transferable trans = new Transferable() {  
	            public DataFlavor[] getTransferDataFlavors() {  
	                return new DataFlavor[] { DataFlavor.javaFileListFlavor };  
	            }  
	  
	            public boolean isDataFlavorSupported(DataFlavor flavor) {  
	                return DataFlavor.javaFileListFlavor.equals(flavor);  
	            }  
	  
	            public Object getTransferData(DataFlavor flavor)  
	                    throws UnsupportedFlavorException, IOException {  
	                if (isDataFlavorSupported(flavor))  
	                    return obj;  
	                throw new UnsupportedFlavorException(flavor);  
	            }  
	  
	        };
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(trans, null);
		}else if("string".equals(type)){
			Transferable trans = new Transferable() {  
	            public DataFlavor[] getTransferDataFlavors() {  
	                return new DataFlavor[] { DataFlavor.stringFlavor };  
	            }  
	  
	            public boolean isDataFlavorSupported(DataFlavor flavor) {  
	                return DataFlavor.stringFlavor.equals(flavor);  
	            }  
	  
	            public Object getTransferData(DataFlavor flavor)  
	                    throws UnsupportedFlavorException, IOException {  
	                if (isDataFlavorSupported(flavor))  
	                    return obj;  
	                throw new UnsupportedFlavorException(flavor);  
	            }  
	  
	        };
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(trans, null);
		}
	}
	/**
	 * @Description
	 * 获取粘贴板内容
	 * @Author qiang.zhu
	 * @param model
	 * @return List
	 */
	public Map<String,Object> getClipboardInfo(){
		Map<String,Object> map = new HashMap<String,Object>();
		try{
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t=cb.getContents(null);
			if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
				List<File> obj=(List)t.getTransferData(DataFlavor.javaFileListFlavor);
				map.put("file", obj);
			}else if(t.isDataFlavorSupported(DataFlavor.stringFlavor)){
				map.put("string", t.getTransferData(DataFlavor.stringFlavor));
			}
		}catch(Exception e){
			
		}
		return map;
	}
}

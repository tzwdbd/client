/**
 * 淘粉吧版权所有 2013
 */
package com.oversea.task.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author rambing
 *
 * 创建时间： 2014年4月11日
 */
public class MD5Util {
	
	private static final int BUFFER_SIZE = 8 * 1024;

	private static final int HEX = 16;
	
	public static String calMD5(File file) throws IOException{
		if(file == null || !file.exists()){
			return null;
		}
		
		FileInputStream fileInputStream = null;
		try{
			fileInputStream = new FileInputStream(file);
			return calMD5(fileInputStream);
		}finally{
			fileInputStream.close();
		}
	}

	/**
	 * 计算一个文件流的MD5值
	 * @param fileInputStream
	 * @return
	 * @throws IOException
	 */
	public static String calMD5(FileInputStream fileInputStream) throws IOException {
		if(fileInputStream == null){
			return null;
		}
		// 缓冲区大小（这个可以抽出一个参数）
		DigestInputStream digestInputStream = null;
		try {
		       // 拿到一个MD5转换器
		       MessageDigest messageDigest =MessageDigest.getInstance("MD5");
		       digestInputStream = new DigestInputStream(fileInputStream,messageDigest);
		       // read的过程中进行MD5处理，直到读完文件
		       byte[] buffer =new byte[BUFFER_SIZE];
		       while (digestInputStream.read(buffer) > 0);
		       // 获取最终的MessageDigest
		       messageDigest= digestInputStream.getMessageDigest();
		       // 拿到结果，也是字节数组，包含16个元素
		       BigInteger bi = new BigInteger(1, messageDigest.digest());
		       return bi.toString(HEX);
		    } catch (NoSuchAlgorithmException e) {
		       return null;
		    } finally {
		          digestInputStream.close();
		    }
	}
	
	/**
	 * 计算一个字节数组的MD5值
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static String calMD5(byte[] data){
		if(data == null || data.length == 0){
			return null;
		}
		
		try {
		       // 拿到一个MD5转换器
		       MessageDigest messageDigest =MessageDigest.getInstance("MD5");
		       messageDigest.update(data);
		       
		       // 拿到结果，也是字节数组，包含16个元素
		       BigInteger bi = new BigInteger(1, messageDigest.digest());
		       return bi.toString(HEX);
		    } catch (NoSuchAlgorithmException e) {
		       return null;
		    }
	}
	

}

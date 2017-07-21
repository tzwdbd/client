package com.oversea.task.utils;

public class NumberUtil {
	
	public static Integer str2Integer(String num){
		if(num.contains("%")){
			//去掉百分号
			num = num.replace("%", "");
		}
		if(num.contains(".")){
			//去掉小数点
			num = num.replace(".", "");
		}
		num = num.trim();
		Integer result = Integer.parseInt(num);
		
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println(str2Integer("94.564%"));
		System.out.println(str2Integer("94.564"));
		System.out.println(str2Integer("4.56"));
	}
}

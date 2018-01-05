package com.oversea.task.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressUtils {
	
	private static final Pattern PT_EXPRESS_NO = Pattern.compile("[A-Z0-9]{12,}");
	private static final Pattern RG_EXPRESS_NO = Pattern.compile("[A-Za-z0-9]{8,}");
	private static final Pattern RG_MALL_NO = Pattern.compile("[0-9]{8,}");
	private static final Pattern PT_CAP_LETTERS = Pattern.compile("^[A-Z]+$");
	
	public static String extractExperssNo(String content) {
		StringBuilder sb = new StringBuilder();
		
		Matcher matcher = PT_EXPRESS_NO.matcher(content);
		String temp;
		while (matcher.find()) {
			temp = matcher.group();
			if (PT_CAP_LETTERS.matcher(temp).matches()) {
				continue;
			}
			sb.append(temp);
			sb.append(',');
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}
	
	public static String regularExperssNo(String content) {
		StringBuilder sb = new StringBuilder();
		
		Matcher matcher = RG_EXPRESS_NO.matcher(content);
		String temp;
		while (matcher.find()) {
			temp = matcher.group();
			sb.append(temp);
			sb.append(',');
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}
	public static String regularMallOrderNo(String content) {
		StringBuilder sb = new StringBuilder();
		
		Matcher matcher = RG_MALL_NO.matcher(content);
		String temp;
		while (matcher.find()) {
			temp = matcher.group();
			sb.append(temp);
			sb.append(',');
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}
	public static boolean isContainNumber(String company) {

        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(company);
        if (m.find()) {
            return true;
        }
        return false;
    }
	
	public static void main(String[] args) {
		//System.out.println(isContainNumber("ABCDEFGHIJLMNOPRSTVWZ"));
		System.out.println(regularMallOrderNo("Your order has been submitted.Order Number: 20517832 "));
//		System.out.println(extractExperssNo("1Z1A48T90314108235"));
//		System.out.println(extractExperssNo("ABCDEFGHIJLMNOPRSTVWZ"));
//		System.out.println(extractExperssNo("							1Z6R646EYW73938531"));
//		System.out.println(extractExperssNo("92612904850788551014809226"));
//		System.out.println(extractExperssNo("Tracking: 1ZE2Y229P207532637"));
//		System.out.println(extractExperssNo("Shipping Carrier:  UPSTracking: 1ZE2Y229P207532637"));
//		System.out.println(extractExperssNo("Shipping Carrier:  UPSTracking: 1Z16R17V0202931812 Shipping Carrier:  UPSTracking: 1ZW264F40290304334"));
//		System.out.println(extractExperssNo("Status:CLOSED - ORDER FULLY SHIPPEDMethod: USPS 1ST PRICarrier: USPSShip Date: 02/26/2017Tracking No: 9205590136271846370800"));
//		System.out.println(extractExperssNo("EDPSASHOA HHHTHHHTPORTLAND, OR, 97230"));
//		System.out.println(extractExperssNo("Shipped on:03/27/17Tracking #:92748999984326000000278435 Track your package now"));
//		System.out.println(extractExperssNo("ORDER PLACED:Jan 17, 2017ORDER NUMBER:0005101529800ORDER STATUS:Fulfilled"));
//		System.out.println(extractExperssNo("RcmdokcB HHHT \n12817 NE Airport Way STE 23883 HHHTPortland, OR\n97230 USTracking #:1Z2R3R62YW07664465Track your package now"));
//		System.out.println(extractExperssNo("				CORPORATE SOCIAL RESPONSIBILITY"));
	}

}


<%@page import="com.enuri.bean.mobile.EmsTower_Proc"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>\
<%@ include file="/jca_include/Jca_Base_Inc_2010.jsp" %>\
<%@ page import="com.enuri.util.common.ChkNull" %>\
<%@ page import="org.json.JSONArray"%>\
<%@ page import="com.enuri.bean.logdata.Access_log_ip" %>\

<%

Access_log_ip access_log_ip = new Access_log_ip();
JSONArray jsonArray = new JSONArray();


String paramType = request.getParameter("type");

int type = 1;
if(!StringUtils.isEmpty(paramType)) {
	type = Integer.parseInt(paramType);
}

EtcData etcData = new EtcData();

switch(type) {
	case 1: 
	default:
		//2시간 동안의 데이터 타입 확인
		out.println(etcData.getRewardCartTable());
		break;
	case 2: //12시간
		out.println(etcData.getRewardCartTable01());
		break;
	case 3: //최근 1일
		out.println(etcData.getRewardCartTable02());
		break;
	case 4: //최근 7일
		out.println(etcData.getRewardCartTable03());
		break;
	}
	
%>	
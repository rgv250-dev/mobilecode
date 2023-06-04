
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
out.println(etcData.getRewardCartTable05());

	
%>	
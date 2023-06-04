대충 사용법 및 기타 


DB 쿼리 결과를 JSONArray 받아
html 태그로 표현 방법
```
public JSONArray  getRewardCartTable01 (){
			
			StringBuffer query = new StringBuffer();
			
			 	query.append("select sp.shop_name ");
				query.append(", isnull(sp.shop_code, b.shop_code) shop_code ");
				query.append(", cart_cnt ");
				query.append(", log_cnt ");
				query.append(", log_err_cnt ");
				query.append(", err_ratio = isnull(cast(log_err_cnt*100.0/(nullif(log_cnt,0)) as decimal(4,1)) ,0) ");
				query.append("from ");
				query.append("( ");
				query.append("select ");
				query.append("shop_code ");
				query.append(", sum(log_cnt) log_cnt ");
				query.append(", sum(cart_cnt) cart_cnt ");
				query.append(", sum(log_err_cnt) log_err_cnt ");
				query.append("from ");
				query.append("( ");
				query.append("select shop_code, count(*) log_cnt, 0 cart_cnt ");
				query.append(", sum(case when error_msg not in( 'SEND_REWARD_RESULT_400', 'SEND_REWARD_SUCCESS') then 1 else 0 end) log_err_cnt ");
				query.append("from mobile_order_log with (readuncommitted) ");
				query.append("where order_date >= CONVERT(varchar(30),Dateadd(hh, -12, Getdate()),120) ");
				query.append("and order_date < CONVERT(varchar(30),Dateadd(hh, 0, Getdate()),120) ");
				query.append("group by shop_code ");
				query.append("union all ");
				query.append("select shop_code, 0 log_cnt, count(*) cart_cnt, 0 log_err_cnt ");
				query.append("from tbl_reward_cart with (readuncommitted)  ");
				query.append("where cart_order_date>= CONVERT(varchar(30),Dateadd(hh, -12, Getdate()),120) ");
				query.append("and cart_order_date <  CONVERT(varchar(30),Dateadd(hh, 0, Getdate()),120) ");
				query.append("group by shop_code ");
				query.append(") a ");
				query.append("group by shop_code ");
				query.append(") b ");
				query.append("full outer join ");
				query.append("(select shop_code, shop_name from tbl_reward_shoplist where is_service=1) sp ");
				query.append("on sp.shop_code = b.shop_code ");
				query.append("order by sp.shop_code ");
				query.append("option (maxdop 1) ");
			
			
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			JSONArray json = new JSONArray();
			
			try{
				con = dbConn.createDBConnection("test");
				pstmt = con.prepareStatement(query.toString(), ResultSet.CONCUR_READ_ONLY);
				rs = pstmt.executeQuery();
				

				while (rs.next()) {
					JSONObject obj = new JSONObject();
					
					
					int logCntData = 0;
					int logErrCountData = 0;
					
					if ( rs.getString("shop_name") == null || rs.getString("shop_name").equals("null") ||  rs.getString("shop_name").trim().length() == 0) {
						obj.put("shop_name", "nil");
					}else {
						obj.put("shop_name", rs.getString("shop_name"));
					}
								
					if (rs.getString("log_cnt") != null) {
						logCntData = Integer.parseInt(rs.getString("log_cnt"));
					}
					
					if (rs.getString("log_err_cnt") != null) {
						logErrCountData = Integer.parseInt(rs.getString("log_err_cnt"));
					}
					
					obj.put("reward_log_cnt", logCntData );
					obj.put("reward_errorlog_cnt", logErrCountData);
					
					if (logErrCountData == 0) {
						obj.put("reward_errorlog_rate", 0);
					}else if (logCntData == logErrCountData) {
						obj.put("reward_errorlog_rate", "100%");
					}else {
						int errorlogCountRateData = logCntData-logErrCountData;
						double errorlogRateData = (double)errorlogCountRateData/logCntData;			
						obj.put("reward_errorlog_rate", String.format("%.1f%%", errorlogRateData));
					}
					
					
					
					
					json.put(obj);
				}
				
				return json;
			}catch(Exception e){
				return json.put(e.getMessage());
			}
			
		}
		```

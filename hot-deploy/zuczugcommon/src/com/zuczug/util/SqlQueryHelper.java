package com.zuczug.util;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

/**
 * @author Sven Wong
 * @version 创建时间：2015年8月29日 下午11:04:31
 * @description：SQL的高级查询
 * SQL写在XML中，有一个唯一的KEY，在查询的时候传入这个唯一的KEY，来查询
 */

public class SqlQueryHelper {
	
	public static Map<String, Object> performFindSql(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String,Object> result = ServiceUtil.returnSuccess();
		
		/*
		<entitymodel>
			<sqlEntity>
				<name>findFabrics</name>
				<controller-uri>updateFabricProductAndFeature</controller-uri>
				<description>面料管理模块首页查询</description>
				<keys>productId,internalName,colorId,colorDescription</keys>
				<sql>
					<![CDATA[
						SELECT
							PA.PRODUCT_ID productId,
							PA.INTERNAL_NAME internalName,
							PFA.PRODUCT_FEATURE_ID colorId,
							PFA.DESCRIPTION colorDescription
						FROM 
							PRODUCT PRO,PRODUCT_FEATURE_APPL PFA 
						WHERE PFA.PRODUCT_ID=PRO.PRODUCT
						!!!参数怎么传是一个问题
					]]>
				</sql>
			</sqlEntity>
		</entitymodel>
		*/
		
		//参数1：对应SQL的名称（通过SQL的名称，找到SQL的主体和对应的select的key值）
		//参数2：按顺序替换SQL中对应变量的List
		//viewIndex
		//viewSize
		
		
		return result;
	}
	
	/**
	 * 执行一段SQL查询
	 * @param sql 你需要查询的sql
	 * @param key sql的select中key值，也是返回的每一组数据的Map的key值
	 * @param delegator
	 * @return List< Map< String,Object > >
	 */
	public static List<Map<String , Object>> createSQLQueryList(String sql ,String[] key , Delegator delegator) {
		List<Map<String , Object>> list = new ArrayList<Map<String,Object>>() ;
		Map<String , Object> map = null ;
		GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
		SQLProcessor sqlproc = new SQLProcessor(helperInfo);
		ResultSet rs = null;
		try {
			sqlproc.prepareStatement(sql.toString());
			rs = sqlproc.executeQuery();
			while(rs.next()){
				map = new HashMap<String , Object>();
				for(String item : key){
					map.put(item, rs.getObject(item)) ;
				}
				list.add(map);
			}
			
		} catch (GenericEntityException e) {
			Debug.log(e.getMessage());
		} catch (Exception e) {
			Debug.log(e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				sqlproc.close();
			} catch (GenericDataSourceException e) {
				Debug.log(e.getMessage());
			} catch (SQLException e) {
				Debug.log(e.getMessage());
			}
		}
		return list;
	}
}

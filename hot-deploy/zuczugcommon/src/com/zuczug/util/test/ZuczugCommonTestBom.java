package com.zuczug.util.test;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.testtools.OFBizTestCase;

import com.zuczug.product.ZuczugProductServices;

public class ZuczugCommonTestBom extends OFBizTestCase {

	public static final String module = ZuczugCommonTest.class.getName();

    protected GenericValue userLogin = null;
	
	public ZuczugCommonTestBom(String name) {
		super(name);
	}
	
	@Override
    protected void setUp() throws Exception {
        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
	/**
	 * 测试替换BOM
	 * @throws Exception
	 */
//	public void testCombinedRepeatVirtualProduct()throws Exception {
//    	Map<String, Object> ctx = FastMap.newInstance();
//    	//把KBZTEST1的BOM替换成KBZTEST2
//        List<?> warningList = FastList.newInstance();
//    	ctx.put("virtualProduct", "KBZTEST2");
//    	ctx.put("destroyVirtualProduct", "KBZTEST1");
//        ctx.put("userLogin", userLogin);
//        //查看KBZTEST2原有的变型
//        List<GenericValue> productAssocListBM1 = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","KBZTEST2","productAssocTypeId","PRODUCT_VARIANT"));
//        if (UtilValidate.isNotEmpty(productAssocListBM1)) {
//			for(GenericValue productAssoc1:productAssocListBM1){
//				Debug.logInfo("KBZTEST2//YBX+++++++++++"+productAssoc1.getString("productIdTo"), module);
//			}
//		}
//        
//        Map<String, Object> respMap0 = dispatcher.runSync("combinedRepeatVirtualProductBM", ctx);
//        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
//        assertNull(warningList);
//        
//        //跑完服务查看KBZTEST1是否还有BOM
//        List<GenericValue> KBZTEST1BOM = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productIdTo","KBZTEST1","productAssocTypeId","ENGINEER_COMPONENT"));
//        assertEquals(0, KBZTEST1BOM.size());
//        
//        //查看KBZTEST1是否还有生产BOM
//        List<GenericValue> KBZTEST1ProductionBOM = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productIdTo","KBZTEST1","productAssocTypeId","MANUF_COMPONENT"));
//        assertEquals(0, KBZTEST1ProductionBOM.size());
//        
//        //查看KBZTEST1是否还有变型
//        List<GenericValue> KBZTEST1BIANX = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","KBZTEST1","productAssocTypeId","PRODUCT_VARIANT"));
//        Debug.log("+++++++++++++++2:"+KBZTEST1BIANX);
//        assertEquals(0, KBZTEST1BIANX.size());
//        if (UtilValidate.isNotEmpty(KBZTEST1BIANX)) {
//			for(GenericValue KBZTEST1BIAN:KBZTEST1BIANX){
//				Debug.logInfo("KBZTEST1/XUNI+++++++++++"+KBZTEST1BIAN.getString("productIdTo"), module);
//			}
//		}else {
//			Debug.logInfo("KBZTEST1/XUNI+++++++++++++++++++++++successful:"+KBZTEST1BIANX, module);
//		}
//        
//        //查看KBZTEST2替换后的Bom是否正确
//        List<GenericValue> KBZTEST2BOM = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productIdTo","KBZTEST2","productAssocTypeId","ENGINEER_COMPONENT"));
//        assertEquals(7, KBZTEST2BOM.size());
//        if (UtilValidate.isNotEmpty(KBZTEST2BOM)) {
//			for(GenericValue KBZTEST2B:KBZTEST2BOM){
//				Debug.logInfo("KBZTEST2//BOM+++++++++++"+KBZTEST2B.getString("productId"), module);
//			}
//		}
//        
//        List<GenericValue> BOMTEST5=delegator.findByAnd("ProductAssoc", UtilMisc.toMap(
//        		"productIdTo","KBZTEST2","productId","BOMTEST5"
//        		,"productAssocTypeId","ENGINEER_COMPONENT"));
//        assertEquals("BOMTEST5", BOMTEST5.get(0).getString("productId"));
//        Debug.log("KBZTEST2//BOMst5+++++++++++"+BOMTEST5.get(0).getString("productId"));
//        
//        List<GenericValue> BOMTEST6=delegator.findByAnd("ProductAssoc", UtilMisc.toMap(
//        		"productIdTo","KBZTEST2","productId","BOMTEST6"
//        		,"productAssocTypeId","ENGINEER_COMPONENT"));
//        assertEquals("BOMTEST6", BOMTEST6.get(0).getString("productId"));
//        Debug.log("KBZTEST2//BOMst6+++++++++++"+BOMTEST6.get(0).getString("productId"));
//        
//        List<GenericValue> BOMTEST7=delegator.findByAnd("ProductAssoc", UtilMisc.toMap(
//        		"productIdTo","KBZTEST2","productId","BOMTEST7"
//        		,"productAssocTypeId","ENGINEER_COMPONENT"));
////        assertNotNull(BOMTEST7);
//        assertEquals("BOMTEST7", BOMTEST7.get(0).getString("productId"));
//        Debug.log("KBZTEST2//BOMst7+++++++++++"+BOMTEST7.get(0).getString("productId"));
//        
//        //查看KBZTEST2替换后变型商品是否正确
//        List<GenericValue> KBZTEST2BIANX = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","KBZTEST2","productAssocTypeId","PRODUCT_VARIANT"));
//        assertEquals(4, KBZTEST2BIANX.size());
//        if (UtilValidate.isNotEmpty(KBZTEST2BIANX)) {
//			for(GenericValue KBZTEST2BX:KBZTEST2BIANX){
//				Debug.logInfo("KBZTEST2//BIANXIN+++++++++++"+KBZTEST2BX.getString("productIdTo"), module);
//			}
//		}
//        
//        List<GenericValue> KBZTEST101 = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","KBZTEST2","productIdTo","KBZTEST1-01","productAssocTypeId","PRODUCT_VARIANT"));
//        assertEquals("KBZTEST1-01", KBZTEST101.get(0).getString("productIdTo"));
//        
//        List<GenericValue> KBZTEST102 = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","KBZTEST2","productIdTo","KBZTEST1-02","productAssocTypeId","PRODUCT_VARIANT"));
//        assertEquals("KBZTEST1-02", KBZTEST102.get(0).getString("productIdTo"));
//        Debug.log("KBZTEST2//BIANxin2+++++++++++"+KBZTEST102.get(0).getString("productIdTo"));
//        
//        //查看KBZTEST2替换后的生产Bom是否正确
//        List<GenericValue> KBZTEST2ProductionBOM = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productIdTo","KBZTEST2","productAssocTypeId","MANUF_COMPONENT"));
//        assertEquals(7, KBZTEST2ProductionBOM.size());
//        if (UtilValidate.isNotEmpty(KBZTEST2ProductionBOM)) {
//			for(GenericValue KBZTEST2BProduction:KBZTEST2ProductionBOM){
//				Debug.logInfo("KBZTEST2ProductionBOM//BOM+++++++++++"+KBZTEST2BProduction.getString("productId"), module);
//			}
//		}
//        
//        List<GenericValue> BOMTEST55 = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","BOMTEST55","productIdTo","KBZTEST2","productAssocTypeId","MANUF_COMPONENT"));
//        assertEquals("BOMTEST55", BOMTEST55.get(0).getString("productId"));
//        
//        List<GenericValue> BOMTEST77 = delegator.findByAnd("ProductAssoc", 
//        		UtilMisc.toMap("productId","BOMTEST77","productIdTo","KBZTEST2","productAssocTypeId","MANUF_COMPONENT"));
//        assertEquals("BOMTEST77", BOMTEST77.get(0).getString("productId"));
//        
//    }

	
	/**
	 * 测试没有素然物料编号的商品通过商品ID重新赋值
	 */
	public void testFixZuczugCode()throws Exception {
		
        List<?> warningList = FastList.newInstance();
        Map<String, Object> ctx = FastMap.newInstance();
        ctx.put("userLogin", userLogin);
        Map<String, Object> respMap0 = dispatcher.runSync("FixZuczugCode", ctx);
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);
        //查询Z161TEST1的素然编号是否有变化，没变化就是好的
        GenericValue Z161TEST1=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TEST1"),true);
        assertEquals("Z161TEST1", Z161TEST1.getString("idValue"));
        Debug.log("Z161TEST1+++++++++++"+Z161TEST1.getString("productId"));
        
        //原本Z161TEST2没有素然物料编号，跑完服务查看是否有素然物料编号
        GenericValue Z161TEST2=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TEST2"),true);
        assertEquals("Z161TEST2", Z161TEST2.getString("idValue"));
        Debug.log("Z161TEST2+++++++++++"+Z161TEST2.getString("productId"));
        
        //原本Z161TEST4没有素然物料编号，跑完服务查看是否有素然物料编号
        GenericValue Z161TEST4=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TEST4"),true);
        assertEquals("Z161TEST4", Z161TEST4.getString("idValue"));
        Debug.log("Z161TEST4+++++++++++"+Z161TEST4.getString("productId"));
        
        
        //Z161TESTBIANXIN1没有素然物料编号，跑完服务查看是否有素然物料编号
        GenericValue Z161TESTBIANXIN1=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TESTBIANXIN1"),true);
        assertEquals("Z161TESTBIANXIN1", Z161TESTBIANXIN1.getString("idValue"));
        Debug.log("Z161TESTBIANXIN1+++++++++++"+Z161TESTBIANXIN1.getString("productId"));
        
        
        //Z161TESTBIANXIN2没有素然物料编号，跑完服务查看是否有素然物料编号
        GenericValue Z161TESTBIANXIN2=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TESTBIANXIN2"),true);
        assertEquals("Z161TESTBIANXIN2", Z161TESTBIANXIN2.getString("idValue"));
        Debug.log("Z161TESTBIANXIN2+++++++++++"+Z161TESTBIANXIN2.getString("productId"));

        
        //Z161TEST7不是原材料；跑完服务查看是否有影响
        GenericValue Z161TEST7=delegator.findOne("GoodIdentification", 
        		UtilMisc.toMap("goodIdentificationTypeId","ZUCZUG_CODE","productId","Z161TEST7"),true);
        assertNull(Z161TEST7);
        
	}
	
}

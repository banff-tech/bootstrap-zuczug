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

public class ZuczugCommonTest extends OFBizTestCase {
	public static final String module = ZuczugCommonTest.class.getName();

    protected GenericValue userLogin = null;

    public ZuczugCommonTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testGetVirtualProductIdsByGroup() throws Exception {
        Map<String, Object> gvpbgCtx = FastMap.newInstance();
        List<?> productIds = null;
        List<?> warningList = FastList.newInstance();
        gvpbgCtx.put("productFeatureId", "DEMOGROUP");
        gvpbgCtx.put("userLogin", userLogin);
        Map<String, Object> respMap0 = dispatcher.runSync("getVirtualProductIdsByGroup", gvpbgCtx);
        productIds = UtilGenerics.checkList(respMap0.get("productIds"));
        assertEquals(1, productIds.size());
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);
        
    }
    

    public void testGetProductIdByDesignProductId() throws Exception {
        Map<String, Object> ctx = FastMap.newInstance();
        String productId = null;
        List<?> warningList = FastList.newInstance();
        ctx.put("designProductId", "DemoDesignProduct");
        ctx.put("userLogin", userLogin);
        Map<String, Object> respMap0 = dispatcher.runSync("getProductIdByDesignProductId", ctx);
        productId = (String) respMap0.get("productId");
        assertEquals("DemoProduct", productId);
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);
        
        ctx.put("designProductId", "DemoDesignProduct-001-2");
        ctx.put("userLogin", userLogin);
        respMap0 = dispatcher.runSync("getProductIdByDesignProductId", ctx);
        productId = (String) respMap0.get("productId");
        assertEquals("DemoProduct-62-2", productId);
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);

        ctx.put("designProductId", "DemoDesignProduct-002-4");
        ctx.put("userLogin", userLogin);
        respMap0 = dispatcher.runSync("getProductIdByDesignProductId", ctx);
        productId = (String) respMap0.get("productId");
        assertEquals("DemoProduct-P60-4", productId);
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);
    }
    
    public void testCheckProductRepeatByFeature() throws Exception {
    	Debug.logInfo("++++++++++++++++++++++++++++++++++=========================== entering testCheckProductRepeatByFeature", module);
    	Map<String, Object> pkFields = FastMap.newInstance();
    	pkFields.put("productId", "DemoRibbon1-62-11");
    	GenericValue ribbon1 = delegator.findByPrimaryKey("Product", pkFields);
    	if (null == ribbon1) {
        	Debug.logInfo("++++++++++++++++++++++++++++++++++=========================== there is no DemoRibbon1-62-11 in test db", module);
    	} else {
        	Debug.logInfo("++++++++++++++++++++++++++++++++++=========================== found product with id = " + ribbon1.getString("productId"), module);
    	}
    	assertEquals("DemoRibbon1-62-11", ribbon1.getString("productId"));
        Map<String, Object> ctx = FastMap.newInstance();
        String checkProduct = null;
        List<?> warningList = FastList.newInstance();
        List<String> featureIds = FastList.newInstance();
        featureIds.add("COLOR_62");
        featureIds.add("11MM");
        ctx.put("productId", "DemoRibbon1");
        // ctx.put("primaryProductCategoryId", "ACCE_RIBBON");
        ctx.put("productFeatures", featureIds);
        ctx.put("userLogin", userLogin);
        Map<String, Object> respMap0 = dispatcher.runSync("checkProductRepeatByFeature", ctx);
        checkProduct = (String) respMap0.get("checkProduct");
        assertEquals("DemoRibbon1-62-11", checkProduct);
        warningList = UtilGenerics.checkList(respMap0.get("warningMessageList"));
        assertNull(warningList);
    	
    }
}

package com.zuczug.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import com.zuczug.product.ZuczugProductUtils;

public class ExcelUtilService {
	
	public static String downloadExcel(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
	 	Delegator delegator = (Delegator) request.getAttribute("delegator");
	 	LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	 	String waveId = (String) paramMap.get("waveId");
	 	String subSeriesId = (String) paramMap.get("subSeriesId");
	 	String seriesId = (String) paramMap.get("seriesId");
	 	String seasonId = (String) paramMap.get("seasonId");
	 	String groupName = (String) paramMap.get("groupName");
	 	String productName = (String) paramMap.get("productName");
	 	String productId = (String) paramMap.get("productId");
	 	String isAll = (String) paramMap.get("isAll");
	 	String largeUrl = "";
	 	byte[] imgByte = null;
	 	int index = 0;
	 	List<String> orderBy = UtilMisc.toList("-fromDate");
	 	Map<String, String> parameters = UtilMisc.toMap(
				"isVirtual", "Y",
				"isVariant", "N", 
				"waveId", waveId,
				"subSeriesId", subSeriesId,
				"seriesId", seriesId,
				"seasonId", seasonId,
				"groupName", groupName,
				"productName", productName,
				"productId", productId
				);
	 	
	 	try {
	 		Map<String, Object> realProducts =  dispatcher.runSync("performFind", UtilMisc.toMap("entityName", "ProductRealView", "inputFields", parameters, "noConditionFind", "Y"));
	 		EntityListIterator listIt = (EntityListIterator) realProducts.get("listIt");
	 		List<GenericValue> realVirtualProductList = listIt.getCompleteList();
	 		HSSFWorkbook workbook = new HSSFWorkbook();//产生工作簿对象 
			HSSFSheet sheet = workbook.createSheet();//产生工作表对象
			sheet.setColumnWidth(0, 6000);//设置列宽
			sheet.setColumnWidth(1, 6000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 6000);
			sheet.setColumnWidth(4, 6000);
			sheet.setColumnWidth(5, 6000);
			sheet.setColumnWidth(6, 6000);
			sheet.setColumnWidth(7, 6000);
			sheet.setColumnWidth(8, 6000);
			sheet.setColumnWidth(9, 6000);			
			sheet.setColumnWidth(10, 6000);
			sheet.setColumnWidth(11, 6000);
			sheet.setColumnWidth(12, 6000);
			sheet.setColumnWidth(13, 6000);
			sheet.setColumnWidth(14, 6000);
			sheet.setColumnWidth(15, 6000);
			workbook.setSheetName(0, "商品数据");//设定sheet名称			
			// 生成一个样式  
	        HSSFCellStyle style = workbook.createCellStyle();  
	        // 设置这些样式   
	        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直
	        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平 
	        // 生成一个字体  
	        HSSFFont font = workbook.createFont();   
	        font.setFontHeightInPoints((short) 14);  
	        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
	        // 把字体应用到当前的样式  
	        style.setFont(font);  			
			//产生一行  
			HSSFRow row = sheet.createRow(index); 
			row.setHeightInPoints(25);
			index++;
			//产生第一个单元格  
			HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("照片");			
			cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("SKU");
			cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("货号");
			cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("颜色编号");
			cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("颜色说明");
			cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("款型");
			cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("内部名称");
			cell = row.createCell(7, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("外部名称");
			cell = row.createCell(8, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("吊牌价");
			cell = row.createCell(9, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("系列");
			cell = row.createCell(10, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("子系列");
			cell = row.createCell(11, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("类别");
			cell = row.createCell(12, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("年份");
			cell = row.createCell(13, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("季节");
			cell = row.createCell(14, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("波段");
			cell = row.createCell(15, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("上货日期");
			cell = row.createCell(16, HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			cell.setCellValue("商品描述");
			List<GenericValue> products = null;
			
			
			for (GenericValue realVirtualProduct: realVirtualProductList) {
				List<GenericValue> productContentList = null;
				GenericValue DS = null;			
				GenericValue productSizeFeature  = null;
				GenericValue dsProductAssoc = null;
				GenericValue dsProduct = null;
				GenericValue DSP = null;
				GenericValue productContent = null;
				GenericValue content = null;
				GenericValue dataResource = null;
				products = delegator.findByAnd("ProductAssocViewWithVariant", UtilMisc.toMap("productId", realVirtualProduct.get("productId")));				
				for (int i = 0 ; i < products.size(); i++) {
					productSizeFeature = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl",UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo"), "productFeatureTypeId", "SIZE")));
					if(isAll.equals("Y") || "4".equals(productSizeFeature.get("idCode")) || "27".equals(productSizeFeature.get("idCode")) || "F".equals(productSizeFeature.get("idCode"))) {
						//图片查询
						dsProductAssoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc",UtilMisc.toMap("productIdTo", (String) products.get(i).get("productIdTo"), "productAssocTypeId", "UNIQUE_ITEM")));
						if (UtilValidate.isNotEmpty(dsProductAssoc)) {
							//从设计商品拿取变形设计商品图片路径
							dsProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", (String) dsProductAssoc.get("productId")));
							largeUrl = (String) dsProduct.get("largeImageUrl");
							//从content拿取变形设计商品图片路径
//							if (UtilValidate.isEmpty(largeUrl)) {
//								productContentList = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", (String) dsProductAssoc.get("productId"),"productContentTypeId", "MODELS_POSITIVE_IMAGE"), orderBy);
//								if (UtilValidate.isNotEmpty(productContentList)) {
//									productContent = EntityUtil.getFirst(productContentList);
//									content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId",productContent.get("contentId")));
//									dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId",content.get("dataResourceId")));
//									largeUrl = (String) dataResource.get("objectInfo");
//								}
//							}
							//从设计商品拿取虚拟设计商品图片路径
							if (UtilValidate.isEmpty(largeUrl)) {								
								DS = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc",UtilMisc.toMap("productIdTo", (String) dsProductAssoc.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT")));
								if (UtilValidate.isNotEmpty(DS)) {
									DSP = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", (String) DS.get("productId")));
									largeUrl = (String) DSP.get("largeImageUrl");
								}
							}
//							//从content拿取变形虚拟商品图片路径
//							if (UtilValidate.isEmpty(largeUrl)) {							
//								if (UtilValidate.isNotEmpty(DS)) {
//									productContentList = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", (String) DS.get("productId"),"productContentTypeId", "MODELS_POSITIVE_IMAGE"), orderBy);
//									if (UtilValidate.isNotEmpty(productContentList)) {
//										productContent = EntityUtil.getFirst(productContentList);
//										content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId",productContent.get("contentId")));
//										dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId",content.get("dataResourceId")));
//										largeUrl = (String) dataResource.get("objectInfo");
//									}
//								}								
//							}						
							if (UtilValidate.isNotEmpty(largeUrl)) {
								String str = System.getProperty("ofbiz.home");
								String imgURL = (String) str + "/framework/images/webapp" + largeUrl;			    
							    File img = new File(imgURL);
								InputStream is = new FileInputStream(img);
							    ByteArrayOutputStream bos =  new ByteArrayOutputStream();
							    BufferedInputStream in = new BufferedInputStream(is);							    
							    int buf_size = 1024;  
					            byte[] buffer = new byte[buf_size];  
					            int len = 0;  
					            while (-1 != (len = in.read(buffer, 0, buf_size))) {  
					                bos.write(buffer, 0, len);  
					            }					            
					            imgByte = bos.toByteArray();
							}
						}
						
						
			            
						//基础数据查询
						GenericValue productColor = EntityUtil.getFirst(delegator.findByAnd("ProductDesignFeatureWithType", UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo"), "productFeatureTypeId", "COLOR")));
						GenericValue productStyle = EntityUtil.getFirst(delegator.findByAnd("ProductDesignFeatureWithType", UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo"), "productFeatureTypeId", "STYLE")));
						GenericValue productPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo"), "productPriceTypeId", "LIST_PRICE")));
						GenericValue wave = EntityUtil.getFirst(delegator.findByAnd("ProductCategoryMemberView", UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo"), "productCategoryTypeId", "WAVE")));
						GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", (String)products.get(i).get("productIdTo")));					
						Map<String, String> groupInfo = ZuczugProductUtils.getVariantGroupCategorys(delegator, (String)products.get(i).get("productIdTo"));
						GenericValue group =  EntityUtil.getFirst(delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", groupInfo.get("groupId"))));
						GenericValue series =  EntityUtil.getFirst(delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", groupInfo.get("seriesId"))));
						GenericValue season =  EntityUtil.getFirst(delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", groupInfo.get("seasonId"))));
						GenericValue subSeries =  EntityUtil.getFirst(delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", groupInfo.get("subSeriesId"))));
						
						HSSFRow dataRow = sheet.createRow(index);
						dataRow.setHeightInPoints(180);
						HSSFCell dataCell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						if (UtilValidate.isNotEmpty(dsProductAssoc)) {
							if (UtilValidate.isNotEmpty(largeUrl)) {
								HSSFPatriarch patriarch = sheet.createDrawingPatriarch(); 
								HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1020, 255,(short) 0, index, (short) 0, index);  
					            anchor.setAnchorType(3);     
					            //插入图片    
					            patriarch.createPicture(anchor, workbook.addPicture(imgByte, HSSFWorkbook.PICTURE_TYPE_JPEG)); 
							}
						}						
						dataCell = dataRow.createCell(1, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String)products.get(i).get("productIdTo"));
						dataCell = dataRow.createCell(2, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String)products.get(i).get("productId"));
						dataCell = dataRow.createCell(3, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String) productColor.get("idCode"));
						dataCell = dataRow.createCell(4, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String) productColor.get("description"));
						dataCell = dataRow.createCell(5, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						if (UtilValidate.isNotEmpty(productStyle)) {
							dataCell.setCellValue((String)productStyle.get("productFeatureId"));
							dataCell = dataRow.createCell(6, HSSFCell.CELL_TYPE_STRING);
							dataCell.setCellStyle(style);
						}
						dataCell.setCellValue((String) product.get("internalName"));
						dataCell = dataRow.createCell(7, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String) product.get("productName"));
						dataCell = dataRow.createCell(8, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((BigDecimal) productPrice.get("price") + "");
						dataCell = dataRow.createCell(9, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String) series.get("description"));
						dataCell = dataRow.createCell(10, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						if (UtilValidate.isNotEmpty(subSeries)) {
							dataCell.setCellValue((String) subSeries.get("categoryName"));
							dataCell = dataRow.createCell(11, HSSFCell.CELL_TYPE_STRING);
							dataCell.setCellStyle(style);
						}
						
						dataCell.setCellValue((String) group.get("categoryName"));
						dataCell = dataRow.createCell(12, HSSFCell.CELL_TYPE_STRING);
						Timestamp releaseDate = (Timestamp)product.get("releaseDate");
						dataCell.setCellStyle(style);
						dataCell.setCellValue(releaseDate.toString().substring(0, 4));
						dataCell = dataRow.createCell(13, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						dataCell.setCellValue((String) season.get("categoryName"));
						dataCell = dataRow.createCell(14, HSSFCell.CELL_TYPE_STRING);
						dataCell.setCellStyle(style);
						if (UtilValidate.isNotEmpty(wave)) {
							dataCell.setCellValue((String) wave.get("categoryName"));
							dataCell = dataRow.createCell(15, HSSFCell.CELL_TYPE_STRING);
							dataCell.setCellStyle(style);
						}
						dataCell.setCellValue(releaseDate.toString().substring(0, 11));
						index++;
					}
				}
			}
			String fileName = "商品信息";			
			FileOutputStream fOut = new FileOutputStream("./productInfoTemp.xls");  
			workbook.write(fOut);  
			fOut.flush();  
			fOut.close();  
			
			File txtFile = new File("./productInfoTemp.xls");
	        
	        FileInputStream in = new FileInputStream(txtFile);		
			
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			response.setContentLength((int)txtFile.length());
			//fetch the file
		 	int length = (int)txtFile.length();
		 	if(length != 0)  {
		   		byte[] buf = new byte[4096]; 
		   		ServletOutputStream op = response.getOutputStream();
				while ((in != null) && ((length = in.read(buf)) != -1))  {
					op.write(buf,0, length);
				}
			   	in.close();
		   		op.flush();
		   		op.close();
		 	}
		 	txtFile.delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_", "数据库路径找不到文件，请联系IT部门");
			return "error";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
		return "success";		
	}
	
	public static String upLoadExcel(HttpServletRequest request, HttpServletResponse response) {
		try { 
			DiskFileItemFactory factory = new DiskFileItemFactory();		
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			Delegator delegator = (Delegator) request.getAttribute("delegator");
		 	LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		 	HttpSession session = request.getSession();
	        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
			//设置上传文件大小的上限，-1表示无上限 
			fileUpload.setSizeMax(-1);
			List items = new ArrayList();
			//上传文件，解析表单中包含的文件字段和普通字段
			items = fileUpload.parseRequest(request);
			//遍历字段进行处理
			Iterator iterator = items.iterator();
			while(iterator.hasNext()){
				FileItem fileItem =(FileItem)iterator.next();
				InputStream input = fileItem.getInputStream();
				Workbook wb = new HSSFWorkbook(input);  
				Sheet sheet = wb.getSheetAt(0);  //获得第一个表单  
	            Iterator<Row> rows = sheet.rowIterator(); //获得第一个表单的迭代器
	            int productIdCellIndex = 0;//记录货号的位置
	            int realProductDescriptionIndex = 0;//记录商品描述的位置
	            boolean productIdCellOk = false; 
	            boolean realProductDescriptionOk = false;
	            int cellIndex = 0;//记录游标
	            while (rows.hasNext()) {  
	                Row row = rows.next();  //获得行数据  
	                if (productIdCellOk && realProductDescriptionOk) {
	                	Cell productIdCell = row.getCell(productIdCellIndex);
	                	Cell realProductDescriptionCell = row.getCell(realProductDescriptionIndex);
	                	if (UtilValidate.isEmpty(productIdCell) || UtilValidate.isEmpty(realProductDescriptionCell)) {
	                		continue;
	                	}
	                	String productId = productIdCell.getStringCellValue();
	                	String realProductDescription = realProductDescriptionCell.getStringCellValue();	
	                	if (UtilValidate.isEmpty(realProductDescription) || UtilValidate.isEmpty(productId)) {
	                		continue;
	                	}
	                	List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "REAL_PRODUCT_DESCRIPTION"));
	                	if (UtilValidate.isNotEmpty(productContents)) {
	                		GenericValue productContent = EntityUtil.getFirst(productContents);
		                	GenericValue content = delegator.findOne("Content", true, UtilMisc.toMap("contentId",productContent.get("contentId")));
		                	GenericValue electronicText = delegator.findOne("ElectronicText", true, UtilMisc.toMap("dataResourceId",content.get("dataResourceId")));
		                	dispatcher.runSync("updateElectronicText", UtilMisc.toMap(
		                				"dataResourceId",content.get("dataResourceId"),
		                				"textData",realProductDescription,
		                				"userLogin",userLogin
		                			));
	                	} else {
	                		String dataResourceId = delegator.getNextSeqId("DataResource");
	                		dispatcher.runSync("createDataResource", UtilMisc.toMap(
	                				"dataResourceId", dataResourceId,
	                				"dataResourceTypeId","ELECTRONIC_TEXT",
	                				"dataTemplateTypeId","NONE",
	                				"dataResourceName","realProductDescription",
	                				"mimeTypeId","text/html",
	                				"isPublic","Y",
	                				"statucId","IM_APPROVED",
	                				"userLogin",userLogin
	                				));
	            			dispatcher.runSync("createElectronicText", UtilMisc.toMap(
	            					"dataResourceId",dataResourceId,
	            					"textData",realProductDescription,
	            					"userLogin",userLogin
	            					));
	            			String contentId = delegator.getNextSeqId("Content");
	            			dispatcher.runSync("createContent", UtilMisc.toMap(
	            						"contentId",contentId,
	            						"contentTypeId","ANNOTATION",
	            						"dataResourceId",dataResourceId,
	            						"statucId","IM_APPROVED",
	            						"userLogin",userLogin
	            					));
	            			dispatcher.runSync("createProductContent", UtilMisc.toMap(
	            						"productId",productId,
	            						"contentId",contentId,
	            						"productContentTypeId","REAL_PRODUCT_DESCRIPTION",
	            						"userLogin",userLogin
	            					));
	                	}
	                	
	                	
	                } else {
	                	//获得行号从0开始  
	                    Iterator<Cell> cells = row.cellIterator();    //获得第一行的迭代器                  
	                    while (cells.hasNext()) {  
	                        Cell cell = cells.next();  
	                        switch (cell.getCellType()) {   //根据cell中的类型来输出数据  
	                        case HSSFCell.CELL_TYPE_NUMERIC:                     
	                            break;  
	                        case HSSFCell.CELL_TYPE_STRING:  
	                            if (cell.getStringCellValue().equals("SKU")) {
	                            	productIdCellIndex = cellIndex;
	                            	productIdCellOk = true;
	                            }
	                            if (cell.getStringCellValue().equals("商品描述")) {
	                            	realProductDescriptionIndex = cellIndex;
	                            	realProductDescriptionOk = true;
	                            }
	                            break;  
	                        case HSSFCell.CELL_TYPE_BOOLEAN:    
	                            break;  
	                        case HSSFCell.CELL_TYPE_FORMULA:    
	                            break;  
	                        default:  
	                            System.out.println("unsuported sell type");  
	                        break;   
	                        } 
	                        cellIndex++;
	                    }
	                }  
	            }  
			}
			request.setAttribute("_EVENT_MESSAGE_", "导入成功");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "success";		
	}


}
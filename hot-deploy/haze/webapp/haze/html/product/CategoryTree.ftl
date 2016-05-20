
<script type="text/javascript">
<#-- some labels are not unescaped in the JSON object so we have to do this manualy -->
function unescapeHtmlText(text) {
    return jQuery('<div />').html(text).text()
}
jQuery(window).load(createTree());

<#-- creating the JSON Data -->
var rawdata = [
        <#if (completedTree?has_content)>
            <@fillTree rootCat = completedTree/>
        </#if>
        
        <#macro fillTree rootCat>
            <#if (rootCat?has_content)>
                <#list rootCat as root>
                    {
                    "data": {"title" : unescapeHtmlText("<#if root.categoryName?exists>${root.categoryName?js_string} [${root.productCategoryId}]<#else>${root.productCategoryId?js_string}</#if>"), "attr": {"href" : "<@ofbizUrl>/EditProdCatalog?prodCatalogId=${root.productCategoryId}</@ofbizUrl>","onClick" : "callDocument('${root.productCategoryId}', 'catalog','${parameters.productTypeId?if_exists}');"}},
                    "attr": {"id" : "${root.productCategoryId}", "rel" : "root", "isCatalog" : "${root.isCatalog?string}" ,"isCategoryType" : "${root.isCategoryType?string}" ,"productTypeId" : "${parameters.productTypeId?if_exists}"}
                    <#if root.child?exists>
                    ,"state" : "closed"
                    </#if>
                    <#if root_has_next>
                        },
                    <#else>
                        }
                    </#if>
                </#list>
            </#if>
        </#macro>
     ];
console.log(rawdata);
 <#-- create Tree-->
  function createTree() {
    jQuery(function () {
        <#-- reset the tree when user browsing out of scope of catalog manager -->
        <#if stillInCatalogManager>
            $.cookie('jstree_select', null);
            $.cookie('jstree_open', null);
        <#else>
        <#-- Coloring the category when type the product categoryId manualy at the url bar -->
            $.cookie('jstree_select', "<#if productCategoryId?exists>${productCategoryId}<#elseif prodCatalogId?exists>${prodCatalogId}<#elseif showProductCategoryId?exists>${showProductCategoryId}</#if>");
        </#if>
        jQuery("#tree").jstree({
        "plugins" : [ "themes", "json_data","ui" ,"cookies", "types"],
            "json_data" : {
               		"data" : rawdata,
		          	"ajax" : { 
			          	"url" : "<@ofbizUrl>getCatalogChild</@ofbizUrl>", 
		          		"type" : "POST",
		         		"data" : function (n) {
			                return { 
			                    "isCategoryType" :  n.attr ? n.attr("isCatalog").replace("node_","") : 1 ,
			                    "isCatalog" :  n.attr ? n.attr("isCatalog").replace("node_","") : 1 ,
			                    "productCategoryId" : n.attr ? n.attr("id").replace("node_","") : 1 ,
			                    "additionParam" : "','category','${parameters.productTypeId?if_exists}" ,
			                    "hrefString" : "EditCategory?productCategoryId=" ,
			                    "onclickFunction" : "callDocument"
			            	}; 
	        		}
             }
            },
            "types" : {
             "valid_children" : [ "root" ],
             "types" : {
                 "CATEGORY" : {
                     "icon" : { 
                         "image" : "/images/jquery/plugins/jsTree/themes/apple/d.png",
                         "position" : "10px40px"
                     }
                 }
             }
         }
        });
    });
  }
  
  function callDocument(id,type,productTypeId) {
    //jQuerry Ajax Request
    //var dataSet = {};
    //if(type == "catalog") {
        //$("#content").load("<@ofbizUrl>EditProdCatalog?prodCatalogId="+ id + "&productTypeId=" + productTypeId + "</@ofbizUrl>");
    //} else {
       // $("#content").load("<@ofbizUrl>EditProductCategory?productCategoryId="+ id +"&productTypeId=" + productTypeId + "</@ofbizUrl>");
    //}
    
    var dataSet = {};
    if(type == "catalog") {
        URL = 'EditProdCatalog';
        dataSet = {"prodCatalogId" : id, "productTypeId" : productTypeId};
    } else {
        URL = 'EditProductCategory';
        dataSet = {"productCategoryId" : id, "productTypeId" : productTypeId};
    }
    jQuery.ajax({
        url: URL,
        type: 'POST',
        data: dataSet,
        error: function(msg) {
            alert("An error occured loading content! : " + msg);
        },
        success: function(msg) {
            jQuery('#centerdiv').html(msg);
        }
    });

  }
</script>

<div id="tree" style="width:100%;min-height:100%;float:left"></div>


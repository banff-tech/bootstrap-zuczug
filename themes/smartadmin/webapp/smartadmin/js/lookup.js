var number = 0 ;
var inputFieldIdClick = "" ;

function Lookup(date){
    /*
    requestUrl,
    inputFieldId,
    dialogTarget,
    dialogOptionalTarget,
    formName,
    width,
    height,
    position,
    modal,
    ajaxUrl,
    showDescription,
    presentation,
    defaultMinLength,
    defaultDelay,
    args
     */

    if(number ==0){
        //给整个弹出窗口的背景一个蒙层background: #aaaaaa url(images/ui-bg_flat_0_aaaaaa_40x100.png) 50% 50% repeat-x;
        var overlay = '<div style="z-index: 999;width: 100%;height: 100%;display: none;background-color: rgb(204, 204, 204);-moz-opacity:0.8;opacity: 0.3;outline: 0px; position: absolute;top:0px;" id="overlay" onclick="closeLookUp();"></div>' ;
        $("#main").after(overlay);
    }

    //if(number != 0){
    //    return ;
    //}

    var requestUrl = date['requestUrl'];
    var inputFieldId = date['inputFieldId'];
    var dialogTarget = date['dialogTarget'];
    var dialogOptionalTarget = date['dialogOptionalTarget'];
    var formName = date['formName'];
    var width = date['width'];
    var height = date['height'];
    var position = date['position'];
    var modal = date['modal'];
    var ajaxUrl = date['ajaxUrl'];
    var showDescription = date['showDescription'];
    var presentation = date['presentation'];
    var defaultMinLength = date['defaultMinLength'];
    var defaultDelay = date['defaultDelay'];
    var args = date['args'];

    if(width==null || width == ""){
        width = "800";
    }
    //alert($(document).height());
    if(height==null || height == ""){
        height = 600 ;
    }
    var buttonMarginLeft = width * 0.165;

    //用来存放 div 的 id
    var divId = inputFieldId + "_div";
    //生成的 div
    var divInfo = "<div name='lookupDivName' id='"+divId+"' style='z-index: 1000; outline: 0px; position: absolute; display: none; background-color: #ccc; max-width:"+width+"px; width:100%; max-height:"+height+"px;'></div>";
    $("#main").after(divInfo);


    var queryArgs = "presentation=" + presentation;
    if (typeof args == "object" && jQuery.isArray(args)) {
        for (var i = 0; i < args.length; i++) {
            queryArgs += "&parm" + i + "=" + jQuery(args[i]).val();
        }
    }

    jQuery.ajax({
        type: "post",
        url: requestUrl,
        data: queryArgs,
        timeout: 5000,
        cache: false,

        success: function (data) {
            //alert(divId);
            //将获取过来的前面的菜单栏给去掉
            data = data.replace('id="main"',"");
            $("#"+divId).append(data);
            jQuery("#" + divId + " section[id='widget-grid']").css("max-height",(height-73)+"px");
            handle(divId);
        }
    });

    number++;
    //alert(inputFieldId);
}

function handle(divId){
    var lookupForm = jQuery("#" + divId + " form:first");
    lookupForm.attr("name", "form_" + divId);
    lookupForm.attr("id", "form_" + divId);
    //将提交按钮换成 button
    var submitButton = jQuery("#" + divId + " input[type='submit']");
    var buttonMarginLeft = $("#"+divId).width() * 0.165;
    //alert(buttonMarginLeft);
    submitButton.css("margin-left",buttonMarginLeft);
    submitButton.attr("type","button");
    submitButton.click(function(){
        queryData();
    });

    //将影藏和放大按钮删除掉
    jQuery("#" + divId + " a[data-original-title='Collapse']").remove();
    jQuery("#" + divId + " a[data-original-title='Fullscreen']").remove();

    //设置所有弹出的 div 距离下方的距离
    jQuery("#" + divId + " article").css("margin-bottom","-14px");
    //给列表中的 lib 添加 css 样式
    jQuery("#" + divId + " td label[class='col-md-2 control-label']").css("width","100%","margin-left","-13px");

    //将header移动到整个页面的外部显示
    var headerHtml = jQuery("#" + divId + " header[role='heading']").html() ;
    var headerDiv = '<div class="jarviswidget jarviswidget-sortable" style="margin-bottom: 0px;" data-widget-colorbutton="false" data-widget-editbutton="false" data-widget-custombutton="false" role="widget"><header role="heading">'+headerHtml+'</header></div>';
    //alert(headerDiv);
    jQuery("#" + divId + " header[role='heading']").remove();
    jQuery("#" + divId + " section[id='widget-grid']").before(headerDiv);

    jQuery("#" + divId + " section[id='widget-grid']").css("overflow","auto");
    var height = jQuery("#" + divId).height();
    jQuery("#" + divId + " section[id='widget-grid']").css("max-height",(height-73)+"px");

    //给删除按钮加上点击时间，关闭弹出层
    jQuery("#" + divId + " a[data-original-title='Delete']").click(function(){
        closeLookUp();
    });

    //给 table 添加样式
    jQuery("#" + divId + " table").removeClass("basic-table").addClass("table-striped");
    jQuery("#" + divId + " table").removeClass("hover-bar");

    jQuery("#" + divId + " table").addClass("table");
    jQuery("#" + divId + " table").addClass("table-bordered");
    jQuery("#" + divId + " table").addClass("table-hover");
    jQuery("#" + divId + " table").addClass("dataTable");
    jQuery("#" + divId + " table").addClass("no-footer");

    jQuery("#" + divId + " table tbody tr td a").removeClass("smallSubmit");

    jQuery("#" + divId + " table tbody tr td a").addClass("btn");
    jQuery("#" + divId + " table tbody tr td a").addClass("btn-labeled");
    jQuery("#" + divId + " table tbody tr td a").addClass("btn-default");
    jQuery("#" + divId + " table tbody tr td a").addClass("link");

    jQuery("#" + divId + " table tbody tr td a").each(function(){
        //alert($(this).text());
        var text = $(this).text();
        $(this).removeClass("btn-primary").addClass("btn-labeled");
        $(this).removeAttrs("href");
        $(this).empty();
        $(this).append('<span class="btn-label"><i class="glyphicon glyphicon-chevron-right"></i></span>'+text);
        $(this).click(function(){
            var onClickText = $(this).text().trim();
            getValue(onClickText);
        });
    });


}

function closeLookUp(){
    $("#overlay").css("display","none");
    $("[name='lookupDivName']").css("display","none");
}

function showDiv(divId){
    //这里是获取当前显示需要写入值的 ID
    inputFieldIdClick = $(divId).attr("id").replace("_button","");

    $("#"+inputFieldIdClick+"_div").css({'top':document.body.scrollTop,'left':0, 'display':'block'});
    $("#overlay").css("display","block");
}

function queryData(){
    var data = jQuery("#form_" + inputFieldIdClick + "_div").serialize();
    var actions = jQuery("#form_" + inputFieldIdClick + "_div").attr("action");
    queryIng(data,actions);
}

function queryIng(data,actions){
    var divId = inputFieldIdClick + "_div" ;
    jQuery.ajax({
        url: actions,
        type: "POST",
        data: data,
        success: function(result) {
            result = result.replace('id="main"',"");
            result = result.replace(/submitPagination/g,'nextPageLookUp');

            //btn-labeled
            $("#"+divId).empty();
            $("#"+divId).append(result);
            handle(divId);
        }
    });
}

function getValue(data){
    //alert(data);
    $("#" + inputFieldIdClick ).val(data);
    $("#" + inputFieldIdClick).css("background-color","yellow");
    closeLookUp();
}

function nextPageLookUp(val1,val2){
    queryIng(null,val2);
}
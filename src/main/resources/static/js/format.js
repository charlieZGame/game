/*
var json = $("#LornajsonTextBox").val();
var jsonObj = null;
try {
    //// 成功解析
    jsonObj = JSON.parse(json);
} catch (e) {
    //// 抛出异常
    console.log(e);
}
*/

function formatAction(target,jsonData) {
    try {
        var jsonObj = JSON.parse(jsonData);
    } catch (e) {
        alert(e);
        console.log(e);
    }
    var response = formatJson(jsonObj, 1);
    target.innerHTML = response;
}

var typeEnum = {
    TYPE_STRING: "string",
    TYPE_INT: "number",
    TYPE_OBJECT: "object",
    TYPE_BOOLEAN: "boolean",
};

/**
 * 格式化JSON
 * @jsonObj object
 * @tabIndex 显示的缩进等级
 **/
function formatJson(jsonObj, tabIndex) {
    //// 声明索引，是否数组元素，该jsonobj长度
    var innerhtml = "", idx = 0, isArray = jsonObj instanceof Array, length = 0;
    if (jsonObj != null) {
        length = Object.keys(jsonObj).length;
    }
    //// 遍历对象
    for (var obj in jsonObj) {
        //// 句末是否需要加上逗号
        var isD = idx + 1 != length;
        var preInnerHtml = "";
        ///// 如果是OBJECT类型，继续递归
        if (typeof jsonObj[obj] == typeEnum.TYPE_OBJECT) {
            if (isArray) {
                //// 包装数组-object
                preInnerHtml = getObjectArrayDiv(formatJson(jsonObj[obj], tabIndex + 1), isD);
            } else {
                //// 包装object
                preInnerHtml = getObjectDiv(obj, formatJson(jsonObj[obj], tabIndex + 1), isD);
            }

        } else {
            //// 普通类型直接包装展示
            if (isArray) {
                preInnerHtml = getArrayDiv(jsonObj[obj], isD);
            } else {
                preInnerHtml = getDiv(obj, jsonObj[obj], isD);
            }
        }
        innerhtml += preInnerHtml;
        idx++;
    }
    //// 包装本次的OBJECT
    return getPanel(innerhtml, tabIndex, isArray, length);
}



function getDiv(key, value, isD) {
    return "<div>" + getTitleSpan(key) + ":" + getValueSpan(value) + (isD ? "," : "") + "</div>";;
}

//// 包装Object类型
function getObjectDiv(key, value, isD) {
    return "<div>" + getTitleSpan(key) + ":" + value + (isD ? "," : "") + "</div>";;
}

//// 包装数组内对象
function getObjectArrayDiv(value, isD) {
    return "<div>" + value + (isD ? "," : "") + "</div>";;
}

//// 包装数组
function getArrayDiv(value, isD) {
    return "<div>" + getValueSpan(value) + (isD ? "," : "") + "</div>";;
}

//// 包装Key
function getTitleSpan(value) {
    return "<span class='key'>\"" + value + "\"</span>";
}

//// 包装Value
function getValueSpan(value) {
    var type = typeof value;

    switch (type) {
        case typeEnum.TYPE_STRING:
            return "<span class='value_string'>\"" + value + "\"</span>";
        case typeEnum.TYPE_INT:
            return "<span class='value_int'>" + value + "</span>";
        case typeEnum.TYPE_BOOLEAN:
            return "<span class='value_bool'>" + value + "</span>";
    }
    return "error";
}

//// 包装object
function getPanel(innerHtml, tabIndex, isArray, index) {
    if (isArray) {
        return "<span class=\"\"><i>-</i>[</span><div class=\"tab_" + tabIndex + "\">" + innerHtml + "</div><label class=\"tips\">Array <span class='tips_math'>" + index + "<span></label><span>]</span>";
    } else {
        return "<span class=\"\"><i>-</i>{</span><div class=\"tab_" + tabIndex + "\">" + innerHtml + "</div><label class=\"tips\">Object{...}</label><span>}</span>";
    }
}

//// 缩进事件
function folding() {
    var text = $(this).text();
    if (text == "-") {
        $(this).text("+");
        $(this).parent().parent().find("div").hide();
        $(this).parent().parent().find("label").show();
    } else {
        $(this).text("-");
        $(this).parent().parent().find("div").show();
        $(this).parent().parent().find("label").hide();
    }

}
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script src="js/jquery-1.11.1.min.js"></script>
    <script src="js/format.js"></script>

    <style type="text/css">
        td{border:solid #000000 1px; padding: 3px; margin: 0px;}
        .table1 tr:hover,.table1 tr.hilite
        {
            background-color:#c0e2b3;
            color:#000000;
        }
    </style>

    <script type="text/javascript">

        function changeValue() {
            var myselect = document.getElementById("type");
            var index = myselect.selectedIndex;
            var value = myselect.options[index].value;
            if(value == 1){
                document.getElementById("eclpOrderId").value = "ESL57194882539272";
            }else if(value == 2){
                document.getElementById("eclpOrderId").value = "{'waybillId':'LD1001170670','carrierCode':'BDB'}";
            }
        }


        function submit() {

            var role = document.getElementById("role").value;
            var type = document.getElementById("type").value;
            var eclpOrderId = document.getElementById("eclpOrderId").value;
            $.ajax({
                url: '<%=request.getContextPath() %>/trace/getTrace.do',
                data: {
                    role:role,
                    type:type,
                    eclpOrderId:eclpOrderId
                },
                async: false,
                cache: false,
                success: function (data) {
                    response = data;
                    document.getElementById("table1").innerHTML = data;
                },
                error: function (req) {
                    alert("error method 任务执行失败 textStatus:");
                }
            });
        }

        function format() {
            formatAction(document.getElementById("table1"),response);
        }

    </script>
</head>
<body>
<table>
    <tr><td>ES数据源</td><td>${esDataSource}</td></tr>
    <tr><td>ES索引信息</td><td>${esIndex}</td></tr>
    <tr><td>ES类型信息</td><td>${esType}</td></tr>
</table>
<input type="button" onclick="submit()" value="查询全程跟踪信息" />
<input type="button" onclick="format()" value="格式化返回数据" />
<br />
<table id="table1" class="table1" cellspacing="0">
</table>
</body>
</html>

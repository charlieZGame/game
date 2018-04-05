<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="common.jsp" %>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>来源麻将移动端-后台管理</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="<%=basePath%>/js/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%=basePath%>/js/AdminLTE-2.3.0/plugins/font-awesome-4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="<%=basePath%>/js/AdminLTE-2.3.0/css/AdminLTE.min.css">
    <link rel="stylesheet" href="<%=basePath%>/js/AdminLTE-2.3.0/css/skins/_all-skins.min.css">
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
    <!-- Header -->
    <header class="main-header">
        <!-- Logo -->
        <a href="#" class="logo">
            <!-- mini logo for sidebar mini 50x50 pixels -->
            <span class="logo-mini">Trace</span>
            <!-- logo for regular state and mobile devices -->
            <span class="logo-lg"><b>全程跟踪</b></span>
        </a>
        <!-- Header Navbar: style can be found in header.less -->
        <nav class="navbar navbar-static-top" role="navigation">
            <!-- Sidebar toggle button-->
            <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>

            <div class="navbar-custom-menu">


            </div>
        </nav>
    </header>

    <!-- Left side column. contains the logo and sidebar -->
    <aside class="main-sidebar">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
            <ul class="sidebar-menu">
                <li class="treeview">
                    <a href="#">
                        <i class="fa fa-files-o"></i>
                        <span>接口列表</span>
                    </a>
                    <ul class="treeview-menu">
                        <li><a href="/trace/tracePage.do" target="content"><i class="fa fa-circle-o text-red"></i><span>获取全程跟踪</span></a></li>
                        <li><a href="/trace/initOutsideInterfacePage.do" target="content"><i class="fa fa-circle-o text-red"></i><span>外部接口调用测试</span></a></li>
                        <li><a href="/trace/initCarrierCode.do" target="content"><i class="fa fa-circle-o text-red"></i><span>承运商编码对照表</span></a></li>
                    </ul>
                </li>
            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>


    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <iframe name="content" src="index.html" style="overflow: hidden;" frameborder="0" width="100%" height="100%"></iframe>
    </div><!-- /.content-wrapper -->


    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark">
        <!-- Create the tabs -->
        <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
            <li><a href="#control-sidebar-home-tab" data-toggle="tab"><i class="fa fa-home"></i></a></li>

            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i class="fa fa-gears"></i></a></li>
        </ul>


    </aside>
    <!-- /.control-sidebar -->

</div>

<!-- jQuery 2.1.4 -->
<script src="<%=basePath%>/js/AdminLTE-2.3.0/plugins/jQuery/jQuery-2.1.4.min.js"></script>
<!-- Bootstrap 3.3.5 -->
<script src="<%=basePath%>/js/bootstrap/js/bootstrap.min.js"></script>
<!-- FastClick -->
<script src="<%=basePath%>/js/AdminLTE-2.3.0/plugins/fastclick/fastclick.min.js"></script>
<!-- AdminLTE App -->
<script src="<%=basePath%>/js/AdminLTE-2.3.0/js/app.min.js"></script>
<!-- AdminLTE for demo purposes -->
<script src="<%=basePath%>/js/AdminLTE-2.3.0/js/demo.js"></script>

<script>
    $(function(){
        var header = $(".main-header");
        var content = $(".content-wrapper");
        var footer = $(".main-footer");
        content.css("min-height",content.outerHeight()-header.outerHeight()-footer.outerHeight());
        content.height(content.outerHeight()-header.outerHeight()-footer.outerHeight());
    });
</script>
</body>
</html>


<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Cache-Control" content="no-siteapp" />
<meta name="viewport"
	content="width=device-width, maximum-scale=1.0, initial-scale=1.0,initial-scale=1.0,user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<title>涞源麻将游戏-运营管理平台</title>
<link rel="shortcut icon" type="image/x-icon" href="/images/favicon.ico?t=1487250759056"/>
<link rel="stylesheet" href="/css/flexboxgrid.min.css">
<link rel="stylesheet" type="text/css" href="/css/darktooltip.css" />
<link rel="stylesheet" href="/css/layui.css">
<link rel="stylesheet" href="/js/ztree/zTreeStyle/zTreeStyle.css">
<link rel="stylesheet" href="/js/select/css/select2.min.css"/>	
<link rel="stylesheet" href="/css/beimi.css">

<script src="/js/jquery-1.10.2.min.js"></script>

<script src="/js/jquery.form.js"></script>

<script src="/js/select/js/select2.min.js"></script>

<script src="/layui.js"></script>
<script src="/js/ukefu.js"></script>
<script src="/im/js/socket.io.js"></script>	
<script src="/js/ace/ace.js" type="text/javascript" charset="utf-8"></script>
<script src="/js/ace/theme-chrome.js" type="text/javascript" charset="utf-8"></script>
<script src="/js/weixinAudio.js"></script>

<script src="/js/ztree/jquery.ztree.all.min.js"></script>
<script type="text/javascript" src="/js/jquery.darktooltip.js"></script>
<script language="javascript">
	var layinx , layerhelper ;
	$(document).ready(function(){
		layui.use('layer', function(){
			layerhelper = layer ;
			<#if Request["msg"]?? && Request["msg"] == "security">
			layer.alert("您访问的资源需要安全验证，请确认您有系统管理员权限！", {icon: 2});
			</#if>
		});
		$(".ukefu-left-menu").darkTooltip({
    		gravity: "west"
    	});
	});
	function closeentim(){
		if(layerhelper){
			layerhelper.close(layinx);
		}	
	}
</script>

</head>

<body>
	<div class="layui-layout layui-layout-admin">
		<div class="layui-header header header-ukefu">
			<div class="layui-main">
				<a class="logo" href="/"><img src="images/logo.png"></a>
				<ul class="layui-nav">
					<li class="layui-nav-item layui-this">
						<a href="javascript:void(0)" onclick="return false;" data-title="首页" data-href="/apps/content.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
							<i class="kfont" style="position: relative;">&#xe717;</i>
							首页
						</a>
					</li>
					<li class="layui-nav-item " style="position: relative;">
						<div class="ukefu-last-msg" data-num="0" id="ukefu-last-msg">
							<small class="ukefu-msg-tip bg-red" id="msgnum">0</small>
						</div>
						<a href="javascript:void(0)" id="agentdesktop" onclick="return false;"  data-title="运营" data-href="/apps/platform/index.html" class="iframe_btn" data-id="operate" data-type="tabAdd">
							<i class="kfont" style="position: relative;">&#xe6bc;</i>
							运营
						</a>
					</li>
					<li class="layui-nav-item " style="position: relative;">
						<div class="ukefu-last-msg" data-num="0" id="ukefu-last-msg">
							<small class="ukefu-msg-tip bg-red" id="msgnum">0</small>
						</div>
						<a href="javascript:void(0)" id="agentdesktop" onclick="return false;"  data-title="业务" data-href="/apps/platform/index.html" class="iframe_btn" data-id="business" data-type="tabAdd">
							<i class="layui-icon" style="position: relative;">&#xe857;</i>
							业务
						</a>
					</li>
					<li class="layui-nav-item " style="position: relative;">
						<div class="ukefu-last-msg" data-num="0" id="ukefu-last-msg">
							<small class="ukefu-msg-tip bg-red" id="msgnum">0</small>
						</div>
						<a href="javascript:void(0)" id="agentdesktop" onclick="return false;"  data-title="数据" data-href="/apps/platform/index.html" class="iframe_btn" data-id="bi" data-type="tabAdd">
							<i class="layui-icon" style="position: relative;">&#xe629;</i>
							数据
						</a>
					</li>
					<#if user?? && user.usertype == "0">
					<#if models?? && models["dev"]!=null>
					<li class="layui-nav-item " style="position: relative;">
						<div class="ukefu-last-msg" data-num="0" id="ukefu-last-msg">
							<small class="ukefu-msg-tip bg-red" id="msgnum">0</small>
						</div>
						<a href="javascript:void(0)" id="agentdesktop" onclick="return false;"  data-title="开发平台" data-href="/apps/dev/index.html" class="iframe_btn" data-id="bi" data-type="tabAdd">
							<i class="layui-icon" style="position: relative;">&#xe62d;</i>
							开发平台
						</a>
					</li>
					</#if>
					<li class="layui-nav-item ">
						<a href="javascript:void(0)" onclick="return false;"  data-title="系统" data-href="/admin/content.html" class="iframe_btn" data-id="admin" data-type="tabAdd">
							<i class="layui-icon" style="position: relative;">&#xe631;</i>
							系统
						</a>
					</li>
					</#if>
					
					<li class="layui-nav-item"><a href="javascript:void(0)">
						<i class="layui-icon" style="position: relative;">&#xe612;</i>
						<#if user??>${user.email!''}</#if></a>
						<dl class="layui-nav-child">
					      <dd><a href="/apps/profile.html" data-toggle="ajax" data-width="750" data-title="修改资料">个人资料</a></dd>
					      <dd><a href="/logout.html">退出系统</a></dd>
					    </dl>
					</li>
				</ul>
			</div>
		</div>
		<div class="layui-side layui-bg-black">
			<div class="layui-side-scroll">
				<ul class="layui-nav layui-nav-tree site-ukefu-nav">

					<li class="layui-nav-item layui-nav-itemed">
						<dl class="layui-nav-child">
							
							<dd class="ukefu-left-menu">
								<a href="javascript:void(0)" onclick="return false;" data-title="首页" data-href="/apps/content.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="kfont" style="top: 1px;">&#xe717;</i>
								</a>
							</dd>
							
							<dd class="ukefu-left-menu" data-tooltip="账号设置">
								<a href="javascript:void(0)" data-title="账号设置" data-href="/apps/platform/config/account.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="layui-icon" style="position: relative;">&#xe620;</i>
								</a>
							</dd>
							<dd class="ukefu-left-menu" data-tooltip="玩法设置">
								<a href="javascript:void(0)" data-title="玩法设置" data-href="/apps/platform/config/game.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="layui-icon" style="position: relative;">&#xe614;</i>
								</a>
							</dd>
							<dd class="ukefu-left-menu" data-tooltip="AI设置">
								<a href="javascript:void(0)" data-title="AI设置" data-href="/apps/platform/config/ai.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="kfont" style="position: relative;">&#xe63a;</i>
								</a>
							</dd>
							<dd class="ukefu-left-menu" data-tooltip="在线玩家">
								<a href="javascript:void(0)" data-title="在线玩家" data-href="/apps/platform/online/gameusers.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="kfont" style="position: relative;">&#xe60b;</i>
								</a>
							</dd>
							<dd class="ukefu-left-menu" data-tooltip="游戏房间">
								<a href="javascript:void(0)" data-title="游戏房间" data-href="/apps/platform/gameroom.html" class="iframe_btn" data-id="maincontent" data-type="tabChange">
									<i class="layui-icon" style="position: relative;">&#xe735;</i>
								</a>
							</dd>

						</dl>
					</li>
					<li class="layui-nav-item" style="height: 30px; text-align: center"></li>
				</ul>
			</div>
		</div>
		<div class="layui-body">
			<div class="layui-tab product-tab" lay-filter="ukefutab" lay-allowClose="true">
				<ul class="layui-tab-title">
					<li lay-id="maincontent" class="layui-this ukefu-home"><i class="kfont"
						style="position: relative;">&#xe717;</i> 首页</li>
				</ul>
				<div class="layui-tab-content product-content ukefu-tab">
					<div class="layui-tab-item layui-show" style="height:100%;">
						<iframe frameborder="0" src="/apps/content.html" id="maincontent" name="maincontent" width="100%" height="100%"></iframe>
					</div>
				</div>
			</div>

		</div>
	</div>
	<script>
			layui.use('element', function(){
  			  var element = layui.element;
			  //触发事件
			  $('.iframe_btn').on('click', function(){
				var type = $(this).data('type');
				if(type == "tabAdd"){
					active.tabAdd($(this).data('href') , $(this).data('title'), $(this).data('id'));
				}else if(type == "tabChange"){
					active.tabChange($(this).data('href') , $(this).data('title'), $(this).data('id'));
				}
				$(this).parent().addClass("layui-this");
			  });
			});
			
	</script>
</body>

</html>
var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,


    properties: {
          // 这个属性引用了星星预制资源
          useragreement: {
              default: null,
              type: cc.Node
          },

      },
    // use this for initialization
    onLoad: function () {

    },



    login:function(){
         cc.beimi.audio.playUiSound();
        if(!this.useragreement.active){
            this.alert("请先同意用户协议，再登录");
          return;
        }
        this.io = require("IOUtils");
        this.loadding();
        if(this.io.get("userinfo") == null){
            //发送游客注册请求
            var xhr = cc.beimi.http.httpGet("/api/guest?servertime="+new Date().getTime() , this.sucess , this.error , this);
        }else{
            //通过ID获取 玩家信息
            var data = JSON.parse(this.io.get("userinfo")) ;
            if(data.token != null){     //获取用户登录信息
                var xhr = cc.beimi.http.httpGet("/api/guest?token="+data.token.id, this.sucess , this.error , this);
            }
        }
	},
    sucess:function(result , object){
        var data = JSON.parse(result) ;
        if(data!=null && data.token!=null && data.data!=null){
            //放在全局变量
            object.reset(data , result);
            cc.beimi.gamestatus = data.data.gamestatus ;
            /**
             * 登录成功后即创建Socket链接
             */
            object.connect();
            //预加载场景
            if(cc.beimi.gametype!=null && cc.beimi.gametype != ""){//只定义了单一游戏类型 ，否则 进入游戏大厅
                object.scene(cc.beimi.gametype , object) ;
            }else{
                /**
                 * 暂未实现功能
                 */
            }
        }
    },
    error:function(object){
        object.closeloadding(object.loaddingDialog);
        object.alert("网络异常，服务访问失败");
    },


    wxlogin:function(){
        cc.beimi.audio.playUiSound();
        if(!this.useragreement.active){
            this.alert("请先同意用户协议，再登录");
          return;
        }
        this.loadding();
        var agent = anysdk.agentManager;
        var user_plugin = agent.getUserPlugin();

        user_plugin.setListener(this.onUserResult, this);

        user_plugin.login();
  },

  onUserResult:function(code, msg){
      cc.log("on user result action.");
      cc.log("msg:"+msg);
      cc.log("code:"+code); //这里可以根据返回的 code 和 msg 做相应的处理
      object.closeloadding(object.loaddingDialog);
      switch(code) {
          case anysdk.UserActionResultCode.kLoginSuccess:
              // 登录成功！
              break;
          case anysdk.UserActionResultCode.kLoginNetworkError: //登陆网络出错回调
              // 登录失败：网络异常，请稍后再试
              break;
          case anysdk.UserActionResultCode.kLoginCancel: //登陆取消回调
              // 您取消了登录
              break;
          case anysdk.UserActionResultCode.kLoginFail: //登陆失败回调
              //登陆失败后，游戏相关处理
              // "登录失败：" + msg
              break;
      }
  }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

var beiMiCommon = require("BeiMiCommon");
cc.Class({
  extends: beiMiCommon,
  properties: {
    useragreement: {
      default: null,
      type: cc.Node
    },
    //
    cards_current: {
      default: null,
      type: cc.Prefab
    },

    scoreHistoryDetailDialog:{
      default: null,
      type: cc.Prefab
    }
  },
  // use this for initialization
  onLoad: function() {
    this.io = require("IOUtils");
    this.wxlogining = false;
    console.log("wxUserinfo=====>", this.io.get("wxUserinfo"));
    if (this.io.get("wxUserinfo") != null) {
      this.wxlogin();
    }

    // let dialog = cc.instantiate(this.scoreHistoryDetailDialog) ;
    // cc.beimi.openwin = dialog;
    // cc.beimi.openwin.parent = this.root();
    // let dialogScript = dialog.getComponent("ScoreHistoryDialogDetail");
    // if (dialogScript) {
    //   dialogScript.init("2222");
    //   }

  },


  login: function() {
    cc.beimi.audio.playUiSound();
    var Base64 = require("Base64");
    if (!this.useragreement.active) {
      this.alert("请先同意用户协议，再登录");
      return;
    }

    this.loadding();
    // if(this.io.get("userinfo") == null){
    //发送游客注册请求
    var xhr = cc.beimi.http.httpGet("/api/guest?servertime=" + new Date().getTime(), this.sucess, this.error, this);
    console.log("注册新游客===");
    // }else{
    //     //通过ID获取 玩家信息
    //     var data = JSON.parse(this.io.get("userinfo")) ;
    //     if(data.token != null){     //获取用户登录信息
    //         var xhr = cc.beimi.http.httpGet("/api/guest?token="+data.token.id, this.sucess , this.error , this);
    //     }
    //     console.log("获取原来游客信息===");
    // }
  },
  sucess: function(result, object) {
    var data = JSON.parse(result);
    if (data != null && data.token != null && data.data != null) {
      //放在全局变量
      object.reset(data, result);
      cc.beimi.gamestatus = data.data.gamestatus;
      /**
             * 登录成功后即创建Socket链接
             */
      object.connect();
      //预加载场景
      if (cc.beimi.gametype != null && cc.beimi.gametype != "") { //只定义了单一游戏类型 ，否则 进入游戏大厅
        object.scene(cc.beimi.gametype, object);
      } else {
        /**
                 * 暂未实现功能
                 */
      }
    }
  },
  error: function(object) {
    object.closeloadding(object.loaddingDialog);
    object.alert("网络异常，服务访问失败");
  },

  wxlogin: function() {
    console.log("wxUserinfo=====>", this.io.get("wxUserinfo"));
    cc.beimi.audio.playUiSound();
    if (!this.useragreement.active) {
      this.alert("请先同意用户协议，再登录");
      return;
    }
    cc.beimi.sessiontimeout = false;
    if(2==2) {
        cc.beimi.user = {};
        cc.beimi.user.nickname = "杨柳依依";
        cc.beimi.user.avatar = "http://img.suncity.ink/game/2018/04/9999988.png";
        cc.beimi.user.sex = 2;
        cc.beimi.user.openId = "wx7788992669ok";
        cc.beimi.user.password = "187CBF3CEFC803087D733F8A6DDEBCD2";
        cc.beimi.http.httpGet("/wechart/login?openId=" + cc.beimi.user.openId +
          "&nickname=" + encodeURIComponent(cc.beimi.user.nickname) + "&sex=" + cc.beimi.user.sex +
          "&avatar=" + encodeURIComponent(cc.beimi.user.avatar)  + "&pwd=" + cc.beimi.user.password,
          this.sucess , this.error , this);
        return;
    }
    this.loadding();
    if (this.io.get("wxUserinfo") == null) {
      this.wxlogining = true;
      this.loginByWeiXin();
    } else {
      //通过ID获取 玩家信息
      var data = JSON.parse(this.io.get("wxUserinfo"));
      if (data.openId != null) {
        cc.beimi.user = data;
        cc.beimi.http.httpGet("/wechart/login?openId=" + cc.beimi.user.openId + "&nickname=" + encodeURIComponent(cc.beimi.user.nickname) + "&sex=" + cc.beimi.user.sex + "&avatar=" + encodeURIComponent(cc.beimi.user.avatar) + "&pwd=" + cc.beimi.user.password, this.sucess, this.error, this);
      } else {
        this.wxlogining = true;
        this.loginByWeiXin();
      }
    }
  },

  onUserResult: function(code, msg) {
    cc.log("on user result action.");
    cc.log("msg:" + msg);
    cc.log("code:" + code); //这里可以根据返回的 code 和 msg 做相应的处理
    object.closeloadding(object.loaddingDialog);
    switch (code) {
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
  },

  // called every frame, uncomment this function to activate update callback
  update: function(dt) {
    if (this.wxlogining) {
      if (this.getWxFlag()) {
        this.wxlogining = false;
        cc.beimi.user = {};
        cc.beimi.user.nickname = this.getNickname();
        cc.beimi.user.avatar = this.getAvatar() + "?a=1.jpg";
        cc.beimi.user.sex = this.getSex();
        cc.beimi.user.openId = this.getOpenId();
        cc.beimi.user.password = this.getPassword();
        this.io.put("wxUserinfo", JSON.stringify(cc.beimi.user));
        cc.beimi.http.httpGet("/wechart/login?openId=" + cc.beimi.user.openId + "&nickname=" + encodeURIComponent(cc.beimi.user.nickname) + "&sex=" + cc.beimi.user.sex + "&avatar=" + encodeURIComponent(cc.beimi.user.avatar) + "&pwd=" + cc.beimi.user.password, this.sucess, this.error, this);
      }
    }
  }
});

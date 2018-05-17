cc.Class({
  extends: cc.Component,

  properties: {
  },

  // use this for initialization
  onLoad: function() {
    cc.beimi.room_callback = null; //加入房间回调函数
    cc.beimi.isStartHearbeat = false;
  },
  ready: function() {
    var check = false;
    if (cc.beimi) {
      check = true;
    } else {
      cc.beimi.joinroom=false;
      this.scene("login", this);
    }
    cc.beimi.audio.playBGM("bgFight.mp3");
    return check;
  },
  connect: function() {
    if (cc.beimi.sessiontimeout) {
      return
    }
    let self = this;
    self.lastConnectTime = Date.now();
    cc.beimi.isConnect = false;
    self.loadding();
    if (cc.beimi.isConnecting) {
      return;
    }
    cc.beimi.isConnecting = true;
    /**
         * 登录成功后，创建 Socket链接，
         */
    if (cc.beimi.socket != null) {
      cc.beimi.socket.disconnect();
      cc.beimi.socket = null;
    }

    var param = {
      token: cc.beimi.authorization,
      orgi: cc.beimi.user.orgi
    };

    var opts = {
              'reconnection':true,
              'query':{
                token: cc.beimi.authorization,
                orgi: cc.beimi.user.orgi
              }
          }

    cc.beimi.socket = window.io.connect(cc.beimi.http.wsURL + '/bm/game?token='+cc.beimi.authorization,opts);
    cc.game.on(cc.game.EVENT_HIDE, function(event) {
      console.log("游戏在后台运行");
      //self.alert("HIDE TRUE");
    });
    cc.game.on(cc.game.EVENT_SHOW, function(event) {
      console.log("游戏在前台运行");
      //self.alert("SHOW TRUE");
      cc.beimi.lastRecordTime = 0;
      cc.beimi.lastRecieveTime = 0;
      cc.beimi.isPing = false;
      if(!cc.beimi.isConnect && cc.beimi.authorization != null) {
        self.connect();
      }
    });
    cc.beimi.socket.on('connect', function(data) {
      console.log("已经连接服务器");

      cc.beimi.isConnect = true;
      cc.beimi.lastRecieveTime = Date.now();
      cc.beimi.isPing = true;
      cc.beimi.socket.emit("heartbeat", cc.beimi.authorization);
      //self.alert("connected to server");
      self.startHearbeat();
      //每次连接就获取最新游戏状态
      cc.beimi.socket.emit("gamestatus", JSON.stringify(param));
      self.closeloadding();
    });

    cc.beimi.socket.on('disconnect', function(data) {
      console.log("与服务器连接中断");
      cc.beimi.isConnect = false;
      // cc.beimi.socket.open();

      // if (cc.find("Canvas/loadding")) {
      //   self.alert("网络繁忙，请稍后再试");
      // }
      if(Date.now() - self.lastConnectTime < 2000){
          return
      }
      if(cc.beimi.authorization != null) {
        cc.beimi.isConnecting = false;
        self.connect();
      }
    });

    cc.beimi.socket.on("tokenIllegal", function(result) {
        console.log("收到tokenIllegal消息",result);
        cc.beimi.sessiontimeout = true;
        self.alert("登录已过期，请重新登录");
    });

    cc.beimi.socket.on("gamestatus", function(result) {
      if (result != null) {
        var data = self.parse(result);
        if (data.gamestatus == "timeout") { //会话过期，退出登录 ， 会话时间由后台容器提供控制
          cc.beimi.sessiontimeout = true;
          self.closeloadding();
          self.alert("登录已过期，请重新登录");
        } else if (cc.beimi.extparams != null) {
          if (data.gamestatus == "playing" && data.gametype != null) {
            /**
                         * 修正重新进入房间后 玩法被覆盖的问题，从服务端发送过来的 玩法数据是 当前玩家所在房间的玩法，是准确的
                         */
            if (cc.beimi.extparams != null) {
              cc.beimi.extparams.playway = data.playway;
              cc.beimi.extparams.gametype = data.gametype;
              if (data.cardroom != null && data.cardroom == true) {
                cc.beimi.extparams.gamemodel = "room";
              }
            }
            if (data.gametype == "koudajiang") {
              //我添加的
              self.scene("majiang", self);
            } else {
              self.scene(data.gametype, self);
            }

          } else {
            console.error("cc.beimi.joinroom---->",cc.beimi.joinroom);
            console.error("cc.beimi.extparams---->",cc.beimi.extparams);
            if (cc.beimi.joinroom) {
              //创建房间，检查房卡

              // if (cc.beimi.extparams.gametype == "koudajiang") {
              //   self.scene("majiang", self);
              // } else {
              //   self.scene(cc.beimi.extparams.gametype, self);
              // }
              var param = {
                token: cc.beimi.authorization,
                playway: cc.beimi.extparams.playway,
                orgi: cc.beimi.user.orgi,
                extparams: cc.beimi.extparams
              };
              cc.beimi.socket.on("cardCheck", cc.beimi.cardCheck7788);
              cc.beimi.socket.emit("cardCheck", JSON.stringify(param));
            }
          }
        }
        cc.beimi.gamestatus = data.gamestatus;
      }
    });

    cc.beimi.cardCheck7788 = function(result) {
      var resultObj = self.parse(result);
      //房卡不够
      console.log("resultObj==cardCheck=>",resultObj.status);
      if(resultObj.status==-1){
         self.alert(resultObj.msg || '房间创建失败，请联系管理员');
      }else {
        if (cc.beimi.extparams&&cc.beimi.extparams.gametype&&cc.beimi.extparams.gametype == "koudajiang") {
          self.scene("majiang", self);
        } else if(cc.beimi.extparams&&cc.beimi.extparams.gametype){
          self.scene(cc.beimi.extparams.gametype, self);
        }
      }
    };
    cc.beimi.socket.on("cardCheck", cc.beimi.cardCheck7788);

    /**
         * 加入房卡模式的游戏类型 ， 需要校验是否是服务端发送的消息
         */
    cc.beimi.socket.on("searchroom", function(result) {
      //result 是 GamePlayway数据，如果找到了 房间数据，则进入房间，如果未找到房间数据，则提示房间不存在
      if (result != null && cc.beimi.room_callback != null) {
        cc.beimi.room_callback(result, self);
      }
    });



    // if(!this.isStartHearbeat){
    //   this.lastRecordTime = 0;
    //   this.lastRecieveTime = 0;
    //   this.isPing = false;
    //   this.startHearbeat();
    // }
    cc.beimi.isConnecting = false;
    return cc.beimi.socket;
  },

  startHearbeat:function(){
      let self = this;
      cc.beimi.isStartHearbeat = true;
      cc.beimi.socket.on('heartbeat',function(){
          console.log('--------------Recieve heartbeat------------');
          cc.beimi.lastRecieveTime = Date.now();
          cc.beimi.isPing = false;
      });
      setInterval(function(){
          if(Date.now() - cc.beimi.lastRecordTime < 2000){
              return
          }
          if(cc.beimi.authorization == null){
              clearInterval();
              return
          }
          if(!cc.beimi.isPing && (Date.now() - cc.beimi.lastRecordTime > 11000 ||
              Date.now() - cc.beimi.lastRecieveTime > 15000)){
              console.log('--------------Send heartbeat------------');
              console.error('-----------'+new Date()+'----------');
              cc.beimi.isPing = true;
              cc.beimi.lastRecieveTime = Date.now();
              cc.beimi.socket.emit("heartbeat", cc.beimi.authorization);
          } else if(cc.beimi.isPing && Date.now() - cc.beimi.lastRecieveTime > 11000){
              cc.beimi.isConnect = false;
              cc.beimi.isPing = false;
              console.log('--------------connect------------');
              self.connect();
          }
          cc.beimi.lastRecordTime = Date.now();
      },5000);
  },

  disconnect: function() {
    if (cc.beimi.socket != null) {
      cc.beimi.socket.disconnect();
      cc.beimi.socket = null;
    }
  },
  registercallback: function(callback) {
    cc.beimi.room_callback = callback;
  },
  cleancallback: function() {
    cc.beimi.room_callback = null;
  },
  getCommon: function(common) {
    var object = cc.find("Canvas/script/" + common);
    return object.getComponent(common);
  },
  loadding: function() {
    console.log("------------------------------loadding:"+cc.beimi.loadding.size()+"----------------------------");
    if (cc.beimi.loadding.size() > 0) {
      this.loaddingDialog = cc.beimi.loadding.get();
      this.loaddingDialog.parent = cc.find("Canvas");
      this._animCtrl = this.loaddingDialog.getComponent(cc.Animation);
      var animState = this._animCtrl.play("loadding");
      animState.wrapMode = cc.WrapMode.Loop;
    }
  },

  alert: function(message) {
    this.closeloadding();
    if (cc.beimi.dialog.size() > 0) {
      this.alertdialog = cc.beimi.dialog.get();
      this.alertdialog.parent = cc.find("Canvas");
      let node = this.alertdialog.getChildByName("message");
      if (node != null && node.getComponent(cc.Label)) {
        node.getComponent(cc.Label).string = message;
      }
    }
  },

  showQuitApp: function() {
    if (cc.beimi.quitDialog.size() > 0) {
      this.quitDialog = cc.beimi.quitDialog.get();
      this.quitDialog.parent = cc.find("Canvas");
    }
    this.closeloadding();
  },

  closeloadding: function() {
    if (cc.find("Canvas/loadding")) {
      cc.beimi.loadding.put(cc.find("Canvas/loadding"));
    }
  },
  closeOpenWin: function() {
    if (cc.beimi.openwin != null) {
      cc.beimi.openwin.destroy();
      cc.beimi.openwin = null;
    }
  },
  pvalistener: function(context, func) {
    cc.beimi.listener = func;
    cc.beimi.context = context;
  },
  cleanpvalistener: function() {
    cc.beimi.listener = null;
    cc.beimi.context = null;
  },
  pva: function(pvatype, balance) { //客户端资产变更（仅显示，多个地方都会调用 pva方法）
    if (pvatype != null) {
      if (pvatype == "gold") {
        cc.beimi.user.goldcoins = balance;
      } else if (pvatype == "cards") {
        cc.beimi.user.cards = balance;
      } else if (pvatype == "diamonds") {
        cc.beimi.user.diamonds = balance;
      }
    }
  },
  updatepva: function() {
    if (cc.beimi.listener != null && cc.beimi.context != null) {
      cc.beimi.listener(cc.beimi.context);
    }
  },
  resize: function() {
    let win = cc.director.getWinSize();
    cc.view.setDesignResolutionSize(win.width, win.height, cc.ResolutionPolicy.EXACT_FIT);
  },

  closealert: function() {
    if (cc.find("Canvas/alert")) {
      cc.beimi.dialog.put(cc.find("Canvas/alert"));
    }
  },

  scene: function(name, self) {
    let ctx = this;
    cc.director.preloadScene(name, function() {
      ctx.closeloadding();
      ctx.closealert();
      cc.director.loadScene(name);
    });
  },

  preload: function(extparams, self) {
    if (!cc.beimi.isConnect) {
      self.alert("网络繁忙，请稍后再试");
      if(cc.beimi.authorization != null) {
        self.connect();
      }
      return
    }

    this.loadding();
    /**
         *切换游戏场景之前，需要先检查是否 是在游戏中，如果是在游戏中，则直接进入该游戏，如果不在游戏中，则执行 新场景游戏
         */
    cc.beimi.extparams = extparams;
    console.log("========cc.beimi.extparams======",cc.beimi.extparams);
    cc.beimi.joinroom =  true;
    /**
         * 发送状态查询请求，如果玩家当前在游戏中，则直接进入游戏恢复状态，如果玩家不在游戏中，则创建新游戏场景
         */
    var param = {
      token: cc.beimi.authorization,
      orgi: cc.beimi.user.orgi
    };
    var req = cc.beimi.socket.emit("gamestatus", JSON.stringify(param));
  },

  root: function() {
    return cc.find("Canvas");
  },

  decode: function(data) {
    var cards = new Array();
    if (data != null) {
      const tempCards = data.split(",");
      for (var i = 0; i < tempCards.length; i++) {
        cards.push(parseInt(tempCards[i]));
      }
    }

    // if(!cc.sys.isNative) {
    //     var dataView = new DataView(data);
    //     for(var i= 0 ; i<data.byteLength ; i++){
    //         cards[i] = dataView.getInt8(i);
    //     }
    // }else{
    //     var Base64 = require("Base64");
    //     var strArray = Base64.decode(data) ;
    //     if(strArray && strArray.length > 0){
    //         for(var i= 0 ; i<strArray.length ; i++){
    //             cards[i] = strArray[i];
    //         }
    //     }
    // }
    return cards;
  },

  parse: function(result) {
    var data;
    if (!cc.sys.isNative) {
      data = result;
    } else {
      data = JSON.parse(result);
    }
    return data;
  },
  reset: function(data, result) {
    //放在全局变量
    cc.beimi.authorization = data.token.id;
    if(cc.beimi.user == null) {
        cc.beimi.user = data.data;
    } else {
        var u = cc.beimi.user;
        cc.beimi.user = data.data;
        cc.beimi.user.nickname = u.nickname;
        cc.beimi.user.avatar = u.avatar;
        cc.beimi.user.sex = u.sex;
        cc.beimi.user.openId = u.openId;
        cc.beimi.user.password = u.password;
    }
    cc.beimi.games = data.games;

    cc.beimi.gametype = data.gametype;
    cc.beimi.announcement = data.announcement;

    cc.beimi.data = data;
    cc.beimi.playway = null;
    this.io.put("userinfo", result);
  },
  logout: function() {
    this.closeOpenWin();
    cc.beimi.authorization = null;
    cc.beimi.user = null;
    cc.beimi.games = null;

    cc.beimi.playway = null;

    this.disconnect();
  },
  socket: function() {
    let socket = cc.beimi.socket;
    if (socket == null) {
      socket = this.connect();
    }
    return socket;
  },
  map: function(command, callback) {
    if (cc.beimi != null && cc.beimi.routes[command] == null) {
      cc.beimi.routes[command] = callback || function() {};
    }
  },
  cleanmap: function() {
    if (cc.beimi != null && cc.beimi.routes != null) {
      //cc.beimi.routes.splice(0 , cc.beimi.routes.length) ;
      for (var p in cc.beimi.routes) {
        delete cc.beimi.routes[p];
      }
    }
  },

  route: function(command) {
    return cc.beimi.routes[command] || function() {};
  },

  /**
     * 解决Layout的渲染顺序和显示顺序不一致的问题
     * @param target
     * @param func
     */
  layout: function(target, func) {
    if (target != null) {
      let temp = new Array();
      let children = target.children;
      children.sort(func);
    //   for (var inx = 0; inx < children.length; inx++) {
    //     temp.push(children[inx]);
    //      let handcards = children[inx].getComponent("HandCards");
    //     console.error("---children-----"+inx+"-------",handcards);
    //   }
    //   for (var inx = 0; inx < temp.length; inx++) {
    //     target.removeChild(temp[inx]);
    //   }
    //   // console.error("---------清空了target--------");
    //   temp.sort(func);
    //   console.error("---------完成排序--------");
    //   for (var inx = 0; inx < temp.length; inx++) {
    //     temp[inx].parent = target;
    //     let handcards1 =temp[i].getComponent("HandCards");
    //    console.error("---temp-----"+inx+"-------",handcards1);
    //   }
    //   console.error("---------赋值给target--------");
    //   temp.splice(0, temp.length);
    //   console.error("---------清空了临时--------");
   }
  },

  cardCheck : function(result) {
    console.log("---------------kkk-------------");
    var resultObj = self.parse(result);
    //房卡不够
    console.log("resultObj==cardCheck=>",resultObj.status);
    if(resultObj.status==-1){
       self.closeOpenWin();
       self.alert(resultObj.msg || '房间创建失败，请联系管理员');
    }else {
      console.log("---------------0000000000000000000000-------------");
      if (cc.beimi.extparams.gametype == "koudajiang") {
        console.log("---------------11111111111111111111-------------");
        self.scene("majiang", self);
      } else {
        console.log("--------------22222222222222222222222-------------");
        self.scene(cc.beimi.extparams.gametype, self);
      }
    }
  },

  /**
     * 分享
     */
  wxShare: function(title, description, url) {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      jsb.reflection.callStaticMethod("org/cocos2dx/javascript/AppActivity", "shareByWeiXin",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", title, description, url);
    } else if(cc.sys.os == cc.sys.OS_IOS){
      jsb.reflection.callStaticMethod("OCNativeForCocos", "shareByWeiXinWithTitle:description:url:", title, description, url);
    }
  },

  /**
     * 登录
     */
  loginByWeiXin: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      jsb.reflection.callStaticMethod("org/cocos2dx/javascript/AppActivity", "loginByWeiXin", "()V");
    } else if(cc.sys.os == cc.sys.OS_IOS){
      jsb.reflection.callStaticMethod("OCNativeForCocos", "loginByWeiXin");
    }
  },

  getWxFlag: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var flag = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getWxFlag", "()Z");
      return flag;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var flag = jsb.reflection.callStaticMethod("OCNativeForCocos", "getWxFlag");
      return flag;
    }
  },

  getOpenId: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var openId = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getOpenId", "()Ljava/lang/String;");
      return openId;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var openId = jsb.reflection.callStaticMethod("OCNativeForCocos", "getOpenId");
      return openId;
    }
  },

  getNickname: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var name = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getNickname", "()Ljava/lang/String;");
      return name;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var name = jsb.reflection.callStaticMethod("OCNativeForCocos", "getNickname");
      return name;
    }
  },

  getAvatar: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var avatar = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getAvatar", "()Ljava/lang/String;");
      return avatar;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var avatar = jsb.reflection.callStaticMethod("OCNativeForCocos", "getAvatar");
      return avatar;
    }
  },

  getSex: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var sex = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getSex", "()I");
      return sex;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var sex = jsb.reflection.callStaticMethod("OCNativeForCocos", "getSex");
      return sex;
    }
  },

  getMsg: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var msg = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getMsg", "()Ljava/lang/String;");
      return msg;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var sex = jsb.reflection.callStaticMethod("OCNativeForCocos", "getMsg");
      return sex;
    }
  },

  getPassword: function() {
    if(cc.sys.os == cc.sys.OS_ANDROID) {
      var pwd = jsb.reflection.callStaticMethod("com/baoding/majiang/Define", "getPassword", "()Ljava/lang/String;");
      return pwd;
    } else if(cc.sys.os == cc.sys.OS_IOS){
      var pwd = jsb.reflection.callStaticMethod("OCNativeForCocos", "getPassword");
      return pwd;
    }
  },

  // called every frame, uncomment this function to activate update callback
  // update: function (dt) {

  // },
});

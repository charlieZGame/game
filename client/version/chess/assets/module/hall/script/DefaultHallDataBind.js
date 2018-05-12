var beiMiCommon = require("BeiMiCommon");
cc.Class({
  extends: beiMiCommon,

  properties: {
    username: {
      default: null,
      type: cc.Label
    },
    goldcoins: {
      default: null,
      type: cc.Label
    },
    cards: {
      default: null,
      type: cc.Label
    },
    girl: {
      default: null,
      type: cc.Node
    },
    useravatar: {
      default: null,
      type: cc.Sprite
    },
    lblNotice: {
      default: null,
      type: cc.Label
    },
    lblNoticeLayout: {
      default: null,
      type: cc.Node
    },

    black:{
      default: null,
      type: cc.Node
    }

  },

  // use this for initialization
  onLoad: function() {
    let self = this;
    cc.beimi.isHasEnterRoom=1;
    console.error("大厅--onLoad---------");
    this.node.on('mousedown', function(event) {
      console.log("场景中的鼠标点击事件--mousedown---------");
    }, self);

    // self.girl.active = false;
    if (this.ready()) {
      if (cc.beimi.user.nickname == null) {
        this.username.string = cc.beimi.user.username;
        cc.loader.load("http://wx.qlogo.cn/mmhead/Q3auHgzwzM7Wz9H4T725hoCkR7dvYQibsgBSHNFYwtPjcfIiaPox3Deg/132?aa=aa.jpg", function(error, res) {
          this.useravatar.spriteFrame = new cc.SpriteFrame(res);
        }.bind(this));
      } else {
        this.username.string = cc.beimi.user.nickname + "  ID:" + cc.beimi.user.username;
        if (cc.beimi.user.avatar) {
          cc.loader.load(cc.beimi.user.avatar, function(error, res) {
            this.useravatar.spriteFrame = new cc.SpriteFrame(res);
          }.bind(this));
        }
      }

      if (cc.beimi.user.userCategory==3||cc.beimi.user.userCategory=="3") {
        this.black.active = true;
      }else {
        this.black.active = false;
      }
      this.pva_format(cc.beimi.user.playerlevel, cc.beimi.user.goldcoins, cc.beimi.user.cards, cc.beimi.user.diamonds, self);
      this.pvalistener(self, function(context) {
        console.error("更新pva");
        context.pva_format(cc.beimi.user.playerlevel, cc.beimi.user.goldcoins, cc.beimi.user.cards, cc.beimi.user.diamonds, context);
      });

      this.refreshNotice();

      //刷新房卡
      var param = {
        token: cc.beimi.authorization
      };
      cc.beimi.socket.emit("getUserInfo", JSON.stringify(param));
      cc.beimi.socket.on("getUserInfo", function(result) {
        /**
          * 更新个人账号资产信息
          */
        var data = self.parse(result);
        cc.beimi.user.playerlevel = data.playerlevel;
        cc.beimi.user.goldcoins = data.goldcoins;
        cc.beimi.user.cards = data.cards;

        /**
          * 刷新个人资产显示
          */
        self.updatepva();
      });

      cc.beimi.socket.on("proxyCreateRoom", function(result) {
        /**
          * 代开房间
          */
        var data = self.parse(result);
        console.log("proxyCreateRoom====>",data);
        self.closeOpenWin();
        self.alert("代开房间号:"+data.roomId);
      });
    }

    cc.beimi.socket.emit("searchHaveNotFinishGame", cc.beimi.authorization);
    cc.beimi.socket.on("searchHaveNotFinishGame", function(result) {
      /**
        * 获取是否有正在游戏的状态
        */
      var data = self.parse(result);
      console.error("searchHaveNotFinishGame====>",data);
      if (data.type==1||data.type=="1") {
          cc.beimi.isHasEnterRoom=1
      }else if (data.type==2||data.type=="2") {
          cc.beimi.isHasEnterRoom=2;
          cc.beimi.extparams = data;
      }

    });


    //cc.beimi.audio.playBGM("bgFight.mp3");
    cc.systemEvent.on(cc.SystemEvent.EventType.KEY_DOWN, this.onKeyDown, this);
  },

  onKeyDown: function(event) {
    switch (event.keyCode) {
      case cc.KEY.back:
        this.showQuitApp();
        break;
    }
  },

  update: function(dt) {
    var x = this.lblNotice.node.x;
    x -= dt * 100;
    if (x + this.lblNotice.node.width < -600) {
      x = 700;
    }
    this.lblNotice.node.x = x;
  },

  pva_format: function(playerlevel, coins, cards, diamonds, object) {
    if (coins > 9999) {
      var num = coins / 10000;
      object.goldcoins.string = num.toFixed(2) + "万 " + playerlevel;
    } else {
      object.goldcoins.string = coins + " " + playerlevel;
    }
    object.cards.string = cards + "张";
  },

  playToLeft: function() {
    this._girlAnimCtrl = this.girl.getComponent(cc.Animation);
    this._girlAnimCtrl.play("girl_to_left");
  },
  playToRight: function() {
    this._girlAnimCtrl = this.girl.getComponent(cc.Animation);
    this._girlAnimCtrl.play("girl_to_right");
  },

  onDestroy: function() {
    cc.systemEvent.off(cc.SystemEvent.EventType.KEY_DOWN, this.onKeyDown, this);
    this.cleanpvalistener();
  },

  refreshNotice: function() {
    if (cc.beimi.announcement != null && cc.beimi.announcement != '') {
      this.lblNotice.string = cc.beimi.announcement;
    } else {
      this.lblNoticeLayout.active = false;
    }
  }
});

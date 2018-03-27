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
    }
  },

  // use this for initialization
  onLoad: function() {
    let self = this;

    this.node.on('mousedown', function(event) {
      console.log("场景中的鼠标点击事件--mousedown---------");

    }, self);

    // self.girl.active = false;
    if (this.ready()) {
      this.username.string = cc.beimi.user.username;
      console.log("加载用户名字--", cc.beimi.user.username);

      this.pva_format(cc.beimi.user.goldcoins, cc.beimi.user.cards, cc.beimi.user.diamonds, self);
      this.pvalistener(self, function(context) {
        context.pva_format(cc.beimi.user.goldcoins, cc.beimi.user.cards, cc.beimi.user.diamonds, context);
      });

      cc.loader.load("http://wx.qlogo.cn/mmhead/Q3auHgzwzM7Wz9H4T725hoCkR7dvYQibsgBSHNFYwtPjcfIiaPox3Deg/132?aa=aa.jpg", function(error, res) {
        console.log("加载用户头像--", JSON.stringify(res));
        console.log("加载用户名字--", this.useravatar);
        this.useravatar.spriteFrame = new cc.SpriteFrame(res);
      }.bind(this));

      this.refreshNotice();
    }
    cc.beimi.audio.playBGM("bgMain.mp3");

    cc.systemEvent.on(cc.SystemEvent.EventType.KEY_DOWN, this.onKeyDown, this);
  },

  onKeyDown: function(event) {
        switch(event.keyCode) {
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

  pva_format: function(coins, cards, diamonds, object) {
    if (coins > 9999) {
      var num = coins / 10000;
      object.goldcoins.string = num.toFixed(2) + '万';
    } else {
      object.goldcoins.string = coins;
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
    if (cc.beimi.announcement!=null&&cc.beimi.announcement!='') {
        this.lblNotice.string = cc.beimi.announcement;
    }else {
        this.lblNoticeLayout.active = false;
    }

  },

});

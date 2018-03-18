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
    this.cleanpvalistener();
  },

  refreshNotice: function() {
    this.io = require("IOUtils");
    var data = JSON.parse(this.io.get("userinfo"));
    var xhr = cc.beimi.http.httpGet("/api/guest?token=" + data.token.id, this.sucess, this.error, this);
  },

  sucess: function(result, object) {
    var data = JSON.parse(result);
    if (data != null && data.token != null && data.data != null) {
      console.log("获取公告成功");
      object.lblNotice.string = '我是获取的公告信息---------';
    }
  },

  error: function(object) {
    if (this.lblNoticeLayout) {
        this.lblNoticeLayout.active = false;
    }
    console.log("获取公告失败");
  }
});

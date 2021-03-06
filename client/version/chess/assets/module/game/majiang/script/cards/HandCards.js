cc.Class({
  extends: cc.Component,

  properties: {
    atlas: {
      default: null,
      type: cc.SpriteAtlas
    },
    beimi0: {
      default: null,
      type: cc.SpriteAtlas
    },
    cardvalue: {
      default: null,
      type: cc.Node
    },
    target: {
      default: null,
      type: cc.Node
    },

    laizi: {
      default: null,
      type: cc.Node
    },

    copyCard: {
      default: null,
      type: cc.Node
    },

    copyCardMian: {
      default: null,
      type: cc.Node
    },

  },

  // use this for initialization
  onLoad: function() {
    this.lastonecard = false;
    this.take = false;
    this.isLaizi = false;
    this.copyCard.active = true;
  },

  init: function(cvalue, laiziValues, isKouCard, isResultCard) { //resultCard 是结果牌不能移动
    this.value = cvalue;
    this.isResultCard=0;
    if(isResultCard){
      this.isResultCard=1;
      this.copyCard.active = false;
    }

    this.koucard = 0;
    if (isKouCard) {
      this.koucard = 1;
      this.zIndex = cvalue - 1000;
      this.copyCard.active = false;
    }
    this.laizi.active = false;
    let cardframe;
    let cardcolors = parseInt(this.value / 4);
    let cardtype = parseInt(cardcolors / 9);

    this.mjtype = cardtype;
    this.mjvalue = parseInt((this.value % 36) / 4);

    let deskcard;
    this.lastonecard = false;
    if (cardcolors < 0) {
      deskcard = "wind" + (cardcolors + 8); //东南西北风 ， 中发白
    } else {
      if (cardtype == 0) { //万
        deskcard = "wan" + (parseInt((this.value % 36) / 4) + 1);
      } else if (cardtype == 1) { //筒
        deskcard = "tong" + (parseInt((this.value % 36) / 4) + 1);
      } else if (cardtype == 2) { //条
        deskcard = "suo" + (parseInt((this.value % 36) / 4) + 1);
      }
    }
    if (deskcard == "suo2") {
      cardframe = this.beimi0.getSpriteFrame('牌面-' + deskcard);
    } else {
      cardframe = this.atlas.getSpriteFrame('牌面-' + deskcard);
    }
    this.cardvalue.getComponent(cc.Sprite).spriteFrame = cardframe;
    this.copyCardMian.getComponent(cc.Sprite).spriteFrame = cardframe;

    if (laiziValues && laiziValues.length > 0) {
      for (var i = 0; i < laiziValues.length; i++) {
        const laiziValue = parseInt(laiziValues[i] / 4);
        if (laiziValue == cardcolors) {
          this.laizi.active = true;
          this.copyCard.active = false;
          return;
        }
      }
    }

    if (isKouCard) {
      this.setCardKou();
      console.error("------运行扣牌动画------");
    }

    // var anim = this.getComponent(cc.Animation);
    // anim.play("majiang_current");
  },

  lastone: function() {
    // if(this.lastonecard == false){
    console.log("放最后一张牌，宽度增加30");
    this.lastonecard = true;
    this.target.width = this.target.width + 30;
    // }
  },

  selected: function() {
    this.target.opacity = 168;
    this.selectcolor = true;
  },

  relastone: function() {
    if (this.lastonecard == true || this.target.width == 30) {
      console.log("---是relastone");
      this.lastonecard = false;
      this.target.width = this.target.width - 30;
    }
  },

  reinit: function() {
    this.relastone();
    this.lastonecard = false;
    this.selectcolor = false;
    this.target.opacity = 255;
    if (this.take) {
      this.target.y = this.target.y - 30;
      this.take = false;
    }
  },

  setCardKou: function() {
    if (this.copyCard.active) {
        this.copyCard.active = false;
    }

    this.koucard = 1;
    this.zIndex = this.value - 1000;
    var anim = this.getComponent(cc.Animation);
    anim.play("majiang_kou");
  }

});

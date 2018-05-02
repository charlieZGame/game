var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,
  properties: {
    target: {
      default: null,
      type: cc.Node
    },

    card: {
      default: null,
      type: cc.Node
    },

    // copyCard: {
    //   default: null,
    //   type: cc.Node
    // },

  },

  onLoad: function() {
    this.clickstate = false;
    var self = this;
    self.currenX = self.originalX = self.card.x;
    self.currenY = self.originalY = self.card.y;

    let handCards = self.target.getComponent("HandCards")
    this.copyCard = handCards.copyCard;
    let socket = this.socket();
    if (handCards.laizi.active) {
      if (self.copyCard) {
        self.copyCard.active = false;
      }
      return
    } else if (handCards.koucard) {
      if (self.copyCard) {
        self.copyCard.active = false;
      }
      return
    } else if (handCards.isResultCard) {
      if (self.copyCard) {
        self.copyCard.active = false;
      }
      return
    } else {
      if (self.copyCard) {
        self.copyCard.active = true;
      }
    }

    self.copyCard.on(cc.Node.EventType.TOUCH_MOVE, function(event) {
      console.log("TOUCH_MOVE11111111111111")
      self.copyCard.opacity = 180;
      var delta = event.touch.getDelta();
      self.copyCard.x += delta.x;
      self.copyCard.y += delta.y;
    });

    self.copyCard.on(cc.Node.EventType.TOUCH_END, function(event) {
      self.currenX = self.copyCard.x;
      self.currenY = self.copyCard.y;
      console.log("TOUCH_END", self.currenY - self.originalY)
      self.copyCard.x = self.originalX;
      self.copyCard.y = self.originalY;
      self.copyCard.opacity = 255;
      if (self.currenY - self.originalY > 80) {
        //出牌
        socket.emit("doplaycards", handCards.value);
      }
    });

    self.copyCard.on(cc.Node.EventType.TOUCH_CANCEL, function(event) {
      self.copyCard.x = self.originalX;
      self.copyCard.y = self.originalY;
      self.copyCard.opacity = 255;
    });

  },

  onClick: function() {
    let handCards = this.target.getComponent("HandCards")
    let self = this;
    console.log('点击的handCards.isLaizi---->', handCards);
    if (handCards.laizi.active) {
      console.log('点击的赖子--->');
      return
    }
    if (handCards.koucard) {
      console.log('点击的扣牌--->');
      return
    }

    if (this.clickstate == true) {
      //出牌
      console.log('click出牌--->');
      this.node.dispatchEvent(new cc.Event.EventCustom('takecard', true));
    } else {
      if (handCards.take == true) {
        handCards.take = false;
        this.target.y = this.target.y - 30;
      } else {
        if (self.currenY - self.originalY == 0) {
          handCards.take = true;
          this.target.y = this.target.y + 30;
        }
      }
      this.clickstate = true;
      setTimeout(function() { //双击算法
        self.clickstate = false;
      }, 800);
    }
  }
});

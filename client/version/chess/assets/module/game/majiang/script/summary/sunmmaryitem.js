
cc.Class({
    extends: cc.Component,

    properties: {
      dizhunode:{
        default: null,
        type: cc.Node
      },

      nickname:{
          default: null,
          type: cc.Label
      },

      username:{
          default: null,
          type: cc.Label
      },

      score:{
          default: null,
          type: cc.Label
      },

      card_panel:{
        default: null,
        type: cc.Node
      },

      hu:{
        default: null,
        type: cc.Node
      },

      cards_current: {
        default: null,
        type: cc.Prefab
      },

      cards_an_gang: {
        default: null,
        type: cc.Prefab
      },

      cards_ming_gang: {
        default: null,
        type: cc.Prefab
      },

      desc: {
        default: null,
        type: cc.Label
      },

      useravatar: {
        default: null,
        type: cc.Sprite
      },

    },


    start () {
    },

    onLoad: function() {

    },

   //手里牌 碰的桌面牌  赖子牌
    init(data,laizicards){//handcards, deskcards,laizicards,banker,nickname,score
      this.cardsArray = new Array();
      let pengcards=[];
      if (data.gameResultChecks&&data.gameResultChecks[0].pengs&&data.gameResultChecks[0].pengs.length>0) {
        let tempPengCards = this.decode(data.gameResultChecks[0].pengs);
        for (var i = 0; i < tempPengCards.length; i+=3) {
          pengcards.push(tempPengCards[i]);
        }
      }

      if (pengcards&&pengcards.length>0) {
        console.log("所有碰数据--》",pengcards);
        for (var i = 0; i < pengcards.length; i++) {
          let cards_peng = cc.instantiate(this.cards_ming_gang);
          cards_peng.setScale(0.8);
          cards_peng.width=104;
          let temp_script = cards_peng.getComponent("GangAction");
          temp_script.init(pengcards[i], false);
          cards_peng.parent = this.card_panel;
          this.cardsArray.push(cards_peng);
        }
      }

      if (data.gameResultChecks[0].mgangs&&data.gameResultChecks[0].mgangs.length>0) {
        let tempMgangCards = this.decode(data.gameResultChecks[0].mgangs);
        let mgangcards=[];
        for (var i = 0; i < tempMgangCards.length; i+=4) {
          mgangcards.push(tempMgangCards[i]);
        }
        console.log("所有明杠数据--》",mgangcards);
        if (mgangcards&&mgangcards.length>0) {
          for (var i = 0; i < mgangcards.length; i++) {
            let cards_peng = cc.instantiate(this.cards_ming_gang);
            cards_peng.setScale(0.8);
            cards_peng.width=118;
            let temp_script = cards_peng.getComponent("GangAction");
            temp_script.init(mgangcards[i], true);
            cards_peng.parent = this.card_panel;
            this.cardsArray.push(cards_peng);
          }
        }
      }

      let gangcards=[];
      if (data.gameResultChecks[0].agangs&&data.gameResultChecks[0].agangs.length>0) {
        let tempGangCards = this.decode(data.gameResultChecks[0].agangs);
        for (var i = 0; i < tempGangCards.length; i+=4) {
          gangcards.push(tempGangCards[i]);
        }
        if (gangcards&&gangcards.length>0) {
          for (var i = 0; i < gangcards.length; i++) {
            let cards_gang = cc.instantiate(this.cards_an_gang);
            cards_gang.setScale(0.8);
            cards_gang.width=144;
            let temp_script = cards_gang.getComponent("GangAction");
            temp_script.init(gangcards[i], true);
            cards_gang.parent = this.card_panel;
            this.cardsArray.push(cards_gang);
          }
        }
      }

      this.handlecardpool = new cc.NodePool();
      for (var i = 0; i < 14; i++) {
        this.handlecardpool.put(cc.instantiate(this.cards_current));
      }
      let handcards = [];
      if (data.gameResultChecks[0].pairs&&data.gameResultChecks[0].pairs.length>0) {
          handcards = handcards.concat(this.decode(data.gameResultChecks[0].pairs));
      }
      if (data.gameResultChecks[0].three&&data.gameResultChecks[0].three.length>0) {
        handcards = handcards.concat(this.decode(data.gameResultChecks[0].three));
      }

      if (data.gameResultChecks[0].others&&data.gameResultChecks[0].others.length>0) {
          handcards = handcards.concat(this.decode(data.gameResultChecks[0].others));
      }

      for (var i =0; i <handcards.length; i++) {
          let temp;
        if (this.handlecardpool.size() > 0) { // 通过 size 接口判断对象池中是否有空闲的对象
          temp = this.handlecardpool.get();
        } else { // 如果没有空闲对象，也就是对象池中备用对象不够时，我们就用 cc.instantiate 重新创建
          temp = cc.instantiate(this.cards_current);
        }

        temp.setScale(0.5);
        temp.width=38;
        let temp_script = temp.getComponent("HandCards");
        temp_script.init(handcards[i],laizicards,false,true);
        temp.parent = this.card_panel;
        this.cardsArray.push(temp);
      }
      if (data.dizhu) {
        this.dizhunode.active = true;
      }else {
        this.dizhunode.active = false;
      }

      if(data.win){
        this.hu.active = true;
      }else {
        this.hu.active = false;
      }
      this.nickname.string = data.nickName;
      this.username.string = data.username;
      this.desc.string = data.desc;
      this.score.string = data.score;
      let self = this;
      if (data.photo&&data.photo.length>0) {
        cc.loader.load(data.photo, function(error, res) {
          self.useravatar.spriteFrame = new cc.SpriteFrame(res);
        }.bind(self));
      }
    },


    decode:function(cardsData){
        var cards = new Array();
        if (cardsData!=null) {
          const tempCards = cardsData.split(",");
          for (var i = 0; i < tempCards.length; i++) {
            cards.push(parseInt(tempCards[i]));
          }
        }
          return cards ;
        },
    // update (dt) {},

    onDestroy: function() {
        for (var i = 0; i < this.cardsArray.length; i++) {
          this.cardsArray[i].destroy();
        }
    }
});

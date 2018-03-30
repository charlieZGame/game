
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

      score:{
          default: null,
          type: cc.Label
      },

      card_panel:{
        default: null,
        type: cc.Node
      },

      cards_current: {
        default: null,
        type: cc.Prefab
      },

      // cards_desk: {
      //   default: null,
      //   type: cc.Prefab
      // },

    },


    start () {

    },


   //手里牌 碰的桌面牌  赖子牌
    init(data){//handcards, deskcards,laizicards,banker,nickname,score
      this.handlecardpool = new cc.NodePool();
      for (var i = 0; i < 14; i++) {
        this.handlecardpool.put(cc.instantiate(this.cards_current));
      }

      // this.deskcardpool = new cc.NodePool();
      // for (var i = 0; i < 14; i++) {
      //   this.deskcardpool.put(cc.instantiate(this.cards_current));
      // }
      //
      // for (var i =0; i < data.deskcards.length; i++) {
      //   let temp = this.handlecardpool.get();
      //   temp.setScale(0.5);
      //   let temp_script = temp.getComponent("DeskCards");
      //   temp_script.init(data.deskcards[i]);
      //   temp.parent = this.card_panel;
      // }

      for (var i =0; i < data.handcards.length; i++) {
        let temp = this.handlecardpool.get();
        temp.setScale(0.5);
        let temp_script = temp.getComponent("HandCards");
        temp_script.init(data.handcards[i],data.laizicards);
        temp.parent = this.card_panel;
      }

      if (data.banker) {
        this.dizhunode.active = true;
      }else {
        this.dizhunode.active = false;
      }

      this.nickname.string = data.nickname;
      this.score.string = data.score;
    }
    // update (dt) {},
});

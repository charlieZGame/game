cc.Class({
    extends: cc.Component,
    properties: {
      tongcards_tip_layout: {
        default: null,
        type: cc.Node
      },

      tiaocards_tip_layout: {
        default: null,
        type: cc.Node
      },

      wancards_tip_layout: {
        default: null,
        type: cc.Node
      },

      zicards_tip_layout: {
        default: null,
        type: cc.Node
      },

      hucards_tip: { //我 将要胡的牌
        default: null,
        type: cc.Prefab
      },
   },

   onLoad: function() {

   },

    init:function(values){
      for (var i = 0; i < values.length; i++) {
        let cardcolors = parseInt(values[i] / 4);
        let cardtype = parseInt(cardcolors / 9);
        let hucards_tip;
        hucards_tip = cc.instantiate(this.hucards_tip);

        let temp_script = hucards_tip.getComponent("huCards");
        temp_script.init(values[i]);
        if (cardcolors < 0) { //东南西北风 ， 中发白
          hucards_tip.parent = this.zicards_tip_layout;
        } else {
          if (cardtype == 0) { //万
            hucards_tip.parent = this.wancards_tip_layout;
          } else if (cardtype == 1) { //筒
            hucards_tip.parent = this.tongcards_tip_layout;
          } else if (cardtype == 2) { //条
            hucards_tip.parent = this.tiaocards_tip_layout;
          }
        }
      }
    },

});

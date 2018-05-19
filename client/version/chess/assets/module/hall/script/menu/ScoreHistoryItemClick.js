var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
      target: {
        default: null,
        type: cc.Node
      },

      scoreHistoryDetailDialog: {
          default: null,
          type: cc.Prefab
      }


    },

    // use this for initialization
    onLoad: function () {

    },
    onClick:function(){
        cc.beimi.audio.playUiSound();
        console.log("关闭弹出框");
        this.closeOpenWin();
        let dialog = cc.instantiate(this.scoreHistoryDetailDialog) ;
        cc.beimi.openwin = dialog;
        cc.beimi.openwin.parent = this.root();
        let dialogScript = dialog.getComponent("ScoreHistoryDialogDetail");
        if (dialogScript) {
          dialogScript.init(this.target.getComponent("ScoreHistoryItem").roomUuid,this.target.getComponent("ScoreHistoryItem").roomId);
          }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

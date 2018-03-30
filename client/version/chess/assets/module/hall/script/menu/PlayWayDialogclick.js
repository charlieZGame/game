cc.Class({
    extends: cc.Component,

    properties: {
      target: {
        default: null,
        type: cc.Node
      }
    },


    playwayClick(){
      cc.beimi.audio.playUiSound();
      console.log("------------playwayClick--------");
      let dialogScore = this.target.getComponent("PlayWayDialog");
      dialogScore.selectPlayWay();
    },

    scoreClick(){
       cc.beimi.audio.playUiSound();
        console.log("------------scoreClick--------");
        let dialogScore = this.target.getComponent("PlayWayDialog");
        dialogScore.selectScore();
    }

    // update (dt) {},
});

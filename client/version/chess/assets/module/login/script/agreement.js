cc.Class({
    extends: cc.Component,

    properties: {
      user_agreement:{
        default:null,
        type:cc.Node
      },

      agreement_dialog: {
          default: null,
          type: cc.Node
      },

      close_agreement_dialog: {
          default: null,
          type: cc.Node
      },

      isAgreement: true
    },

    // LIFE-CYCLE CALLBACKS:

    onLoad () {
       this.user_agreement.active = true;
       this.agreement_dialog.active = false;
    },

    start () {

    },

    onAgreement:function(){
     cc.beimi.audio.playUiSound();
      this.user_agreement.active = !this.isAgreement;
      this.isAgreement = !this.isAgreement;
        console.log("onAgreement---------",this.user_agreement.active );
    },

    showAgreement:function(){
      cc.beimi.audio.playUiSound();
      this.agreement_dialog.active = true;
    },

    hideAgreement:function(){
      cc.beimi.audio.playUiSound();
      this.agreement_dialog.active = false;
    },


    // update (dt) {},
});

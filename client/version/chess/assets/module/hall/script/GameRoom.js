var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
        roomidDialog: {
            default: null,
            type: cc.Prefab
        }
    },

    // use this for initialization
    onLoad: function () {

    },
    onClick:function(event, data){
        cc.beimi.audio.playUiSound();
        this.loadding();
        let object = this ;
        setTimeout(function(){
            object.scene(data , object) ;
        } , 200);
    },
    onClickJoinRoom:function(){
      let self = this;
       cc.beimi.audio.playUiSound();
       if (cc.beimi.isHasEnterRoom==2) {
           self.preload(cc.beimi.extparams, this) ;
       }else {
         if(this.roomidDialog){
             cc.beimi.openwin = cc.instantiate(this.roomidDialog) ;
             cc.beimi.openwin.parent = this.root();
         }
       }
    },

});

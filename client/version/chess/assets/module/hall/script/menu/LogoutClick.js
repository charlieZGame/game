var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {

    },

    // use this for initialization
    onLoad: function () {

    },
    onClick:function(){
        cc.beimi.audio.playUiSound();
        this.logout();
        cc.beimi.joinroom=false;
        this.scene("login", this) ;
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {

    },

    // use this for initialization
    onLoad: function () {
        this.node.on(cc.Node.EventType.TOUCH_START, function(e){
            e.stopPropagation();
        });
        /**
         * 关闭ALERT的回调动作
         */
         let self = this;
        this.node.on("closeQuit", function (event) {
            event.stopPropagation();
        });
    },

    onClose:function(){
        let dialog = cc.find("Canvas/quitdialog") ;
        cc.beimi.quitDialog.put(dialog);
        this.node.dispatchEvent( new cc.Event.EventCustom("closeQuit", true) );
    },

    sureQuit:function(){
         cc.director.end();
    }
});

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
        this.node.on("close", function (event) {
            if(cc.beimi!=null && cc.beimi.sessiontimeout == true){
                cc.beimi.sessiontimeout = null;
                self.scene("login" , self) ;
            }else if (cc.beimi!=null &&cc.beimi.isLeaveroom) {
               console.log("====cc.beimi.isLeaveroom==========",cc.beimi.isLeaveroom);
                self.scene(cc.beimi.gametype, self);
                cc.beimi.joinroom=false;
                cc.beimi.isLeaveroom= false;
            }
            event.stopPropagation();
        });
    },
    onClose:function(){
        let dialog = cc.find("Canvas/alert") ;
        cc.beimi.dialog.put(dialog);
        this.node.dispatchEvent( new cc.Event.EventCustom("close", true) );
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

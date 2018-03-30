"use strict";
cc._RF.push(module, '214d0MHSC5DcoE4sSZlfyz2', 'BeiMiRoomOption');
// resources/script/components/BeiMiRoomOption.js

"use strict";

var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
        // foo: {
        //     // ATTRIBUTES:
        //     default: null,        // The default value will be used only when the component attaching
        //                           // to a node for the first time
        //     type: cc.SpriteFrame, // optional, default is typeof default
        //     serializable: true,   // optional, default is true
        // },
        // bar: {
        //     get () {
        //         return this._bar;
        //     },
        //     set (value) {
        //         this._bar = value;
        //     }
        // },
        playway: {
            default: null,
            type: cc.Node
        }
    },

    // LIFE-CYCLE CALLBACKS:

    onLoad: function onLoad() {},

    onClick: function onClick() {
        if (this.playway != null) {
            var script = this.playway.getComponent("RoomPlayway");
            var roomplayway = cc.instantiate(script.roomoption);
            cc.beimi.openwin = roomplayway;
            cc.beimi.openwin.parent = this.root();
            var roomoption = roomplayway.getComponent("RoomOption");
            if (roomoption != null) {
                roomoption.init(script.data);
            }
        }
    }

    // update (dt) {},
});

cc._RF.pop();
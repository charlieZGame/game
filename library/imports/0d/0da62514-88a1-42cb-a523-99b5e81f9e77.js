"use strict";
cc._RF.push(module, '0da62UUiKFCy6UjmbXoH553', 'PlayPoker');
// module/game/dizhu/script/PlayPoker.js

"use strict";

cc.Class({
    extends: cc.Component,

    properties: {
        // foo: {
        //    default: null,      // The default value will be used only when the component attaching
        //                           to a node for the first time
        //    url: cc.Texture2D,  // optional, default is typeof default
        //    serializable: true, // optional, default is true
        //    visible: true,      // optional, default is true
        //    displayName: 'Foo', // optional
        //    readonly: false,    // optional, default is false
        // },
        // ...
        posy: cc.Integer,
        card: {
            default: null,
            type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function onLoad() {
        this.posy = this.card.y;
    },
    takecard: function takecard(event) {
        var beiMiCard = event.target.parent.getComponent("BeiMiCard");
        if (beiMiCard.game != null) {
            if (event.target.y == this.posy) {
                event.target.y = event.target.y + 30;
                beiMiCard.selected = true;
            } else {
                event.target.y = event.target.y - 30;
                beiMiCard.selected = false;
            }
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

cc._RF.pop();
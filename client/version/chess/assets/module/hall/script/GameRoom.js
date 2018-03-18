var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

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
       cc.beimi.audio.playUiSound();
        if(this.roomidDialog){
            cc.beimi.openwin = cc.instantiate(this.roomidDialog) ;
            cc.beimi.openwin.parent = this.root();
        }
    },

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

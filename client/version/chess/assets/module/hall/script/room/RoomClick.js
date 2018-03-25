cc.Class({
    extends: cc.Component,

    properties: {

    },
    onClick:function(){
        this.node.dispatchEvent( new cc.Event.EventCustom("checkbox", true) );
    },
    onCreateRoom:function(event , data){
        this.node.dispatchEvent( new cc.Event.EventCustom(data, true) );
    },

    onRadioClick:function(){
        this.node.dispatchEvent( new cc.Event.EventCustom("radio", true) );
    },
});

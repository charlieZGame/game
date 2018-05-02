cc.Class({
    extends: cc.Component,

    properties: {
      
    },

    // use this for initialization
    onLoad: function () {

    },
    /**
     * 结算页面上的 背景的 点击事件，主要是用于事件拦截，禁用冒泡
     * @param event
     */
    onBGClick:function(event){
        event.stopPropagation();
    },
    /**
     * 结算页面上的关闭按钮 的 点击事件 , 关闭按钮 和 继续按钮 功能是一样的，都是继续游戏
     */
    onCloseClick:function(){
        /**
         * 发射事件到 上一级 处理
         */
        this.node.dispatchEvent( new cc.Event.EventCustom("close", true) );
    },
    /**
     * 结算页面上的关闭按钮 的 点击事件 , 关闭按钮 和 继续按钮 功能是一样的，都是继续游戏
     */
    onBeginClick:function(){
        /**
         * 发射事件到 上一级 处理
         */
        this.node.dispatchEvent( new cc.Event.EventCustom("begin", true) );
    },

    onOverClick:function(){
        /**
         * 发射事件到 上一级 处理
         */
        this.node.dispatchEvent( new cc.Event.EventCustom("over", true) );
    }

});

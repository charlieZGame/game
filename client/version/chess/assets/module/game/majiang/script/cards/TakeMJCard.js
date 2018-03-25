cc.Class({
    extends: cc.Component,
    properties: {
        target:{
            default:null ,
            type : cc.Node
        }
    },

    onLoad: function () {
        this.clickstate = false  ;
    },

    onClick:function(){
        let handCards = this.target.getComponent("HandCards")
        let self = this ;
        if(this.clickstate == true){
            //出牌
            this.node.dispatchEvent( new cc.Event.EventCustom('takecard', true) );
        }else{
            if(handCards.take == true){
                handCards.take = false ;
                this.target.y = this.target.y - 30 ;
            }else{
                handCards.take = true;
                this.target.y = this.target.y + 30 ;
            }
            this.clickstate = true  ;
            setTimeout(function(){  //双击算法
                self.clickstate = false ;
            } , 500) ;
        }
    }
  
});

cc.Class({
    extends: cc.Component,

    properties: {
        playway: {
            default: null,
            type: cc.Prefab
        },
    },

    // use this for initialization
    onLoad: function () {
        this.playwaypool = new cc.NodePool();
        for(var i=0 ; i<5 ; i++){ //最大玩法数量不能超过20种
            this.playwaypool.put(cc.instantiate(this.playway));
        }
        this.playwayarray = new Array();
    },
    init:function(){
        /**
         * 加载预制的 玩法
         */
        var gametype = cc.beimi.game.type(data);
        if(gametype!=null){
            for(var inx =0 ; inx < gametype.playways.length ; inx++){
                /**
                 * 此处需要做判断，检查 对象池有足够的对象可以使用
                 */
                console.log("获取playways---》",gametype.playways[inx].name);
               // if(gametype.playways[inx].name.contains("斗地主")){
               //     return
               //   }
                var playway = this.playwaypool.get();
                var script = playway.getComponent("Playway") ;
                script.init(gametype.playways[inx]);
                playway.parent = this.content ;
                this.playwayarray.push(playway) ;
            }
        }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

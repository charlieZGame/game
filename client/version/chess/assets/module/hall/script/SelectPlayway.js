var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
        first: {
            default: null,
            type: cc.Node
        },
        second: {
            default: null,
            type: cc.Node
        },
        gamepoint:{
            default: null,
            type: cc.Node
        },
        title:{
            default: null,
            type: cc.Node
        },
        global: {
            default: null,
            type: cc.Node
        },
        playway: {
            default: null,
            type: cc.Prefab
        },
        content: {
            default: null,
            type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
      let self = this;
      this.first.getChildByName("rank").active = false;
      this.init(this);
      cc.beimi.socket.on("searchHaveNotFinishGame", function(result) {
        /**
          * 获取是否有正在游戏的状态
          */
        var data = self.parse(result);
        console.error("searchHaveNotFinishGame==0000==>",data);
        if (data.type==1||data.type=="1") {
            cc.beimi.isHasEnterRoom=1
        }else if (data.type==2||data.type=="2") {
            cc.beimi.isHasEnterRoom=2;
            cc.beimi.extparams = data;
            self.init(self);
        }
      });

    },

   init:function(context){
     if(cc.beimi != null && cc.beimi.user != null){
         context.disMenu("first") ;
         context.playwaypool = new cc.NodePool();
         for(var i=0 ; i<20 ; i++){ //最大玩法数量不能超过20种
             context.playwaypool.put(cc.instantiate(context.playway));
         }
         context.playwayarray = new Array();
         if(context.gamepoint && cc.beimi!=null && cc.beimi.games !=null){
             for(var inx=0 ; inx < context.gamepoint.children.length ; inx++){
                 let name = context.gamepoint.children[inx].name ;
                 var gameenable = true ;
                 console.log("cc.beimi.games",cc.beimi.games);
                 // for(var i=0 ; i<cc.beimi.games.length ; i++){
                 //     var gamemodel = cc.beimi.games[i] ;
                 //     for(var j=0 ; j<gamemodel.types.length ; j++){
                 //         let gametype = gamemodel.types[j] ;
                 //         if(gametype.code == name){
                 //             gameenable = true ; break ;
                 //         }
                 //     }
                 //     if(gameenable == true){break ;}
                 // }
                 if(gameenable == true){
                   console.log("===========this.gamepoint.children[inx].name============",cc.beimi.isHasEnterRoom,context.gamepoint.children[inx]);
                     context.gamepoint.children[inx].active = true;
                     let self=context;
                     let createroomNode = context.gamepoint.children[inx];
                     if (cc.beimi.isHasEnterRoom==2&&context.gamepoint.children[inx].name=="createroom") {
                       cc.loader.loadRes("images/img/create_unable", cc.SpriteFrame, function(error, spriteFrame) {
                          console.log("createroomNode=====>",createroomNode);
                         createroomNode._components[0].spriteFrame = spriteFrame;
                       });
                     }
                 }else{
                     context.gamepoint.children[inx].active = false ;
                 }
             }
         }
     }
   },

    onClick:function(event, data){
       console.log("data==>",data);

       if (data=="createroom"&&cc.beimi.isHasEnterRoom==2) {
         return
       }
        cc.beimi.audio.playUiSound();
        this.disMenu("second") ;
        var girlAni = this.global.getComponent("DefaultHallDataBind");
        girlAni.playToLeft();
        this._secondAnimCtrl = this.second.getComponent(cc.Animation);
        this._secondAnimCtrl.play("playway_display");

        if(this.title){
            for(var inx = 0 ; inx<this.title.children.length ; inx++){
                if(this.title.children[inx].name == data){
                    this.title.children[inx].active = true ;
                }else{
                    this.title.children[inx].active = false ;
                }
            }
        }
        /**
         * 加载预制的 玩法
         */
        var gametype = cc.beimi.game.type(data);
        if(gametype!=null){
            for(var inx =0 ; inx < gametype.playways.length ; inx++){
                /**
                 * 此处需要做判断，检查 对象池有足够的对象可以使用
                 */
                console.log("获取点击的game---》",JSON.stringify(gametype.playways[inx]));
                var playway = this.playwaypool.get();
                var script = playway.getComponent("Playway") ;
                if(script == null){
                    script = playway.getComponent("RoomPlayway") ;
                }
                script.init(gametype.playways[inx]);
                playway.parent = this.content ;
                this.playwayarray.push(playway) ;
            }
        }
    },
    onRoomClick:function(){
        cc.beimi.audio.playUiSound();
        this.disMenu("third") ;
        this._menuDisplay = this.third.getComponent(cc.Animation);
        this._menuDisplay.play("play_room_display");
    },
    onSecondBack:function(event ,data){
        cc.beimi.audio.playUiSound();
        var girlAni = this.global.getComponent("DefaultHallDataBind");
        girlAni.playToRight();
        this.collect();
        this.disMenu("first") ;
    },
    onThirddBack:function(event ,data){
        cc.beimi.audio.playUiSound();
        this.disMenu("first") ;
    },
    collect:function(){
        cc.beimi.audio.playUiSound();
        for(var inx =0 ; inx < this.playwayarray.length ; inx++){
            this.playwaypool.put(this.playwayarray[inx]);
        }
        this.playwayarray.splice(0 ,this.playwayarray.length );
    },
    disMenu:function(order){
        cc.beimi.audio.playUiSound();
        if(order == 'first'){
            this.first.active = true ;
            this.second.active = false ;
            if(this.third != null){
                this.third.active = false ;
            }
        }else if(order == 'second'){
            this.first.active = false;
            this.second.active = true;
            if(this.third != null){
                this.third.active = false ;
            }
        }else if(order == 'third'){
            this.first.active = false;
            this.second.active = false;
            if(this.third != null){
                this.third.active = true ;
            }
        }
    },

});

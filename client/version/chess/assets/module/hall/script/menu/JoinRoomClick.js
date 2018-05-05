var beiMiCommon = require("BeiMiCommon");

cc.Class({
    extends: beiMiCommon,
    properties: {
        numdata: {
            default:null,
            type:cc.Node
        }
    },

    // LIFE-CYCLE CALLBACKS:

    onLoad : function () {
        this.roomid = new Array() ;
    },
    onClick:function(event,data){
        cc.beimi.audio.playUiSound();
        if(this.roomid.length < 6){
            this.roomid.push(data);
            this.disRoomId();
        }
        if(this.roomid.length == 6){
            this.closeOpenWin();
            /**
             * 查询服务端的房间号码 ， 然后通过房间号码找到对应的房间游戏类型，玩法等信息
             */
            if(this.ready()){
                let socket = this.socket();
                /**
                 * 发送 room请求
                 */
                var param = {
                    token:cc.beimi.authorization,
                    roomid:this.roomid.join(""),
                    orgi:cc.beimi.user.orgi,
                    userid:cc.beimi.user.id
                } ;
                socket.emit("searchroom" , JSON.stringify(param));
                console.log("查询房间参数-->",JSON.stringify(param));
                this.registercallback(this.roomCallBack);
            }
            this.loadding();
        }
    },
    roomCallBack:function(result , self){
        console.log("查询加入房间result-->",result);
        var data = self.parse(result) ;
        if(data.result == "ok"){
          var extparams = data;
          extparams.gametype = data.code;
          extparams.playway = data.id;
          extparams.gamemodel = "room";

            /**
             * 发送创建房间开始游戏的请求
             */
           console.log("查询有该房间号，发送创建房间开始游戏的请求-->",JSON.stringify(extparams));
            self.preload(extparams , self) ;
        }else if(data.result == "notexist"){
            self.alert("房间号不存在。");
        }else if(data.result == "full"){
            self.alert("房间已满员。");
        }
    },
    onDeleteClick:function(){
        cc.beimi.audio.playUiSound();
        this.roomid.splice(this.roomid.length-1 , this.roomid.length) ;
        this.disRoomId();
    },
    onCleanClick:function(){
        cc.beimi.audio.playUiSound();
        this.roomid.splice(0 , this.roomid.length) ;
        this.disRoomId();
    },
    disRoomId:function(){
        cc.beimi.audio.playUiSound();
        let children = this.numdata.children ;
        for(var inx = 0 ; inx < 6 ; inx ++){
            if(inx < this.roomid.length){
                children[inx].children[0].getComponent(cc.Label).string = this.roomid[inx] ;
            }else{
                children[inx].children[0].getComponent(cc.Label).string = "" ;
            }
        }
    }
    // update (dt) {},
});

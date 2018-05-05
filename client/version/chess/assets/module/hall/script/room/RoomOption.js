var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,
    properties: {
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
        memo:{
            default:null ,
            type : cc.Label
        },
        optionsnode:{
            default:null ,
            type : cc.Node
        },
        roomtitle:{
            default:null ,
            type : cc.Label
        },
        optiongroup:{
            default:null ,
            type : cc.Prefab
        },
        optiongroupitem:{
            default:null ,
            type : cc.Prefab
        },
        memonode:{
            default:null ,
            type : cc.Node
        },
        createroom:{
            default:null ,
            type : cc.Node
        },

        daikaiNode:{
          default:null ,
          type : cc.Node
        },

        middlecreatebt:{
          default:null ,
          type : cc.Node
        },

        rightcreatebt:{
          default:null ,
          type : cc.Node
        },

        freeopt:{
            default:null ,
            type : cc.Node
        },

        freedaikaiNode:{
          default:null ,
          type : cc.Node
        },

        freemiddlecreatebt:{
          default:null ,
          type : cc.Node
        },

        freerightcreatebt:{
          default:null ,
          type : cc.Node
        },
    },

    // use this for initialization
    onLoad: function () {
        let self = this ;
        this.group = new Array();
        //创建房间
        this.node.on('createroom', function (event) {
            cc.beimi.audio.playUiSound();
            if (!cc.beimi.isConnect) {
              self.alert("网络繁忙，请稍后再试");
              if(cc.beimi.authorization != null) {
                self.connect();
              }
              return
            }
            /**
             * 把参数 汇总一下， 然后转JSON以后序列化成字符串，发送 创建房间的请求
             */
            var extparams = {} ;
            let values = new Array();
            for(var inx=0 ; inx<self.group.length ; inx++){
                let groupitem = self.group[inx] ;
                let value = "" ;
                for(var j=0 ; j<groupitem.groupoptions.length ; j++){
                    let option = groupitem.groupoptions[j] ;
                    if(option.checked == true){
                        if(value != ""){
                            value = value + "," ;
                        }
                        value = value + option.item.value ;
                    }
                }
                extparams[groupitem.data.code] = value ;
            }
            /**
             * 藏到全局变量里去，进入场景后使用，然后把这个参数置空
             * @type {{}}
             */
            extparams.gametype = self.data.code ;
            extparams.playway = self.data.id;
            extparams.gamemodel = "room" ;
            /**
             * 发送创建房间开始游戏的请求
             */
            event.stopPropagation() ;
            console.log("发送创建房间开始游戏的请求======socket=preload======>",JSON.stringify(extparams));
            self.preload(extparams , self) ;
        });
        //代开房间
        this.node.on('daikai', function (event) {
            cc.beimi.audio.playUiSound();
            cc.beimi.daikaiRoom = true;
            cc.beimi.extparams = null;
            if (!cc.beimi.isConnect) {
              self.alert("网络繁忙，请稍后再试");
              if(cc.beimi.authorization != null) {
                self.connect();
              }
              return
            }
            /**
             * 把参数 汇总一下， 然后转JSON以后序列化成字符串，发送 创建房间的请求
             */
            var extparams = {} ;
            let values = new Array();
            for(var inx=0 ; inx<self.group.length ; inx++){
                let groupitem = self.group[inx] ;
                let value = "" ;
                for(var j=0 ; j<groupitem.groupoptions.length ; j++){
                    let option = groupitem.groupoptions[j] ;
                    if(option.checked == true){
                        if(value != ""){
                            value = value + "," ;
                        }
                        value = value + option.item.value ;
                    }
                }
                extparams[groupitem.data.code] = value ;
            }
            /**
             * 藏到全局变量里去，进入场景后使用，然后把这个参数置空
             * @type {{}}
             */
            extparams.gametype = self.data.code ;
            extparams.playway = self.data.id;
            extparams.gamemodel = "room" ;
            /**
             * 发送创建房间开始游戏的请求
             */
            event.stopPropagation() ;
            console.log("发送代开房间开始游戏的请求======socket=preload======>",JSON.stringify(extparams));
            var param = {
              token: cc.beimi.authorization,
              playway: extparams.playway,
              orgi: cc.beimi.user.orgi,
              extparams: extparams
            };
            cc.beimi.socket.on("cardCheck", function(result) {
              var resultObj = self.parse(result);
              //房卡不够
              console.log("resultObj==cardCheck=>",resultObj.status);
              if(resultObj.status==-1 && cc.beimi.daikaiRoom ){
                 cc.beimi.daikaiRoom = false;
                 self.closeOpenWin();
                 self.alert(resultObj.msg || '房间创建失败，请联系管理员');
              }else if(cc.beimi.daikaiRoom){
                cc.beimi.daikaiRoom = false;
                var param = {
                  token: cc.beimi.authorization,
                  playway: extparams.playway,
                  orgi: cc.beimi.user.orgi,
                  extparams: extparams
                };
                cc.beimi.socket.emit("proxyCreateRoom", JSON.stringify(param));
              }
            });
            cc.beimi.socket.emit("cardCheck", JSON.stringify(param));
        });
    },

    init:function(playway){
        this.data = playway ;
        if(this.memo != null && playway.memo!=null && playway.memo!=""){
            this.memonode.active = true ;
            this.memo.string = playway.memo ;
        }else if(this.memonode!=null){
            this.memonode.active = false ;
        }
        if(playway.free == true){
            this.freeopt.active = true;
            this.createroom.active = false ;
            if (cc.beimi.user.usercategory==2||cc.beimi.user.usercategory=="2") {
              this.freedaikaiNode.active = true;
              this.freemiddlecreatebt.active = false;
              this.freerightcreatebt.active = true;
            }else {
              this.freedaikaiNode.active = false;
              this.freemiddlecreatebt.active = true;
              this.freerightcreatebt.active = false;
            }
        }else{
            console.log("cc.beimi.user===>",cc.beimi.user);
            this.freeopt.active = false;
            this.createroom.active = true ;
            if(cc.beimi.user.usercategory==2||cc.beimi.user.usercategory=="2") {
              this.daikaiNode.active = true;
              this.middlecreatebt.active = false;
              this.rightcreatebt.active = true;
            }else  if(cc.beimi.user.usercategory==1||cc.beimi.user.usercategory=="1"||cc.beimi.user.usercategory==3||cc.beimi.user.usercategory=="3") {
              this.daikaiNode.active = false;
              this.middlecreatebt.active = true;
              this.rightcreatebt.active = false;
            }else {
              this.daikaiNode.active = false;
              this.middlecreatebt.active = true;
              this.rightcreatebt.active = false;
            }
        }

       console.log("playway.code=========>",playway.code);
        if (playway.code=="majiang") {
          this.roomtitle.string ="涞源玩法"
        }else if (playway.code=="koudajiang") {
          this.roomtitle.string ="扣大将"
        }
        // if(playway.roomtitle!=null && playway.roomtitle!=""){
        //     console.log("playway.roomtitle=========>",playway.roomtitle);
        //     if (true) {
        //
        //     }
        //     let frame = this.atlas.getSpriteFrame(playway.roomtitle);
        //     if(frame!=null){
        //         this.roomtitle.getComponent(cc.Sprite).spriteFrame = frame ;
        //     }
        // }
        if(this.optiongroup!=null && playway.groups!=null){
            for(var inx = 0 ; inx < playway.groups.length ; inx++){
                let group = cc.instantiate(this.optiongroup) ;
                let playWayGroup = group.getComponent("PlaywayGroup") ;
                playWayGroup.init(playway.groups[inx] , this.optiongroupitem , playway.items) ;
                this.group.push(playWayGroup);
                group.parent = this.optionsnode ;
            }
        }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

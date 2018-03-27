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
            type : cc.Node
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
            // /**
            //  * 把参数 汇总一下， 然后转JSON以后序列化成字符串，发送 创建房间的请求
            //  */
            // var extparams = {} ;
            // let values = new Array();
            // for(var inx=0 ; inx<self.group.length ; inx++){
            //     let groupitem = self.group[inx] ;
            //     let value = "" ;
            //     for(var j=0 ; j<groupitem.groupoptions.length ; j++){
            //         let option = groupitem.groupoptions[j] ;
            //         if(option.checked == true){
            //             if(value != ""){
            //                 value = value + "," ;
            //             }
            //             value = value + option.item.value ;
            //         }
            //     }
            //     extparams[groupitem.data.code] = value ;
            // }
            // /**
            //  * 藏到全局变量里去，进入场景后使用，然后把这个参数置空
            //  * @type {{}}
            //  */
            // extparams.gametype = self.data.code ;
            // extparams.playway = self.data.id;
            // extparams.gamemodel = "room" ;
            // /**
            //  * 发送创建房间开始游戏的请求
            //  */
            // event.stopPropagation() ;
            // console.log("发送创建房间开始游戏的请求======socket=preload======>",JSON.stringify(extparams));
            // self.preload(extparams , self) ;
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
            if (true) {
              this.freedaikaiNode.active = true;
              this.freemiddlecreatebt.active = false;
              this.freerightcreatebt.active = true;
            }else {
              this.freedaikaiNode.active = false;
              this.freemiddlecreatebt.active = true;
              this.freerightcreatebt.active = false;
            }
        }else{
            this.freeopt.active = false;
            this.createroom.active = true ;
            if (true) {
              this.daikaiNode.active = true;
              this.middlecreatebt.active = false;
              this.rightcreatebt.active = true;
            }else {
              this.daikaiNode.active = false;
              this.middlecreatebt.active = true;
              this.rightcreatebt.active = false;
            }
        }
        if(playway.roomtitle!=null && playway.roomtitle!=""){
            let frame = this.atlas.getSpriteFrame(playway.roomtitle);
            if(frame!=null){
                this.roomtitle.getComponent(cc.Sprite).spriteFrame = frame ;
            }
        }
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

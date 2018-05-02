var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

  properties: {
        username:{
            default:null ,
            type:cc.Label
        },
        useravatar: {
          default: null,
          type: cc.Sprite
        },
        goldcoins:{
            default:null ,
            type:cc.Label
        },
        selected:{
            default:null ,
            type : cc.Node
        },
        creator:{
            default:null ,
            type : cc.Node
        },
        selectcards:{
            default:null ,
            type : cc.Node
        },
        selectcolor:{
            default:null ,
            type : cc.Node
        },

       chatbgld:{
            default:null ,
            type : cc.Node
        },

        chatmessageld:{
             default:null ,
             type : cc.Label
         },

        chatbglt:{
             default:null ,
             type : cc.Node
         },

         chatmessagelt:{
              default:null ,
              type : cc.Label
          },

       chatbgrd:{
              default:null ,
              type : cc.Node
          },

          chatmessagerd:{
               default:null ,
               type : cc.Label
           },
    },

    // use this for initialization
    onLoad: function () {
        this.selected.active = false ;
        this.creator.active = false ;

        this.chatbgld.active = false;

        this.chatbglt.active = false;

        this.chatbgrd.active = false;

    },
    init:function(playerdata , inx , tablepos){
        this.data = playerdata ;    //存放玩家数据
        this.tablepos = tablepos ;
        if(inx == 0){
            this.selectcards.parent.x = this.selectcards.parent.x * -1 ;
        }else if(inx == 1){
            this.selectcards.parent.x = this.selectcards.parent.x * -1 ;
        }

        if(playerdata.nickname == null) {
            this.username.string = playerdata.username;
        } else {
            this.username.string = playerdata.nickname;
            if (playerdata.photo) {
              cc.loader.load(playerdata.photo, function(error, res) {
                this.useravatar.spriteFrame = new cc.SpriteFrame(res);
              }.bind(this));
            }
        }
        this.goldcoins.string = playerdata.goldcoins + " " + playerdata.playerlevel;
    },
    banker:function(){
        this.creator.active = true ;
    },
    selecting:function(){
        if(this.data.id != cc.beimi.user.id){
            this.selectcards.active = true ;
            let ani = this.selectcolor.getComponent(cc.Animation);
            this.animState = ani.play("majiang_select") ;
            // 设置循环模式为 Loop
            this.animState.wrapMode = cc.WrapMode.Loop;
            this.animState.repeatCount = 20; //最大不超过 20次
        }
    },
    selectresult:function(data){
        for(var i = 0 ; i < this.selected.children.length ; i++){
            this.selected.children[i].active = false ;
            if(this.selected.children[i].name == data.color){
                this.selected.children[i].active = true;
            }
        }
        this.selected.active = true ;
        if(this.data.id != cc.beimi.user.id) {
            if (this.animState != null) {
                this.animState.stop("majiang_select");
            }
        }
    },

    setChatMessage:function(sound){
         let message = '';
         if (sound=="sound1") {
           message ='快点吧，我等的花都谢啦';
           cc.beimi.audio.playCharSound(1);
         }else if (sound=="sound2") {
            message ='又断线了，网络怎么这么差呀';
             cc.beimi.audio.playCharSound(2);
         }else if (sound=="sound3") {
           message ='不要走，决战到天亮';
            cc.beimi.audio.playCharSound(3);
         }else if (sound=="sound4") {
           message ='你的牌打得太好啦';
             cc.beimi.audio.playCharSound(4);
         }else if (sound=="sound5") {
           message ='你是妹妹还是哥哥';
             cc.beimi.audio.playCharSound(5);
         }else if (sound=="sound6") {
           message ='和你合作真是太愉快了';
            cc.beimi.audio.playCharSound(6);
         }else if (sound=="sound7") {
           message ='大家好 很高兴见到各位';
             cc.beimi.audio.playCharSound(7);
         }else if (sound=="sound8") {
           message ='真是不好意思，我得离开一会';
            cc.beimi.audio.playCharSound(8);
         }else if (sound=="sound9") {
           message ='不要吵啦，不要吵啦，专心打游戏吧';
            cc.beimi.audio.playCharSound(9);
         }

         if (this.tablepos == "left") {
           this.chatbgld.active = true;
           this.chatmessageld.string = message;
           this.chatbglt.active = false;
           this.chatbgrd.active = false;
         }else if (this.tablepos == "right") {
           this.chatbgld.active = false;
           this.chatmessagerd.string = message;
           this.chatbglt.active = false;
           this.chatbgrd.active = true;
         }else if (this.tablepos == "top") {
           this.chatbgld.active = false;
           this.chatbglt.active = true;
           this.chatmessagelt.string = message;
           this.chatbgrd.active = false;
         }else {
           this.chatbgld.active = true;
           this.chatmessageld.string = message;
           this.chatbglt.active = false;
           this.chatbgrd.active = false;
         }
    },

    hideChatMessage(){
      this.chatbgld.active = false;
      this.chatbglt.active = false;
      this.chatbgrd.active = false;
    },

    clean:function(){
        this.creator.active = false ;
        for(var i = 0 ; i < this.selected.children.length ; i++){
            this.selected.children[i].active = false ;
        }
    }

});

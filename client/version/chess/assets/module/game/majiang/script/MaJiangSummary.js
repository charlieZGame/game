var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,
    properties: {
        workitem:{
            default: null,
            type: cc.Node
        },

        nickname1:{
          default: null,
          type: cc.Label
        },

        nickname2:{
          default: null,
          type: cc.Label
        },

        nickname3:{
          default: null,
          type: cc.Label
        },

        nickname4:{
          default: null,
          type: cc.Label
        },

        id1:{
          default: null,
          type: cc.Label
        },

        id2:{
          default: null,
          type: cc.Label
        },

        id3:{
          default: null,
          type: cc.Label
        },

        id4:{
          default: null,
          type: cc.Label
        },

        avatar1:{
          default: null,
          type: cc.Sprite
        },

        avatar2:{
          default: null,
          type: cc.Sprite
        },

        avatar3:{
          default: null,
          type: cc.Sprite
        },

        avatar4:{
          default: null,
          type: cc.Sprite
        },

        score1:{
          default: null,
          type: cc.Label
        },

        score2:{
          default: null,
          type: cc.Label
        },

        score3:{
          default: null,
          type: cc.Label
        },

        score4:{
          default: null,
          type: cc.Label
        },

        usecards1:{
          default: null,
          type: cc.Label
        },

        usecards1:{
          default: null,
          type: cc.Label
        },


        usecards2:{
          default: null,
          type: cc.Label
        },


        usecards3:{
          default: null,
          type: cc.Label
        },

        usecards4:{
          default: null,
          type: cc.Label
        },

        ju1:{
          default: null,
          type: cc.Label
        },

        ju2:{
          default: null,
          type: cc.Label
        },

        ju3:{
          default: null,
          type: cc.Label
        },

        ju4:{
          default: null,
          type: cc.Label
        },

    },


    onLoad: function () {
        let self = this ;

        this.workitem.on("over",function(event){
            if(self.context !=null){
                self.context.summarytotalpage.destroy();
                cc.beimi.joinroom=false;
                self.scene(cc.beimi.gametype, self);
                cc.beimi.isLeaveroom= true;
            }
            event.stopPropagation();
        });

        /**
         * SummaryClick发射的事件，方便统一处理 / 开始
         */
        this.workitem.on("close",function(event){
            if(self.context !=null){
                self.context.summarytotalpage.destroy();
                self.scene(cc.beimi.gametype, self);
                cc.beimi.joinroom=false;
                cc.beimi.isLeaveroom= false;
            }
            event.stopPropagation();
        });
    },

    create:function(context , data){
      console.log("data-----1111----->",data);
        this.context = context ;
        let self = this;
        var index = 0 ;
        console.log("data--------2222-->",data.data);
        if (data.data&&data.data.roomIds&&data.data.roomIds.length>0) {
          let players = data.data[data.data.roomIds[0]];
          for (var i = 0; i < players.length; i++) {
             if (players[i].userNo==cc.beimi.user.username) {
               this.nickname1.string = players[i].nickname;
               this.id1.string ="ID："+  players[i].userNo;
               this.ju1.string =  players[i].num+'局';
               this.usecards1.string =  players[i].useCards;
               this.score1.string = "总成绩："+ players[i].score;

               if (players[i].photo) {
                 cc.loader.load(players[i].photo, function(error, res) {
                   self.avatar1.spriteFrame = new cc.SpriteFrame(res);
                 }.bind(self));
               }

               players.splice(i,1);
             }
           }

           this.nickname2.string = players[0].nickname;
           this.id2.string =  "ID："+  players[0].userNo;
           this.ju2.string =  players[0].num+'局';
           this.usecards2.string =  players[0].useCards;
           this.score2.string =  "总成绩："+ players[0].score;
           if (players[0].photo) {
             cc.loader.load(players[0].photo, function(error, res) {
               self.avatar2.spriteFrame = new cc.SpriteFrame(res);
             }.bind(self));
           }

           this.nickname3.string = players[1].nickname;
           this.id3.string =  "ID："+  players[1].userNo;
           this.ju3.string =  players[1].num+'局';
           this.usecards3.string =  players[1].useCards;
           this.score3.string =  "总成绩："+ players[1].score;

           if (players[1].photo) {
             cc.loader.load(players[1].photo, function(error, res) {
               self.avatar3.spriteFrame = new cc.SpriteFrame(res);
             }.bind(self));
           }

           this.nickname4.string = players[2].nickname;
           this.id4.string =  "ID："+  players[2].userNo;
           this.ju4.string =  players[2].num+'局';
           this.usecards4.string =  players[2].useCards;
           this.score4.string = "总成绩："+  players[2].score;

            if (players[2].photo) {
              cc.loader.load(players[2].photo, function(error, res) {
                self.avatar4.spriteFrame = new cc.SpriteFrame(res);
              }.bind(self));
            }


          }


        }
});

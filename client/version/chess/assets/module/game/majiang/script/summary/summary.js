cc.Class({
    extends: cc.Component,

    properties: {
        workitem:{
            default: null,
            type: cc.Node
        },

        summaryitem: {
          default: null,
          type: cc.Prefab
        },

        content_panel:{
          default: null,
          type: cc.Node
        },

        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },

        win:{
          default: null,
          type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        let self = this ;
        /**
         * SummaryClick发射的事件，方便统一处理 / 开始
         */
        this.workitem.on("begin",function(event){
            if(self.context !=null){
                self.context.summarypage.destroy();
                self.context.restart();
            }
            event.stopPropagation();
        });
        /**
         * SummaryClick发射的事件，方便统一处理 / 开始
         */
        this.workitem.on("close",function(event){
            if(self.context !=null){
                self.context.summarypage.destroy();
            }
            event.stopPropagation();
        });
    },

    create:function(context , data,laizicards){
        this.context = context ;
        let cardframe;
        let isHasWin=false;
        for (var i = 0; i < data.players.length; i++) {
          if (data.players[i].win) {
            isHasWin=true;
          }
          cardframe = this.atlas.getSpriteFrame('结算-result_lose');
            console.log("没赢了----");
          if (data.players[i].userid==cc.beimi.user.id&&data.players[i].win) {
              console.log("赢了----");
              cardframe = this.atlas.getSpriteFrame('结算-result_win');
              break;
          }

        }
       console.log("isHasWin----",isHasWin);
        if (!isHasWin) {
          cardframe = this.atlas.getSpriteFrame('结算-result_draw');
        }
        console.log("cardframe---->",cardframe);
        this.win.getComponent(cc.Sprite).spriteFrame = cardframe;

        var index = 0 ;
        this.summaryitempool = new cc.NodePool();
        for (var i = 0; i < 4; i++) {
          this.summaryitempool.put(cc.instantiate(this.summaryitem));
        }

        for (var i =0; i < data.players.length ; i++) {
          let temp = this.summaryitempool.get();
          let temp_script = temp.getComponent("sunmmaryitem");

          // let tempdata= {
          //   handcards:[1,9,10,7,5,6,8,8,8],
          //   gangcards:[90],
          //   pengcards:[-5],
          //   laizicards:[1,2,10],
          //   dizhu:true,
          //   username:"花花",
          //   score:"500"
          // };
          temp_script.init(data.players[i],laizicards); //
          temp.parent = this.content_panel;
        }
    },

    onDestroy: function() {
      for (var i = 0; i < this.summaryitempool.size(); i++) {
        this.summaryitempool.get().destroy();
      }
    }
});

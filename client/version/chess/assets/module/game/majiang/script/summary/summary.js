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

    create:function(context , data){
        this.context = context ;
        var index = 0 ;
        this.summaryitempool = new cc.NodePool();
        for (var i = 0; i < 4; i++) {
          this.summaryitempool.put(cc.instantiate(this.summaryitem));
        }

        for (var i =0; i < data.players.length ; i++) {
          let temp = this.summaryitempool.get();
          let temp_script = temp.getComponent("sunmmaryitem");

          let tempdata= {
            handcards:[1,9,10,7,5,6,8,8,8],
            deskcards:[90,60,88,99],
            laizicards:[1,2,10],
            banker:true,
            nickname:"花花",
            score:"500"
          };
          temp_script.init(tempdata); //data.players[i]
          temp.parent = this.content_panel;
        }
    }
});

var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

  properties: {
    scoreDetailItemPrefab: {
      default: null,
      type: cc.Prefab
    },

    scoreScrollview: {
      default: null,
      type: cc.ScrollView
    },

    player1Label: {
      default: null,
      type: cc.Label
    },

    player2Label: {
      default: null,
      type: cc.Label
    },

    player3Label: {
      default: null,
      type: cc.Label
    },

    player4Label: {
      default: null,
      type: cc.Label
    },

  },

  onLoad: function() {},

  init(roomUuid,roomId) {
    console.error("detail====>",roomId);
    let self = this;
    self.items=[];
    let socket = this.socket();
    var param = {
      token: cc.beimi.authorization,
      orgi: cc.beimi.user.orgi,
      startPage:"1",
      pageSize:"99",
      roomUuid:roomUuid+"",
      roomId:roomId+""
    };
      console.error("JSON.stringify(param)====>",JSON.stringify(param));
    socket.emit("getPlayhistoryDetail", JSON.stringify(param));
    socket.on("getPlayhistoryDetail", function(resultStrig) {
      console.log("getPlayhistory==房间战绩===>", JSON.stringify(resultStrig));
       let result =  JSON.parse(resultStrig);
      self.selectScore(result,self);
    });

  //   let  result={"data":[{"cardNum":0,"createTime":1524240614000,"id":"8af48d3b62e3b92b0162e3d204060005","isWin":"1","nickname":"杨柳依依1","obj":[{"score":"-4","nickname":"杨柳依依2"},{"score":"6","nickname":"杨柳依依3"},{"score":"-1","nickname":"杨柳依依4"},{"score":"-1","nickname":"杨柳依依0"}],"photo":"http://img.suncity.ink/game/2018/04/9999988.png","roomId":391124,"score":6,"userId":"wx7788992669ok","username":50002007,"yxbj":"1"},
  // {"cardNum":0,"createTime":1524240614000,"id":"8af48d3b62e3b92b0162e3d204060005","isWin":"1","nickname":"杨柳依依1","obj":[{"score":"-4","nickname":"杨柳依依2"},{"score":"6","nickname":"杨柳依依3"},{"score":"-1","nickname":"杨柳依依4"},{"score":"-1","nickname":"杨柳依依0"}],"photo":"http://img.suncity.ink/game/2018/04/9999988.png","roomId":391124,"score":6,"userId":"wx7788992669ok","username":50002007,"yxbj":"1"}],"msg":"ok","returnCode":1}
  //  self.selectScore(result,self);
  },

  selectScore(result,self) {
      console.error("result----->",result);
    if (!result||result.returnCode!=1) {
       return
    }else {
      this.player1Label.string=result.data[0].obj[0].nickname;
      this.player2Label.string=result.data[0].obj[1].nickname;
      this.player3Label.string=result.data[0].obj[2].nickname;
      this.player4Label.string=result.data[0].obj[3].nickname;

      for (let i = 0; i < result.data.length; i++) {
        console.error("result----111->",result.data[i]);
        let item = cc.instantiate(self.scoreDetailItemPrefab);
        self.scoreScrollview.content.addChild(item);
        item.setPosition(0, -item.height * (1.5 + i) - 5 * i+50);
        item.getComponent('ScoreHistoryDetailItem').init(result.data[i], i);
        self.items.push(item);
     }
    }

  },


});

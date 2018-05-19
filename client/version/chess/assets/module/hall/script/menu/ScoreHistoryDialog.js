var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

  properties: {

    scoreScrollNode: {
      default: null,
      type: cc.Node
    },

    scoreScrollview: {
      default: null,
      type: cc.ScrollView
    },

    scoreItemPrefab: {
      default: null,
      type: cc.Prefab
    },

    emptyLabel: {
      default: null,
      type: cc.Node
    }
  },

  onLoad: function() {
    let self = this;
    self.items=[];
    let socket = this.socket();
    var param = {
      token: cc.beimi.authorization,
      orgi: cc.beimi.user.orgi,
      startPage: "1",
      pageSize: "99"
    };
    socket.emit("getPlayhistory", JSON.stringify(param));
    socket.on("getPlayhistory", function(resultStrig) {
      let result =  JSON.parse(resultStrig);
      console.log("====0000====result===",result);
      console.log("====111111====result===",result.returnCode);
      if (!result || result.returnCode != 1 || !result.data || !result.data.roomIds || (result.data.roomIds && result.data.roomIds.length == 0)) {
        console.log("====2222====result===",result.returnCode);
        if (self.emptyLabel) {
        self.emptyLabel.active = true;
         }
      } else {
          if (self.emptyLabel) {
           self.emptyLabel.active = false;
         }
          if (self.scoreScrollNode) {
                  self.scoreScrollNode.active = true;
                    for (let i = 0; i <  result.data.roomIds.length; ++i) { // spawn items, we only need to do this once
                      let item = cc.instantiate(self.scoreItemPrefab);
                      console.log("---self.scoreScrollview------", self.scoreScrollview);
                      self.scoreScrollview.content.addChild(item);
                      item.setPosition(0, -item.height * (0.5 + i) - 30 * (i + 1));
                      let roomid = result.data.roomIds[i];
                      item.getComponent('ScoreHistoryItem').init(result.data[roomid],i);
                      self.items.push(item);
                    }
          }

      }
    });
  },

  selectScore(result, self) {}
});

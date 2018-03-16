var beiMiCommon = require("BeiMiCommon");
cc.Class({
  extends: beiMiCommon,

  properties: {
    leaveDialogprefab: {
      default: null,
      type: cc.Prefab
    }
  },

  // use this for initialization
  onLoad: function() {},
  onBackClick: function() {
    console.log("------要离开");
    if (this.leaveDialogprefab) {
      cc.beimi.openwin = cc.instantiate(this.leaveDialogprefab);
      cc.beimi.openwin.parent = this.root();
    }
  },

    forceLeaveRoom : function() {
      if (this.ready()) {
        let socket = this.socket();
        var param = {
          token: cc.beimi.authorization,
          orgi: cc.beimi.user.orgi,
          userid: cc.beimi.user.id
        };
        socket.emit("forceleaveroom", JSON.stringify(param));
        this.registercallback(this.forceLeaveRoomCallBack);
        console.log("已发送强制退出的请求forceleaveroom", JSON.stringify(param));
      }
    },

    forceLeaveRoomCallBack : function(result, self) {
      console.log("强制退出请求result-->", result);
      var data = self.parse(result);
      if (data.result == "ok") {
        this.closeOpenWin();
        this.scene(cc.beimi.gametype, this);
      } else {
        this.closeOpenWin();
        self.alert("强制退出被拒绝");
      }
    },

    applyLeaveRoom : function() {
      if (this.ready()) {
        let socket = this.socket();
        var param = {
          token: cc.beimi.authorization,
          orgi: cc.beimi.user.orgi,
          userid: cc.beimi.user.id
        };
        socket.emit("applyleaveroom", JSON.stringify(param));
        this.registercallback(this.leaveRoomCallBack);
        console.log("已发送申请退出的请求applyLeaveRoomCallBack", JSON.stringify(param));
      }
    },

    applyLeaveRoomCallBack : function(result, self) {
      console.log("申请退出请求result-->", result);
      var data = self.parse(result);
      if (data.result == "ok") {
        // this.closeOpenWin();
        // this.scene(cc.beimi.gametype, this);
      } else {
        this.closeOpenWin();
        self.alert("申请退出被拒绝");
      }
    }


  // called every frame, uncomment this function to activate update callback
  // update: function (dt) {

  // },
});

var beiMiCommon = require("BeiMiCommon");
cc.Class({
  extends: beiMiCommon,

  properties: {
    leaveDialogprefab: {
      default: null,
      type: cc.Prefab
    },
    setting:{
      default: null,
      type: cc.Prefab
    },

    //离开房间UI逻辑
    applyLeaveNode: {
      default: null,
      type: cc.Node
    },

    agreeNode: {
      default: null,
      type: cc.Node
    },

    refusedNode: {
      default: null,
      type: cc.Node
    },

    doneNode: {
      default: null,
      type: cc.Node
    },

    leaveTipLabel: {
      default: null,
      type: cc.Label
    },
    doneLabel: {
      default: null,
      type: cc.Label
    },


  },

  // use this for initialization
  onLoad: function() {

  },


  onBackClick: function() {
    console.log("------要离开房间");
    if(cc.beimi.gamestatus == 'ready'){
       this.leaveRoomOnNoPlaying();
       cc.beimi.joinroom=false;
       this.scene(cc.beimi.gametype, this);
    }else
    if (this.leaveDialogprefab) {
      cc.beimi.openwin = cc.instantiate(this.leaveDialogprefab);
      cc.beimi.openwin.parent = this.root();
    }

  },

  onSettingsClick: function() {
      cc.beimi.audio.playUiSound();
      cc.beimi.openwin = cc.instantiate(this.setting) ;
      cc.beimi.openwin.parent = this.root();
   },


  leaveRoomOnNoPlaying: function() {
    if (this.ready()) {
      let socket = this.socket();
      //1 强制退出  2 申请退出  3服务器发送有人申请退出给其他玩家  4 其他玩家投票给服务器  5投票结果给申请退出人  6 有玩家强制退出
      // 7 房间没开始我要退出
      var param = {
        type: '7'
      };
      socket.emit("leaveroom", JSON.stringify(param));
    }
  },

  forceLeaveRoom: function() {
    if (this.ready()) {
      let socket = this.socket();
      //1 强制退出  2 申请退出  3服务器发送有人申请退出给其他玩家  4 其他玩家投票给服务器  5投票结果给申请退出人  6 有玩家强制退出
      // 7 房间没开始我要退出
      var param = {
        type: '1'
      };
      socket.emit("leaveroom", JSON.stringify(param));
      this.closeOpenWin();
      // this.scene(cc.beimi.gametype, this);
    }
  },

  applyLeaveRoom: function() {
    if (this.ready()) {
      let socket = this.socket();
      var param = {
        type: '2'
      };
      socket.emit("leaveroom", JSON.stringify(param));
      this.closeOpenWin();
      this.alert("申请退出已发出请耐心等待，1分钟之内会有答复");
    }
  },

  agreeLeaveRoom: function() {
    if (this.ready()) {
      let socket = this.socket();
      var param = {
        type: '4',
        isAgree: "1",
        srcUserId: cc.beimi.leaveUserId
      };
      socket.emit("leaveroom", JSON.stringify(param));
      this.doneNode.active = true;
      this.doneLabel.string = '已同意';
      this.agreeNode.active = false;
      this.refusedNode.active = false;
    }
  },

  refuseLeaveRoom: function() {
    if (this.ready()) {
      let socket = this.socket();
      var param = {
        type: '4',
        isAgree: "0",
        srcUserId: cc.beimi.leaveUserId
      };
      socket.emit("leaveroom", JSON.stringify(param));
      this.doneNode.active = true;
      this.doneLabel.string = '已拒绝';
      this.agreeNode.active = false;
      this.refusedNode.active = false;
    }
  },


});

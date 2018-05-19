var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

  properties: {
    timeLabel: {
      default: null,
      type: cc.Label
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

    myLabel: {
      default: null,
      type: cc.Label
    },

    roomIdLabel: {
      default: null,
      type: cc.Label
    },

    juLabel:{
      default: null,
      type: cc.Label
    }
  },

  onLoad: function() {},

  init(result,index) {
    console.error("item---------->",result);
    this.roomId = result[0].roomId;
    this.roomUuid = result[0].roomUuid;
    this.timeLabel.string = result[0].date;
    for (var i = 0; i < result.length; i++) {
      if (result[i].userNo==cc.beimi.user.username) {
        this.myLabel.string = "我的成绩："+result[i].score;
        result.splice(i, 1);
      }
    }

    this.player1Label.string = result[0].nickname;
    this.player2Label.string = result[1].nickname;
    this.player3Label.string = result[2].nickname;

    this.roomIdLabel.string = (index+1) +" 房间号："+result[0].roomId;
    this.juLabel.string = "局数："+result[0].num;
  }
});

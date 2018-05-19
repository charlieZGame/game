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

    player4Label: {
      default: null,
      type: cc.Label
    },

  },

  onLoad: function() {},

  init(result,index) {
    // if (index==0) {
    //   this.player1Label.string=result.obj[0].nickname;
    //   this.player2Label.string=result.obj[1].nickname;
    //   this.player3Label.string=result.obj[2].nickname;
    //   this.player4Label.string=result.obj[3].nickname;
    // }else {
      this.timeLabel.string=(index+1)+".  "+ this.timestampToTime(result.createTime);
      this.player1Label.string=result.obj[0].score;
      this.player2Label.string=result.obj[1].score;
      this.player3Label.string=result.obj[2].score;
      this.player4Label.string=result.obj[3].score;
    // }

  },

  timestampToTime(timestamp) {
      var date = new Date(timestamp);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
      var  Y = date.getFullYear() + '-';
      var  M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
      var  D = date.getDate()<10?("0"+date.getDate()+' '): date.getDate()+' ';
      var  h = date.getHours()<10?("0"+date.getHours()+':'): date.getHours()+':';
      var  m = date.getMinutes()<10?("0"+date.getMinutes()+''): date.getMinutes()+'';
      return M+D+h+m;
    }


});

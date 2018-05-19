var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

    properties: {

    },


    start () {

    },

    // update (dt) {},
      shareFriendsClick:function(event,data){
        this.wxShare("三缺一", "快来三缺一跟我嗨到爆！", "http://www.laiyuanmajiang.top:8080/wap/index.html");
      },

      shareCircleClick:function(event,data){
        this.wxShare("三缺一", "快来三缺一跟我嗨到爆！", "http://www.laiyuanmajiang.top:8080/wap/index.html");
      },
});

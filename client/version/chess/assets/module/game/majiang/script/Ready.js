var beiMiCommon = require("BeiMiCommon");
cc.Class({
  extends: beiMiCommon,

  properties: {
    // foo: {
    //    default: null,      // The default value will be used only when the component attaching
    //                           to a node for the first time
    //    url: cc.Texture2D,  // optional, default is typeof default
    //    serializable: true, // optional, default is true
    //    visible: true,      // optional, default is true
    //    displayName: 'Foo', // optional
    //    readonly: false,    // optional, default is false
    // },
    // ...
    target: {
      default: null,
      type: cc.Node
    }
  },

  // use this for initialization
  onLoad: function() {
    let self = this;

    this.node.on("chatbutton", function(event) {
      self.settingChatDialog(self);
      event.stopPropagation();
    });

    this.node.on("sound1", function(event) {
      var param = {
        type: '1',
        sound: 'sound1',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(1);
    });

    this.node.on("sound2", function(event) {
      var param = {
        type: '1',
        sound: 'sound2',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(2);
    });

    this.node.on("sound3", function(event) {
      var param = {
        type: '1',
        sound: 'sound3',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(3);
    });

    this.node.on("sound4", function(event) {
      var param = {
        type: '1',
        sound: 'sound4',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(4);
    });

    this.node.on("sound5", function(event) {
      var param = {
        type: '1',
        sound: 'sound5',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(5);
    });

    this.node.on("sound6", function(event) {
      var param = {
        type: '1',
        sound: 'sound6',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(6);
    });

    this.node.on("sound7", function(event) {
      var param = {
        type: '1',
        sound: 'sound7',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(7);
    });

    this.node.on("sound8", function(event) {
      var param = {
        type: '1',
        sound: 'sound8',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(8);
    });

    this.node.on("sound9", function(event) {
      var param = {
        type: '1',
        sound: 'sound9',
        srcUserId:cc.beimi.user.id
      };
      self.emitSocket(param,self);
      self.settingChatDialog(self);
      event.stopPropagation();
      cc.beimi.audio.playCharSound(9);
    });
  },

  emitSocket(param,self) {
    let socket = this.socket();
    socket.emit("chat", JSON.stringify(param));
    let majiang = self.target.getComponent("MajiangDataBind");
    majiang.chat_event(param);
    event.stopPropagation();
  },

  settingChatDialog(self){
    cc.beimi.audio.playUiSound();
    let majiang = self.target.getComponent("MajiangDataBind");
    majiang.settingChatDialog();
  },

  onClick: function(event) {
    cc.beimi.audio.playUiSound();
    let majiang = this.target.getComponent("MajiangDataBind");
    majiang.startgame();
  },

  onChatClick: function(event, data) {
    cc.beimi.audio.playUiSound();
    this.node.dispatchEvent(new cc.Event.EventCustom(data, true));
  }
});

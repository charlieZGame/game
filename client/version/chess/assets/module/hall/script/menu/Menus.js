// Learn cc.Class:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/class.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/class/index.html
// Learn Attribute:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/reference/attributes.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/reference/attributes/index.html
// Learn life-cycle callbacks:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/life-cycle-callbacks.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/life-cycle-callbacks/index.html

cc.Class({
    extends: cc.Component,

    properties: {
      messageBtn: {
          default: null,
          type: cc.Node
      },
      playwayBtn: {
          default: null,
          type: cc.Node
      },
      shoppingBtn: {
          default: null,
          type: cc.Node
      },
      feedbackBtn: {
          default: null,
          type: cc.Node
      }
    },

    // LIFE-CYCLE CALLBACKS:

    onLoad () {
      this.messageBtn.active = false;
      this.playwayBtn.active = false;
      this.shoppingBtn.active = false;
      this.feedbackBtn.active = false;
    },

    start () {

    },

    // update (dt) {},
});

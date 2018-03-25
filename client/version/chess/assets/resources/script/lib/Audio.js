cc.Class({
  extends: cc.Component,

  properties: {

    bgVolume: 0, // 背景音量

    deskVolume: 1.0, //   房间 房间音量

    bgAudioID: -1 //   背景 音乐  id
  },

  // use this for initialization
  init: function() {
    var t = cc.sys.localStorage.getItem("bgVolume");
    if (t != null) {
      this.bgVolume = parseFloat(t);
    }

    var t = cc.sys.localStorage.getItem("deskVolume");

    if (t != null) {
      this.deskVolume = parseFloat(t);
    }

    cc.game.on(cc.game.EVENT_HIDE, function() {
      cc.audioEngine.pauseAll();
    });
    cc.game.on(cc.game.EVENT_SHOW, function() {
      cc.audioEngine.resumeAll();
    });
  },

  // called every frame, uncomment this function to activate update callback
  // update: function (dt) {

  // },

  getUrl: function(url) {
    return cc.url.raw("resources/sounds/" + url);
  },

  playBGM: function(url) {
    var audioUrl = this.getUrl(url);
    if (this.bgAudioID >= 0) {
      cc.audioEngine.stop(this.bgAudioID);
    }
    this.bgAudioID = cc.audioEngine.play(audioUrl, true, this.bgVolume);
  },

  playSFX: function(url) {
    var audioUrl = this.getUrl(url);
    if (this.deskVolume > 0) {
      var audioId = cc.audioEngine.play(audioUrl, false, this.deskVolume);
    }
  },

  setSFXVolume: function(v) {
    if (this.deskVolume != v) {
      cc.sys.localStorage.setItem("deskVolume", v);
      this.deskVolume = v;
    }
  },

  getState: function() {
    return cc.audioEngine.getState(this.bgAudioID);
  },
  setBGMVolume: function(v, force) {
    console.log("------setBGMVolume----000--v--", v);
    if (this.bgAudioID >= 0) {
      if (v > 0 && cc.audioEngine.getState(this.bgAudioID) === cc.audioEngine.AudioState.PAUSED) {
        cc.audioEngine.resume(this.bgAudioID);
      } else if (v == 0) {
        cc.audioEngine.pause(this.bgAudioID);
      }
    }
    if (this.bgVolume != v || force) {
      cc.sys.localStorage.setItem("bgVolume", v);
      this.bgVolume = v;
      cc.audioEngine.setVolume(this.bgAudioID, v);
    }

    console.log("------setBGMVolume--------", this.bgVolume);
  },

  playUiSound: function() {
    this.playSFX("ui_click.mp3");
  },

  //开始发牌
  beginGame() {
    this.playSFX("horse/go.mp3");
  },

  //聊天
  playCharSound(type) {
    switch (type) {
      case 1:
        this.playSFX("fix_msg_1.mp3");
        break;
      case 2:
        this.playSFX("fix_msg_2.mp3");
        break;
      case 3:
        this.playSFX("fix_msg_3.mp3");
        break;
      case 4:
        this.playSFX("fix_msg_4.mp3");
        break;
      case 5:
        this.playSFX("fix_msg_5.mp3");
        break;
      case 6:
        this.playSFX("fix_msg_6.mp3");
        break;
      case 7:
        this.playSFX("fix_msg_7.mp3");
        break;
      case 8:
        this.playSFX("fix_msg_8.mp3");
        break;
      case 9:
        this.playSFX("fix_msg_9.mp3");
        break;
      default:

    }
  },

  //打牌
  playTakeCard(cardValue) {
    let num =  parseInt(cardValue / 4);
    if (num < 0) {
      num = num + 8;
      switch (num) {
        case 1:
          this.playSFX("nv/31.mp3");
          break;
        case 2:
          this.playSFX("nv/41.mp3");
          break;
        case 3:
          this.playSFX("nv/51.mp3");
          break;
        case 4:
          this.playSFX("nv/61.mp3");
          break;
        case 5:
          this.playSFX("nv/71.mp3");
          break;
        case 6:
          this.playSFX("nv/81.mp3");
          break;
        case 7:
          this.playSFX("nv/91.mp3");
          break;
        default:

      }
    } else {
      switch (num) {
        case 26:
          this.playSFX("nv/9.mp3");
          break;
        case 25:
          this.playSFX("nv/8.mp3");
          break;
        case 24:
          this.playSFX("nv/7.mp3");
          break;
        case 23:
          this.playSFX("nv/6.mp3");
          break;
        case 22:
          this.playSFX("nv/5.mp3");
          break;
        case 21:
          this.playSFX("nv/4.mp3");
          break;
        case 20:
          this.playSFX("nv/3.mp3");
          break;
        case 19:
          this.playSFX("nv/2.mp3");
          break;
        case 18:
          this.playSFX("nv/1.mp3");
          break;

          //万
        case 0:
          this.playSFX("nv/11.mp3");
          break;
        case 1:
          this.playSFX("nv/12.mp3");
          break;
        case 2:
          this.playSFX("nv/13.mp3");
          break;
        case 3:
          this.playSFX("nv/14.mp3");
          break;
        case 4:
          this.playSFX("nv/15.mp3");
          break;
        case 5:
          this.playSFX("nv/16.mp3");
          break;
        case 6:
          this.playSFX("nv/17.mp3");
          break;
        case 7:
          this.playSFX("nv/18.mp3");
          break;
        case 8:
          this.playSFX("nv/19.mp3");
          break;

        case 9:
          this.playSFX("nv/21.mp3");
          break;
        case 10:
          this.playSFX("nv/22.mp3");
          break;
        case 11:
          this.playSFX("nv/23.mp3");
          break;
        case 12:
          this.playSFX("nv/24.mp3");
          break;
        case 13:
          this.playSFX("nv/25.mp3");
          break;
        case 14:
          this.playSFX("nv/26.mp3");
          break;
        case 15:
          this.playSFX("nv/27.mp3");
          break;
        case 16:
          this.playSFX("nv/28.mp3");
        case 17:
          this.playSFX("nv/29.mp3");
          break;

        default:

      }
    }

  },
  pauseAll: function() {
    cc.audioEngine.pauseAll();
  },

  resumeAll: function() {
    cc.audioEngine.resumeAll();
  }
});

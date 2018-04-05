var beiMiCommon = require("BeiMiCommon");

cc.Class({
  extends: beiMiCommon,

  properties: {
    playerprefab: {
      default: null,
      type: cc.Prefab
    },
    statebtn: {
      default: null,
      type: cc.Node
    },
    mjtimer: {
      default: null,
      type: cc.Label
    },
    desk_tip: {
      default: null,
      type: cc.Node
    },
    desk_cards: {
      default: null,
      type: cc.Label
    },
    cards_current: {
      default: null,
      type: cc.Prefab
    },
    cards_panel: {
      default: null,
      type: cc.Node
    },
    one_card_panel: {
      default: null,
      type: cc.Node
    },
    left_panel: {
      default: null,
      type: cc.Node
    },
    right_panel: {
      default: null,
      type: cc.Node
    },
    top_panel: {
      default: null,
      type: cc.Node
    },
    cards_left: {
      default: null,
      type: cc.Prefab
    },
    cards_right: {
      default: null,
      type: cc.Prefab
    },
    cards_top: {
      default: null,
      type: cc.Prefab
    },
    takecards_one: { //我的和 对家出的牌
      default: null,
      type: cc.Prefab
    },
    takecards_left: {
      default: null,
      type: cc.Prefab
    },
    takecards_right: {
      default: null,
      type: cc.Prefab
    },

    deskcards_current_panel: { //我的和 对家出的牌的位置节点
      default: null,
      type: cc.Node
    },
    deskcards_right_panel: {
      default: null,
      type: cc.Node
    },
    deskcards_top_panel: {
      default: null,
      type: cc.Node
    },
    deskcards_left_panel: {
      default: null,
      type: cc.Node
    },

    searchlight: {
      default: null,
      type: cc.Node
    },
    actionnode_two: { //动作节点
      default: null,
      type: cc.Node
    },
    actionnode_two_list: { //动作节点
      default: null,
      type: cc.Node
    },
    actionnode_three: { //动作节点
      default: null,
      type: cc.Node
    },
    actionnode_three_list: { //动作节点
      default: null,
      type: cc.Node
    },
    actionnode_deal: { //动作节点
      default: null,
      type: cc.Node
    },

    action_gang_ming_prefab: {
      default: null,
      type: cc.Prefab
    },
    action_gang_an_prefab: {
      default: null,
      type: cc.Prefab
    },

    action_peng_prefab: {
      default: null,
      type: cc.Prefab
    },

    action_chi_prefab: {
      default: null,
      type: cc.Prefab
    },

    cards_gang_ming_prefab: { //我的名杠，碰，吃
      default: null,
      type: cc.Prefab
    },
    cards_gang_an_prefab: { //我的暗杠
      default: null,
      type: cc.Prefab
    },

    cards_gang_ming_left_prefab: { //左边玩家名杠，碰，吃
      default: null,
      type: cc.Prefab
    },
    cards_gang_an_left_prefab: { //左边玩家暗杠
      default: null,
      type: cc.Prefab
    },

    cards_gang_ming_right_prefab: { //右边玩家名杠，碰，吃
      default: null,
      type: cc.Prefab
    },
    cards_gang_an_right_prefab: { //右边玩家暗杠
      default: null,
      type: cc.Prefab
    },

    cards_gang_ming_top_prefab: { //上边玩家名杠，碰，吃
      default: null,
      type: cc.Prefab
    },
    cards_gang_an_top_prefab: { //上边玩家暗杠
      default: null,
      type: cc.Prefab
    },

    roomid: {
      default: null,
      type: cc.Label
    },
    gang_current: { //动作节点
      default: null,
      type: cc.Node
    },
    gang_left: { //动作节点
      default: null,
      type: cc.Node
    },
    gang_right: { //动作节点
      default: null,
      type: cc.Node
    },
    gang_top: { //动作节点
      default: null,
      type: cc.Node
    },

    summary: {
      default: null,
      type: cc.Prefab
    },
    inviteplayer: {
      default: null,
      type: cc.Prefab
    },
    hu_cards_current: {
      default: null,
      type: cc.Node
    },
    hu_cards_top: {
      default: null,
      type: cc.Node
    },
    hu_cards_left: {
      default: null,
      type: cc.Node
    },
    hu_cards_right: {
      default: null,
      type: cc.Node
    },
    mask: {
      default: null,
      type: cc.Node
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

    refusedNode: {
      default: null,
      type: cc.Node
    },

    chatScrollView: {
      default: null,
      type: cc.Node
    },

    laiziNode: {
      default: null,
      type: cc.Node
    },

    ju: {
      default: null,
      type: cc.Label
    },

    hucards_tip: { //我 将要胡的牌
      default: null,
      type: cc.Prefab
    },

    ting_tip: { //ting牌node
      default: null,
      type: cc.Node
    },

    hucards_tip_layout: { //我将要胡的牌摆放位置
      default: null,
      type: cc.Node
    },

    selectkou_dialog:{
      default: null,
      type: cc.Node
    },

    desk_title:{
      default: null,
      type: cc.Sprite
    }
  },

  // use this for initialization
  /**
     * 重构后，只有两个消息类型
     */
  onLoad: function() {
    this.initdata(true);
    this.resize();
    this.ting_tip.active = false;
    let self = this;
    if (cc.beimi.extparams.gametype == "koudajiang") {
      cc.loader.loadRes("images/img/game_kou", cc.SpriteFrame, function(error, spriteFrame) {
          self.desk_title.spriteFrame = spriteFrame;
        });
    }
    if (this.mask != null) {
      this.mask.active = false;
    }

    if (this.chatScrollView) {
      this.chatScrollView.active = false;
    }

    // this.node.on('mousedown', function(event) {
    //   console.log("场景中的鼠标点击事件--mousedown---------", self.chatScrollView, self.chatScrollView.active);
    //   if (self.chatScrollView && self.chatScrollView.active == true) {
    //     self.chatScrollView.active = false
    //   }
    // }, self);

    // let testNum=0;
    this.chatScrollView.on('mousedown', function(event) {
      console.log("场景中的鼠标点击事件--聊天框---------");
      //---------------------测试杠碰的
    //   testNum++;
    //   const data={
    //     actype:'',
    //     action:testNum%2==0?'gang':'',
    //     card:5,
    //   };
    //   let cards_gang;
    //   if (data.actype == "an") {
    //     cards_gang = cc.instantiate(self.cards_gang_an_prefab);
    //   } else {
    //     cards_gang = cc.instantiate(self.cards_gang_ming_prefab);
    //   }
    //   let temp_script = cards_gang.getComponent("GangAction");
    //   if (data.action == "gang") {
    //     temp_script.init(data.card, true);
    //   } else {
    //     temp_script.init(data.card, false);
    //   }
    //    // cards_gang.parent = self.gang_current;
    //     cards_gang.parent = self.gang_top;
    // //   cards_gang.parent = self.gang_right;
    //   self.leftactioncards.push(cards_gang);

    //   //胡牌测试
      // for (var i = 0; i < 10; i++) {
        // let hucards_tip;
        // hucards_tip = cc.instantiate(self.hucards_tip);
        // self.leftactioncards.push(hucards_tip);
        // let temp_script = hucards_tip.getComponent("huCards");
        // temp_script.init(89);
        // hucards_tip.parent = self.hucards_tip_layout;
      // }


    //  测试扣牌
    // self.selectkou_dialog.active = true;
    // console.log("可扣牌张数----》", self.playercards.length);
    //   for (var inx =self.playercards.length-4; inx>=0&&inx < self.playercards.length;inx++) {
    //     let handcards = self.playercards[inx].getComponent("HandCards");
    //     console.log("最后的4张牌----》",handcards);
    //     handcards.setCardKou();
    //     self.isCankou = true;
    //   }
      //结果页面测试
      // self.summarypage = cc.instantiate(self.summary);
      // self.summarypage.parent = self.root();
      // let temp = self.summarypage.getComponent("summary");
      // let datare={"game":"d7128eb8f70b49c9ac0eedd5c4c1083b","ratio":2,"command":"allcards","finished":true,"gameRoomOver":false,"score":4,"players":[{"userid":"c178612037ac413dad4d824416961f2a","username":"Guest_11MQ5x","ratio":2,"score":4,"gameover":false,"balance":0,"win":true,"gameResultChecks":[{"pairs":"88,16","three":"92,1,5,14,17,21,23,25,31,93,80,81"},{"pairs":"88,17","three":"92,1,5,14,16,21,23,25,31,93,80,81"},{"pairs":"16,17","three":"88,1,5,92,14,21,23,25,31,93,80,81"},{"pairs":"80,81","three":"88,1,5,92,14,16,93,17,21,23,25,31"}],"dizhu":true},{"userid":"f3fa56bdf75e4bc4a1496d797d63286c","username":"Guest_1F5oVN","ratio":2,"score":4,"gameover":false,"balance":0,"win":false,"gameResultChecks":[{"others":"39,54,62,63,76,79,95,96,97,105,74,84,53"}],"dizhu":false},{"userid":"960017a406234b8784ed9bf649797089","username":"Guest_1opcw5","ratio":2,"score":4,"gameover":false,"balance":0,"win":false,"gameResultChecks":[{"pengs":"44,45,46","others":"-7,-6,51,67,68,100,101,107,47,60"}],"dizhu":false},{"userid":"140c7ea5999848b48195302fe497702b","username":"Guest_1cR94c","ratio":2,"score":4,"gameover":false,"balance":0,"win":false,"gameResultChecks":[{"pengs":"0,1,2","others":"4,11,20,26,30,49,59,64,66,99"}],"dizhu":false}],"hu":false}
      // temp.create(self, datare);

    event.stopPropagation();
    }, self);

    if (this.ready()) {
      let socket = this.socket();
      this.routes = {};
      /**
             * 已初始的玩家对象池 ， 牌局结束 或者 有新玩家加入， 老玩家离开 等事件的时候，需要做对象池回收
             * @type {Array}
             */
      this.playersarray = new Array(); //玩家列表

      this.playercards = new Array(); //手牌对象
      this.laizicards = new Array(); //赖子对象

      this.leftcards = new Array(); //左侧玩家手牌
      this.rightcards = new Array(); //右侧玩家手牌
      this.topcards = new Array(); //对家手牌

      this.deskcards = new Array(); //当前玩家和 对家 已出牌

      this.actioncards = new Array(); //当前玩家和 对家 已出牌
      this.leftactioncards = new Array();
      this.rightactioncards = new Array();
      this.topactioncards = new Array();
      this.huactioncardstip = new Array();

      //记录一下是否可以扣
      this.isCankou = true;

      this.laiziValues = new Array();

      this.inited = false;

      this.centertimer = null;

      this.summarypage = null;

      this.exchange_state("init", this);

      /**
             * 发射的事件， 在 出牌双击 / 滑动出牌的时候发射的，此处用于接受后统一处理， 避免高度耦合
             * 之所以这样设计，是因为在TakeMJCard里需要引用 麻将牌的 对象池 和 出牌的对象池，如果采用对象传入或者 通过find获取的方式处理
             * 则会导致高度的 组件耦合，不利于系统 未来扩展，也会导致 业务逻辑交叉/混乱
             * 无论 胡牌/杠/碰/吃，都需要采用这种方式处理
             */
      this.node.on('takecard', function(event) {
        let card = event.target.getComponent("TakeMJCard");
        if (card != null) {
          let card_script = card.target.getComponent("HandCards");

          /**
          * 提交数据，等待服务器返回
          */
          //开始匹配
          console.log("======玩家doplaycards================", card_script.value);
          socket.emit("doplaycards", card_script.value);
        }
        event.stopPropagation();
      });
      /**
        * ActionEvent发射的事件 ， 点击 杠 , 通知服务器端，用户点击了 杠 动作，服务器端进行处理，处理完毕后通知客户端后续动作
        */
      this.node.on("gang", function(event) {
        self.dealActionProcess(self);
        socket.emit("selectaction", "gang");
        event.stopPropagation();
      });
      /**
        * ActionEvent发射的事件 ， 点击 碰
        */
      this.node.on("peng", function(event) {
        self.dealActionProcess(self);
        socket.emit("selectaction", "peng");
        event.stopPropagation();
      });
      /**
             * ActionEvent发射的事件 ， 点击 吃
             */
      this.node.on("chi", function(event) {
        self.dealActionProcess(self);
        socket.emit("selectaction", "chi");
        event.stopPropagation();
      });
      /**
      * ActionEvent发射的事件 ， 点击 胡
      */
      this.node.on("hu", function(event) {
        self.dealActionProcess(self);
        socket.emit("selectaction", "hu");
        event.stopPropagation();
      });
      /**
             * ActionEvent发射的事件 ， 点击 过
             */
      this.node.on("guo", function(event) {
        self.dealActionProcess(self);
        socket.emit("selectaction", "guo");
        event.stopPropagation();
      });

      this.node.on("bukou", function(event) {
        socket.emit("answerKou","0");
        self.isCankou = false;
        self.selectkou_dialog.active = false;
        self.canceltimer(self);
        event.stopPropagation();
      });

      this.node.on("koupai", function(event) {
        socket.emit("answerKou","1");
        //---------------------测试
          console.log("可扣牌张数----》", self.playercards.length);
          for (var inx =self.playercards.length-4; inx>=0&&inx < self.playercards.length;inx++) {
            let handcards = self.playercards[inx].getComponent("HandCards");
            console.log("最后的4张牌----》",handcards);
            handcards.setCardKou();
            self.isCankou = true;

            //是扣的牌
            if(handcards.koucard){
              self.playercards[inx].zIndex = handcards.value - 1000;
            }

          }
        self.selectkou_dialog.active = false;
        self.canceltimer(self)
        event.stopPropagation();
      });

      if (cc.beimi != null) {
        if (cc.beimi.gamestatus != null && cc.beimi.gamestatus == "playing") {
          //恢复数据
          this.recovery();
        } else if (cc.beimi.extparams != null && cc.beimi.extparams.gamemodel == "room") {
          /**
                     * 房卡模式，开始启动游戏，当前玩家进入等待游戏的状态，显示邀请好友游戏，并分配 6位数字的房间号码
                     */
          /**
                     * 处理完毕，清理掉全局变量
                     * @type {null}
                     */
          this.invite = cc.instantiate(this.inviteplayer);
          this.invite.active = false;
        }
        this.initgame();
      }
      this.addComponent("Voice");
      cc.beimi.audio.playBGM("bgFight.mp3");
    }

  },

  settingChatDialog(self) {
    if (self) {
      if (self.chatScrollView && self.chatScrollView.active == true) {
        self.chatScrollView.active = false
      } else if (self.chatScrollView && self.chatScrollView.active == false) {
        self.chatScrollView.active = true
      }
    } else {
      if (this.chatScrollView && this.chatScrollView.active == true) {
        this.chatScrollView.active = false
      } else if (this.chatScrollView && this.chatScrollView.active == false) {
        this.chatScrollView.active = true
      }
    }
  },

  initgame: function() {
    let self = this;
    if (this.ready()) {
      let socket = this.socket();
      /**
             * 接受指令
             */
      this.map("joinroom", this.joinroom_event); //加入房价
      this.map("players", this.players_event); //接受玩家列表

      this.map("banker", this.banker_event); //庄家

      this.map("play", this.play_event); //人齐了，接收发牌信息

      this.map("selectcolor", this.selectcolor_event); //从服务端发送的 定缺的 指令，如果服务端玩法里不包含定缺， 可以不发送这个指令而是直接开始打牌

      // this.map("selectresult", this.selectresult_event); //暂时去掉定缺 从服务端发送的 定缺的 指令，如果服务端玩法里不包含定缺， 可以不发送这个指令而是直接开始打牌

      this.map("lasthands", this.lasthands_event); //庄家开始打牌了，允许出牌

      this.map("takecards", this.takecard_event); //揭牌

      this.map("action", this.action_event); //服务端发送的 动作事件，有杠碰吃胡过可以选择
      this.map("ting", this.ting_event);

      this.map("selectaction", this.selectaction_event); //我选择的动作， 杠碰吃胡

      this.map("dealcard", this.dealcard_event); //我出的牌

      this.map("allcards", this.allcards_event); //结束结果

      this.map("recovery", this.recovery_event); //恢复牌局数据

      this.map("roomready", this.roomready_event); //提示  这个指令后台没发

      this.map("playeready", this.playeready_event); //玩家点击了开始游戏 ， 即准备就绪

      socket.on("command", function(result) {
        console.log("接受command指令===result=====>", JSON.stringify(result));
        // 我修改的
        // cc.beimi.gamestatus = "playing" ;
        if (self.inited == true) {
          var data = self.parse(result);
          self.route(data.command)(data, self);
        }
      });

      this.map("chat", this.chat_event); //接收了聊天信息
      socket.on("chat", function(result) {
        if (self.inited == true) {
          var data = self.parse(result);
          self.route("chat")(data, self);
        }
      });

      var param = {
        token: cc.beimi.authorization,
        playway: cc.beimi.extparams.playway,
        orgi: cc.beimi.user.orgi,
        extparams: cc.beimi.extparams
      };
      socket.emit("joinroom", JSON.stringify(param));

      this.inited = true;

      if (this.applyLeaveNode) {
        self.applyLeaveNode.active = false;
      }
      //玩家手动离开房间指令接收
      if (this.ready()) {
        socket.on("leaveroom", function(result) {
          console.log("接收到离开房间指令===result=====>", result);
          let resultObj = self.parse(result);
          if (cc.beimi.user.id == result.srcUserId) {
            return;
          } else if (resultObj.type == 3 || resultObj.type == "3") {
            self.applyLeaveNode.active = true;
            self.doneNode.active = false;
            self.leaveTipLabel.string = resultObj.msg;
            cc.beimi.leaveUserId = resultObj.srcUserId;
            self.agreeNode.active = true;
            self.refusedNode.active = true;
          } else if (resultObj != null && (resultObj.type == 5 || resultObj.type == "5")) {
            if (resultObj.isAgree == 1) {
              cc.beimi.isLeaveroom = true;
            }
            cc.beimi.leaveUserId = "";
            self.alert(resultObj.msg || '有人离场，房间解散');
          } else if (result != null && (resultObj.type == 6 || resultObj.type == "6")) {
            cc.beimi.isLeaveroom = true;
            self.alert(resultObj.msg || '有人强制离场，房间解散');
          } else {}
        });
      }
    }
  },

  initdata: function(initplayer) {
    /**
         * 适配屏幕尺寸
         */

    if (initplayer == true) {
      /**
             * 预制的 对象池
             * @type {cc.NodePool}
             */
      this.playerspool = new cc.NodePool();
      /**
             *
             * 初始化玩家 的 对象池
             */
      for (var i = 0; i < 4; i++) {
        this.playerspool.put(cc.instantiate(this.playerprefab));
      }
    }

    /**
      * 当前玩家的 麻将牌的 对象池
      * @type {cc.NodePool}
       */
    this.cardpool = new cc.NodePool();
    /**
         * 初始化当前玩家的麻将牌 对象池
         */
    for (var i = 0; i < 14; i++) {
      this.cardpool.put(cc.instantiate(this.cards_current));
    }

    /**
     * 赖子的 对象池
     * @type {cc.NodePool}
     */
    this.laizicardpool = new cc.NodePool();
    /**
      * 初始化赖子的 对象池
      */
    for (var i = 0; i < 6; i++) {
      this.laizicardpool.put(cc.instantiate(this.cards_current));
    }
  },
  /**
     * 新创建牌局，首个玩家加入，进入等待状态，等待其他玩家加入，服务端会推送 players数据
     * @param data
     * @param context
     */
  joinroom_event: function(data, context) {
    if (data.cardroom == true && context.inviteplayer != null) {
      let script = context.invite.getComponent("BeiMiQR")
      script.init(data.roomid);
      context.invite.parent = context.root();

      if (context.roomid != null) {
        context.roomid.string = data.roomid;
      }
    } else {
      if (context.roomid != null) {
        context.roomid.string = "大厅房间";
      }
    }

    var player = context.playerspool.get();

    //新添加的代码
    if (player == null) {
      /**
             * 预制的 对象池
             * @type {cc.NodePool}
             */
      context.playerspool = new cc.NodePool();
      /**
             *
             * 初始化玩家 的 对象池
             */
      for (var i = 0; i < 4; i++) {
        context.playerspool.put(cc.instantiate(context.playerprefab));
      }
      player = context.playerspool.get();
    }

    var playerscript = player.getComponent("MaJiangPlayer");
    //var playerscript = player.getComponent("MaJiangPlayer");
    var inx = null,
      tablepos = "";
    if (data.player.id == cc.beimi.user.id) {
      player.setPosition(-570, -150);
      tablepos = "current";
      context.index = data.index;
    } else {
      inx = data.index - context.index;
      if (inx == 1) {
        //var playerscript = player.getComponent("MaJiangPlayer");
        player.setPosition(570, 50);
        tablepos = "right";
      } else if (inx == 2) {
        //var playerscript = player.getComponent("MaJiangPlayer");
        player.setPosition(400, 300);
        tablepos = "top";
      } else if (inx == 3) {
        //var playerscript = player.getComponent("MaJiangPlayer");
        player.setPosition(-570, 50);
        tablepos = "left";
      }
    }

    playerscript.init(data.player, inx, tablepos);
    player.parent = context.root();
    context.playersarray.push(player);
    /**
         * 初始化状态，首个玩家加入，然后开始等待其他玩家 ， 如果是 恢复数据， 则不会进入
         */
    //this.statusbtn.active = true ;
  },

  /**
     * 房卡模式下，邀请的好友人到齐了
     * @param data
     * @param context
     */
  roomready_event: function(data, context) {
    if (context.invite != null) {
      context.invite.destroy();
    }
  },
  /**
     *
     * @param data
     * @param context
     */
  playeready_event: function(data, context) {
    if (data.userid == cc.beimi.user.id) {
      context.exchange_state("ready", context);
    }
  },
  /**
     * 新创建牌局，首个玩家加入，进入等待状态，等待其他玩家加入，服务端会推送 players数据
     * @param data
     * @param context
     */
  takecard_event: function(data, context) {
    cc.beimi.audio.playTakeCard(data.card);
    if (data.userid == cc.beimi.user.id) {
      for (var inx = 0; inx < context.playercards.length;) {
        let handcards = context.playercards[inx].getComponent("HandCards");
        if (data.card == handcards.value) {

          context.playercards[inx].zIndex = 0;
          /**
                     * 从数组中移除
                     */
          context.playercards[inx].parent = null;

          handcards.reinit();
          /**
                     * 还回 对象池
                     */
          context.cardpool.put(context.playercards[inx]);

          /**
                     * 从数组中移除
                     */
          context.playercards.splice(inx, 1);

          /**
                     * 放到桌面 ， 需要重构
                     */
          let desk_card = cc.instantiate(context.takecards_one);
          let temp = desk_card.getComponent("DeskCards");
          temp.init(handcards.value);

          context.deskcards.push(desk_card);
          desk_card.parent = context.deskcards_current_panel;
        } else {
          handcards.relastone();
          if (handcards.laizi.active) {
            if (handcards.value >= 0) {
              context.playercards[inx].zIndex = handcards.value - 1000;
            } else {
              context.playercards[inx].zIndex = handcards.value - 800;
            }
          } else {
            if (handcards.selectcolor == true) {
              context.playercards[inx].zIndex = 1000 + handcards.value;
            } else {
              if(handcards.koucard){
                context.playercards[inx].zIndex = handcards.value - 1000;
              }else {
                if (handcards.value >= 0) {
                  context.playercards[inx].zIndex = handcards.value;
                } else {
                  context.playercards[inx].zIndex = 200 + handcards.value;
                }
              }

            }
          }
          inx = inx + 1; //遍历 ++,不处理移除的 牌
        }
      }
      /**
             * 重新排序
             */
      context.layout(context.cards_panel, function(fir, sec) {
        return fir.zIndex - sec.zIndex;
      });
      context.exchange_state("takecard", context); //隐藏 提示状态
    } else {
      //其他玩家出牌

      let temp = context.player(data.userid, context);
      let cardpanel,
        cardprefab,
        deskcardpanel;
      if (temp.tablepos == "right") {
        for (var inx = 0; inx < context.right_panel.children.length; inx++) {
          let right_temp = context.right_panel.children[inx].getComponent("SpecCards");
          right_temp.reinit();
        }

        cardpanel = context.right_panel;
        cardprefab = context.takecards_right;
        deskcardpanel = context.deskcards_right_panel;

      } else if (temp.tablepos == "left") {
        for (var inx = 0; inx < context.left_panel.children.length; inx++) {
          let left_temp = context.left_panel.children[inx].getComponent("SpecCards");
          left_temp.reinit();
        }

        cardpanel = context.left_panel;
        cardprefab = context.takecards_left;
        deskcardpanel = context.deskcards_left_panel;
      } else if (temp.tablepos == "top") {
        for (var inx = 0; inx < context.top_panel.children.length; inx++) {
          let top_temp = context.top_panel.children[inx].getComponent("SpecCards");
          top_temp.reinit();
        }

        cardpanel = context.top_panel;
        cardprefab = context.takecards_one;
        deskcardpanel = context.deskcards_top_panel;
      }
      /**
             * 销毁其中一个对象
             */
      if (cardpanel != null && cardpanel.children && cardpanel.children.length > 0) {
        cardpanel.children[cardpanel.children.length - 1].destroy();
      }
      let desk_card = cc.instantiate(cardprefab);
      let desk_script = desk_card.getComponent("DeskCards");
      desk_script.init(data.card);
      desk_card.parent = deskcardpanel;

      context.deskcards.push(desk_card);
    }
  },
  recover_desk_cards: function(userid, card, context) {
    if (userid == cc.beimi.user.id) {
      /**
             * 放到桌面 ， 需要重构
             */
      let desk_card = cc.instantiate(context.takecards_one);
      let temp = desk_card.getComponent("DeskCards");
      temp.init(card);

      context.deskcards.push(desk_card);
      desk_card.parent = context.deskcards_current_panel;
    } else {
      //其他玩家出牌
      let temp = context.player(userid, context);
      let cardpanel,
        cardprefab,
        deskcardpanel;
      if (temp.tablepos == "right") {

        cardpanel = context.right_panel;
        cardprefab = context.takecards_right;
        deskcardpanel = context.deskcards_right_panel;

      } else if (temp.tablepos == "left") {

        cardpanel = context.left_panel;
        cardprefab = context.takecards_left;
        deskcardpanel = context.deskcards_left_panel;
      } else if (temp.tablepos == "top") {

        cardpanel = context.top_panel;
        cardprefab = context.takecards_one;
        deskcardpanel = context.deskcards_top_panel;
      }
      let desk_card = cc.instantiate(cardprefab);
      let desk_script = desk_card.getComponent("DeskCards");
      desk_script.init(card);
      desk_card.parent = deskcardpanel;
    }
  },
  /**  揭牌
     * 下一个玩家抓牌的事件， 如果上一个玩家出牌后，没有其他玩家杠、碰、吃、胡等动作，则会同时有一个抓牌的事件，否则，会等待玩家 杠、碰、吃、胡完成
     * @param data
     * @param context
     */
  dealcard_event: function(data, context) {
    console.log("-----dealcard_event-------", data);
    let player = context.player(data.userid, context);
    context.select_action_searchlight(data, context, player);
    if (data.userid == cc.beimi.user.id) {
      context.initDealHandCards(context, data);
      console.log("我出牌了");
      context.cleanTingTip(context);
    } else {
      let inx = 0;
      if (player.tablepos == "top") {
        //context.right_panel ;
        inx = 1;
      } else if (player.tablepos == "left") {
        inx = 2;
      }
      context.initPlayerHandCards(0, 1, inx, context, true);
    }
    context.desk_cards.string = data.deskcards;
    if (context.action == "deal" && data.userid == cc.beimi.user.id) {
      //
    } else {
      context.exchange_state("action", context);
    }
  },

  select_action_searchlight: function(data, context, player) {
    context.exchange_searchlight(player.tablepos, context);
    context.exchange_state("nextplayer", context);
  },

  allcards_event: function(data, context) {
    console.log("收到allcards_event",data);
    cc.beimi.gamestatus = "notready";
    //结算界面，
    context.gameover = false;
    setTimeout(function() {
      context.summarypage = cc.instantiate(context.summary);
      context.summarypage.parent = context.root();
      let temp = context.summarypage.getComponent("summary");
      temp.create(context, data,context.laiziValues );

      if (data.gameRoomOver == true) { //房间解散
        context.gameover = true;
      }
    }, 2000);
    /**
         */
    context.exchange_state("allcards", context);
  },
  /**
     * 恢复牌局数据， 等待服务端推送 Players数据后进行恢复
     */
  recoverboard: function(data, context) {
    //this.statebtn.active = false ;
  },
  setAction: function(action, context) {
    context.action = action;
  },
  /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
  players_event: function(data, context) {
    context.collect(context); //先回收资源，然后再初始化
    var inx = 0;
    for (var i = 0; i < data.player.length; i++) {
      let temp = data.player[i];
      if (temp.id == cc.beimi.user.id) {
        context.index = i;
        break;
      }
    }
    if (data.player.length > 1 && inx >= 0) {
      var pos = inx + 1;
      while (true) {
        if (pos == data.player.length) {
          pos = 0;
        }
        if (context.playerexist(data.player[pos], context) == false) {
          var player = context.playerspool.get();
          var playerscript = player.getComponent("MaJiangPlayer");
          var tablepos = "";
          var temp = pos - context.index;
          if (temp == 1 || temp == -3) {
            //var playerscript = player.getComponent("MaJiangPlayer");
            player.setPosition(570, 50);
            tablepos = "right";
          } else if (temp == 2 || temp == -2) {
            //var playerscript = player.getComponent("MaJiangPlayer");
            player.setPosition(400, 300);
            tablepos = "top";
          } else if (temp == 3 || temp == -1) {
            //var playerscript = player.getComponent("MaJiangPlayer");
            player.setPosition(-570, 50);
            tablepos = "left";
          }

          playerscript.init(data.player[pos], inx, tablepos);
          player.parent = context.root();
          context.playersarray.push(player);
        }
        if (pos == inx) {
          break;
        }
        pos = pos + 1;
      }
    }
  },

  playerexist: function(player, context) {
    var inroom = false;
    if (player.id == cc.beimi.user.id) {
      inroom = true;
    } else {
      for (var j = 0; j < context.playersarray.length; j++) {
        let temp = context.playersarray[j];
        var playerscript = temp.getComponent("MaJiangPlayer");
        if (playerscript.data.id == player.id) {
          inroom = true;
          break;
        }
      }
    }
    return inroom;
  },

  /**
     * 接受新的庄家数据
     * @param data
     * @param context
     */
  banker_event: function(data, context) {
    for (var inx = 0; inx < context.playersarray.length; inx++) {
      let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
      if (temp.data.id == data.userid) {
        temp.banker();
        break;
      }
    }
  },

  //接受聊天消息
  chat_event: function(data, context) {
    if (!context) {
      context = this;
    }
    console.log("chat_event-------->", JSON.stringify(data));
    for (var inx = 0; inx < context.playersarray.length; inx++) {
      let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
      if (temp.data.id == data.srcUserId) {
        temp.setChatMessage(data.sound);
        setTimeout(function() {
          temp.hideChatMessage();
        }, 3000);
        break;
      }
    }
  },
  /**
     * 接收到服务端的 恢复牌局的数据 恢复牌局
     * @param data
     * @param context
     */
  recovery_event: function(data, context) {
    var mycards = context.decode(data.player.cards);
    //context.exchange_state("begin" , context);  //隐藏 提示状态
    /**
         * 恢复玩家数据
         */
    context.play_event(data.userboard, context,true);

    /**
         * 恢复庄家数据
         */
    context.banker_event(data.banker, context);

    /**
         *
         * 恢复定缺数据
         */
    context.selectresult_event(data.selectcolor, context);

    /**
         * 恢复其他玩家定缺数据
         */
    for (var i = 0; i < data.cardsnum.length; i++) {
      let temp = data.cardsnum[i];
      context.selectresult_event(temp.selectcolor, context);

      //这里有个错误
      var hiscards = context.decode(temp.hiscards);
      for (var j = 0; j < hiscards.length; j++) {
        context.recover_desk_cards(temp.userid, hiscards[j], context);
      }
    }
    /**
         * 恢复当前玩家 已出的牌
         */
    var hiscards = context.decode(data.hiscards);
    for (var j = 0; j < hiscards.length; j++) {
      context.recover_desk_cards(data.userid, hiscards[j], context);
    }
  },


  //听牌通知
  ting_event:function(data, context) {
    if (data.recommendCards&&data.recommendCards.length>0&&data.userid==cc.beimi.user.id) {
      context.ting_tip.active = true;
      let recommendCardsValue = context.decode(data.recommendCards);
      console.log("收到胡牌提醒---》");
      for (var i = 0; i < recommendCardsValue.length; i++) {
        let hucards_tip;
         hucards_tip = cc.instantiate(context.hucards_tip);
         context.huactioncardstip.push(hucards_tip);
         let temp_script = hucards_tip.getComponent("huCards");
         temp_script.init(recommendCardsValue[i]);
         hucards_tip.parent = context.hucards_tip_layout;
      }
    }
  },

  /**
     * 接受服务端的数据，玩家杠碰、吃胡等动作
     * @param data
     * @param context
     */
  action_event: function(data, context) {
    console.error("接受服务器通知的杠碰、吃胡等动作--------》");
    context.setAction("take", context);
    if (cc.beimi.user.id == data.userid) {
      /**
             * 隐藏其他动作
             */
      context.exchange_state("action", context);
      let gang,
        peng,
        chi,
        hu,
        guo;
      if (data.deal == true) { //发牌的动作
        for (var inx = 0; inx < context.actionnode_deal.children.length; inx++) {
          let temp = context.actionnode_deal.children[inx];
          if (temp.name == "gang") {
            gang = temp;
          }
          if (temp.name == "peng") {
            peng = temp;
          }
          if (temp.name == "chi") {
            chi = temp;
          }
          if (temp.name == "hu") {
            hu = temp;
          }
          temp.active = false;
        }
        if (data.gang) {
          gang.active = true;
        }
        if (data.peng) {
          peng.active = true;
        }
        if (data.chi) {
          chi.active = true;
        }
        if (data.hu) {
          hu.active = true;
        }
        context.actionnode_deal.active = true;
        context.setAction("deal", context);
      } else {
        var actionNum = 0;
        if (data.gang == true || data.peng == true || data.chi == true || data.hu == true) {
          let desk_script = context.actionnode_three.getComponent("DeskCards");
          desk_script.init(data.card);
          for (var inx = 0; inx < context.actionnode_three_list.children.length; inx++) {
            let temp = context.actionnode_three_list.children[inx];
            if (temp.name == "gang") {
              gang = temp;
            }
            if (temp.name == "peng") {
              peng = temp;
            }
            if (temp.name == "chi") {
              chi = temp;
            }
            if (temp.name == "hu") {
              hu = temp;
            }
            if (temp.name == "guo") {
              guo = temp;
            }
            temp.active = false;
          }
          if (data.gang) {
            gang.active = true;
            actionNum = actionNum + 1;
          }
          if (data.peng) {
            peng.active = true;
            actionNum = actionNum + 1;
          }
          if (data.chi) {
            chi.active = true;
            actionNum = actionNum + 1;
          }
          if (data.hu) {
            hu.active = true;
            actionNum = actionNum + 1;
          }
          if (data.deal == false) {
            guo.active = true;
            actionNum = actionNum + 1;
          }
          var posx = 1080 - (actionNum + 1) * 124;
          var actionevent = cc.moveTo(0.5, posx, -147);
          actionevent.easing(cc.easeIn(3.0));
          context.actionnode_three.runAction(actionevent);

          setTimeout(function() {
            if (context.action != null) {
              context.dealActionProcess(context);
            }
          }, 5000);
        }
      }
    }
  },

  selectaction_event: function(data, context) {
    console.error("-----有人碰杠吃--selectaction_event--------", data);
    cc.beimi.audio.playActionSound(data.action);
    let player = context.player(data.userid, context);
    /**
         * 杠碰吃，胡都需要将牌从 触发玩家的 桌牌 里 移除，然后放入当前玩家 桌牌列表里，如果是胡牌，则放到 胡牌 列表里，首先
         * 首先，需要找到触发对象，如果触发对象不是 all ， 则 直接找到 对象对应的玩家 桌牌列表，并找到 桌牌里 的最后 的 牌，
         * 然后将此牌 移除即可，如果对象是 all， 则不用做任何处理即可
         */
    if (cc.beimi.user.id == data.userid) {

      console.log("-------我自己碰杠吃胡-------");
      /**
             * 碰，显示碰的动画，
             * 杠，显示杠的动画，杠分为：明杠，暗杠，弯杠，每种动画效果不同，明杠/暗杠需要扣三家分，弯杠需要扣一家分
             * 胡，根据玩法不同，推倒胡和血流/血战
             */
      //碰的特效
      context.select_action_searchlight(data, context, player);

      if (data.action == "hu") {
        //胡牌了，把胡的牌放入到胡牌列表里，然后 ， 把当前的玩家的牌局置为不可点击
        let hu_card = cc.instantiate(context.takecards_one);
        let temp = hu_card.getComponent("DeskCards");
        temp.init(data.card);

        context.deskcards.push(hu_card);
        hu_card.setScale(0.5, 0.5);
        hu_card.parent = context.hu_cards_current;
        context.mask.active = true; //遮罩，不让操作了
        console.error("-------我自己胡了---");
      } else {
        /**
         * 杠后移除当前手牌，进入到 杠 列表里
         */
        for (var inx = 0; inx < context.playercards.length;) {
          let temp = context.playercards[inx].getComponent("HandCards");
          if (data.cardtype == temp.mjtype && data.cardvalue == temp.mjvalue) {
            context.cardpool.put(context.playercards[inx]);
            context.playercards.splice(inx, 1);
          } else {
            inx++;
          }
        }

        let cards_gang;
        /**
         * 刚和碰共用一个 Prefab，都是来自于 cards_gang_ming_prefab ，显示方式也相同， 区别在于：刚显示四张牌，碰显示两张牌
         */
        if (data.actype == "an") {
          cards_gang = cc.instantiate(context.cards_gang_an_prefab);
        } else {
          cards_gang = cc.instantiate(context.cards_gang_ming_prefab);
        }
        let temp_script = cards_gang.getComponent("GangAction");
        if (data.action == "gang") {
          temp_script.init(data.card, true);
        } else {
          temp_script.init(data.card, false);
        }

        if (data.action == "peng" || data.action == "chi") {
          /**
                     *
                     * 碰了以后的
                     */
          let temp = context.cards_panel.children[context.cards_panel.children.length - 1];
          if (temp != null) {
            let script = temp.getComponent("HandCards");
            if (script != null) {
              script.lastone();
            }
          }
        }

        cards_gang.parent = context.gang_current;
        context.actioncards.push(cards_gang);

        for (var inx = 0; inx < context.deskcards.length; inx++) {
          var temp = context.deskcards[inx];
          if (temp != null) {
            var script = temp.getComponent("DeskCards");
            if (script != null && script.value == data.card) {
              temp.destroy();
              context.deskcards.splice(inx, inx + 1);
              break;
            }
          }
        }
      }

      context.exchange_state("nextplayer", context);

      /**
             * 隐藏 动作 按钮
             */
      context.exchange_state("action", context);
    } else {
      console.log("-------有其他玩家碰杠吃胡-------");
      //以下代码是用于找到 杠/碰/吃/胡牌的 目标牌  ， 然后将此牌 从 桌面牌中移除
      if (data.target!='all') {  //暗杠
        let temp = context.player(data.target, context),
          deskcardpanel;
        if (temp.tablepos == "right") {
          deskcardpanel = context.deskcards_right_panel;
        } else if (temp.tablepos == "left") {
          deskcardpanel = context.deskcards_left_panel;
        } else if (temp.tablepos == "top") {
          deskcardpanel = context.deskcards_top_panel;
        }else {
          deskcardpanel = context.deskcards_current_panel;
        }
        if (deskcardpanel && deskcardpanel.children.length > 0) {
          deskcardpanel.children[deskcardpanel.children.length - 1].destroy();
        }

      }

      //提示当前玩家有人杠/碰/吃/胡牌
      let actionPlayer = context.player(data.userid, context);
      context.exchange_searchlight(actionPlayer.tablepos,context);
      console.log("点击了碰杠吃的玩家---》",actionPlayer);
      let cards_gang;
      let  actionPlayerDeskcardpanel;
      if (actionPlayer.tablepos == "right") {

        let rightpre;
        if (data.action=="peng") {
          rightpre = cc.instantiate(context.action_peng_prefab);

          if (deskcardpanel && deskcardpanel.children.length > 0) {
            deskcardpanel.children[deskcardpanel.children.length - 1].destroy();
          }

        }else if (data.action=="chi") {
          rightpre = cc.instantiate(context.action_chi_prefab);
        }else if(data.action == "gang") {
          if (data.actype=="an") {
          rightpre =  cc.instantiate(context.action_gang_ming_prefab);
          }else {
            rightpre =  cc.instantiate(context.action_gang_an_prefab);
          }
        }
        rightpre.parent = context.deskcards_right_panel.parent;

        if (data.actype == "an") {
          cards_gang = cc.instantiate(context.cards_gang_an_right_prefab);
        } else {
          cards_gang = cc.instantiate(context.cards_gang_ming_right_prefab);
        }
        let temp_script = cards_gang.getComponent("GangAction");
        if (data.action == "gang") {
          temp_script.init(data.card, true);
        } else {
          temp_script.init(data.card, false);
        }
        cards_gang.parent = context.gang_right;
        context.rightactioncards.push(cards_gang);
      } else if (actionPlayer.tablepos == "left") {
        let leftpre;
        if (data.action=="peng") {
          leftpre = cc.instantiate(context.action_peng_prefab);
        }else if (data.action=="chi") {
          leftpre = cc.instantiate(context.action_chi_prefab);
        }else if(data.action == "gang") {
          if (data.actype=="an") {
          leftpre =  cc.instantiate(context.action_gang_ming_prefab);
          }else {
            leftpre =  cc.instantiate(context.action_gang_an_prefab);
          }
        }
        leftpre.parent = context.deskcards_left_panel.parent;
        if (data.actype == "an") {
          cards_gang = cc.instantiate(context.cards_gang_an_left_prefab);
        } else {
          cards_gang = cc.instantiate(context.cards_gang_ming_left_prefab);
        }
        let temp_script = cards_gang.getComponent("GangAction");
        if (data.action == "gang") {
          temp_script.init(data.card, true);
        } else {
          temp_script.init(data.card, false);
        }
        cards_gang.parent = context.gang_left;
        context.leftactioncards.push(cards_gang);
      } else if (actionPlayer.tablepos == "top") {
        let toppre;
        if (data.action=="peng") {
          toppre = cc.instantiate(context.action_peng_prefab);
        }else if (data.action=="chi") {
          toppre = cc.instantiate(context.action_chi_prefab);
        }else if(data.action == "gang") {
          if (data.actype=="an") {
          toppre =  cc.instantiate(context.action_gang_ming_prefab);
          }else {
            toppre =  cc.instantiate(context.action_gang_an_prefab);
          }
        }
        toppre.parent = context.deskcards_top_panel.parent;


        if (data.actype == "an") {
          cards_gang = cc.instantiate(context.cards_gang_an_top_prefab);
        } else {
          cards_gang = cc.instantiate(context.cards_gang_ming_top_prefab);
        }
        let temp_script = cards_gang.getComponent("GangAction");
        if (data.action == "gang") {
          temp_script.init(data.card, true);
        } else {
          temp_script.init(data.card, false);
        }
        cards_gang.parent = context.gang_top;
        context.topactioncards.push(cards_gang);
      }

    }
  },
  /**
     * 接收发牌信息，需要根据玩家位置确定是哪家的牌
     * @param data
     * @param context
     */
  play_event: function(data, context,isRecovery) {
    cc.beimi.gamestatus = "playing";
    //改变状态，开始发牌
    context.exchange_state("begin", context);

    cc.beimi.audio.beginGame();
    var temp_player = data.player;
    var cards = context.decode(temp_player.cards);
    var laizis = [];
    if (temp_player.powerfull != null) {
      laizis = context.decode(temp_player.powerfull);
    }
    context.laiziValues = laizis;
    //显示剩余牌数
    setTimeout(function() {
      context.calcdesc_cards(context, 136, data.deskcards);
      context.ju.string = data.currentnum + "/" + data.numofgames + "局"
    }, 0);

    var groupNums = 0;
    for (var times = 0; times < 4; times++) {
      //初始化当前玩家数据
      context.initMjCards(groupNums, context, cards, temp_player.banker, laizis); //庄家banker
      //初始化其他玩家数据》》
      var inx = 0;
      for (var i = 0; i < data.players.length; i++) {
        if (data.players[i].playuser != cc.beimi.user.id) {
          context.initPlayerHandCards(groupNums, data.players[inx++].deskcards, inx, context, false);
        }
      }
      groupNums = groupNums + 1;
    }

    let ani = context.cards_panel.getComponent(cc.Animation);
    ani.play("majiang_reorder");

    var maxvalue = -2000;
    var maxvalluecard;
    for (var i = 0; i < context.playercards.length; i++) {
      let temp_script = context.playercards[i].getComponent("HandCards");
      if (temp_script.value >= 0) {
        context.playercards[i].zIndex = temp_script.value;
      } else {
        context.playercards[i].zIndex = 200 + temp_script.value;
      }
      //是赖子
      if (temp_script.laizi.active) {
        context.playercards[i].zIndex = temp_script.value - 1000;
      }

      //是扣的牌
      if(temp_script.koucard){
        context.playercards[i].zIndex = temp_script.value - 1000;
      }

      if (context.playercards[i].zIndex > maxvalue) {
        maxvalue = context.playercards[i].zIndex;
        maxvalluecard = context.playercards[i];
      }
    }
    /**
         * 重新排序
         */
    context.layout(context.cards_panel, function(fir, sec) {
      return fir.zIndex - sec.zIndex;
    });

    setTimeout(function() {
      if (temp_player.banker == true && maxvalluecard != null) {
        maxvalluecard.getComponent("HandCards").lastone();
      }
    }, 200);

    context.exchange_state("play", context);

    /**
         * 统一处理排序 的动画
         */

    /**
         * 初始化状态，首个玩家加入，然后开始等待其他玩家 ， 如果是 恢复数据， 则不会进入
         */
    //this.statusbtn.active = true ;

    context.showLaizi(context, laizis);

    //提示扣牌
    if (context.isCankou&&(data.playway=="2"||data.playway==2)&&!isRecovery&&cards.length==4) {
      context.selectkou_dialog.active = true;
      context.timer(context, 30, true);
    }else if ((!context.isCankou||cards.length!=4 )&&(data.playway=="2"||data.playway==2)&&!isRecovery) {
      let socket = context.socket();
      socket.emit("answerKou","0");
    }
  },
  /**
     * 开始定缺
     * @param data
     * @param context
     */
  selectcolor_event: function(data, context) {
    for (var inx = 0; inx < context.playersarray.length; inx++) {
      let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
      if (temp.data.id == cc.beimi.user.id) {
        temp.selecting();
      }
    }

    context.exchange_state("selectcolor", context);
  },
  /**
     * 通知定缺结果
     * @param data
     * @param context
     */
  selectresult_event: function(data, context) {
    for (var inx = 0; inx < context.playersarray.length; inx++) {
      let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
      if (temp.data.id == data.userid) {
        temp.selectresult(data);
        break;
      }
    }
    if (data.userid == cc.beimi.user.id) {
      context.exchange_state("selectresult", context);
      if (data.color < 10) {
        context.changecolor(data, context);
      }
    }
  },
  /**
     * 开始打牌，状态标记
     * @param data
     * @param context
     */
  lasthands_event: function(data, context) {
    if (data.userid == cc.beimi.user.id) { //该我出牌 , 庄家出牌，可以不用判断是否庄家了 ，不过，庄家数据已经传过来了
      context.exchange_state("lasthands", context);
      context.exchange_searchlight("current", context);
    } else {
      context.exchange_state("otherplayer", context); //当前玩家出牌，计时器开始计时，探照灯照向该玩家
      for (var inx = 0; inx < context.playersarray.length; inx++) {
        let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
        if (temp.data.id == data.userid) {
          context.exchange_searchlight(temp.tablepos, context);;
          break;
        }
      }
    }
  },

  changecolor: function(data, context) {
    let lastcard;
    for (var inx = 0; inx < context.playercards.length; inx++) {
      let temp = context.playercards[inx].getComponent("HandCards");
      if (temp.laizi.active) {
        console.log("----是赖子选色不参与排序----");
        return;
      }
      temp.relastone();
      if (parseInt(temp.value / 36) == data.color && temp.value >= 0) {
        temp.selected();
        context.playercards[inx].zIndex = 1000 + temp.value;
        if (lastcard == null || lastcard.zIndex < context.playercards[inx].zIndex) {
          lastcard = context.playercards[inx];
        }
      }
    }
    /**
         * 重新排序
         */
    context.layout(context.cards_panel, function(fir, sec) {
      return fir.zIndex - sec.zIndex;
    });

    if (data.banker == cc.beimi.user.id && lastcard != null) {
      let temp = lastcard.getComponent("HandCards");
      temp.lastone();
    }
  },

  /**
     * 显示 剩余牌
     * @param start
     * @param end
     */
  calcdesc_cards: function(context, start, end) {
    start = start - 1;
    if (start > end) {
      context.desk_cards.string = start;
      setTimeout(function() {
        context.calcdesc_cards(context, start, end);
      }, 15);
    }
  },

  initDealHandCards: function(context, data) {
    let temp = context.cardpool.get();
    let temp_script = temp.getComponent("HandCards");
    context.playercards.push(temp);
    temp_script.init(data.card, context.laiziValues);
    temp_script.lastone();

    //这里修改了 去掉选色功能了
    // if (parseInt(data.card / 36) == data.color && data.card >= 0) {
    //   temp_script.selected();
    // }

    temp.zIndex = 2000; //直接放到最后了，出牌后，恢复 zIndex
    temp.parent = context.cards_panel; //庄家的最后一张牌
  },

  /**
     * 初始化其他玩家手牌，
     * @param groupNums
     * @param deskcards
     * @param inx
     * @param context
     * @param spec 是否特殊的牌，即刚抓起来的牌
     */
  initPlayerHandCards: function(groupNums, deskcards, inx, context, spec) {
    let parent = context.right_panel;
    let cardarray = context.rightcards;
    let prefab = context.cards_right;

    if (inx == 1) {
      parent = context.top_panel;
      cardarray = context.topcards;
      prefab = context.cards_top;
    } else if (inx == 2) {
      parent = context.left_panel;
      cardarray = context.leftcards;
      prefab = context.cards_left;
    }
    context.initOtherCards(groupNums, context, deskcards, prefab, cardarray, parent, spec, inx); //左侧，
  },

  initOtherCards: function(group, context, cards, prefab, cardsarray, parent, spec, inx) {
    for (var i = group * 4; i < cards && i < (group + 1) * 4; i++) {
      //let temp = context.cardpool.get();
      //temp.parent = parent ;
      let temp = cc.instantiate(prefab);
      let temp_script = temp.getComponent("SpecCards");
      temp_script.init(spec, inx);

      temp.parent = parent;
      cardsarray.push(temp);
    }
  },

  initMjCards: function(group, context, cards, banker, laizis) {
    for (var i = group * 4; i < cards.length && i < (group + 1) * 4; i++) {
      let temp = context.cardpool.get();
      let temp_script = temp.getComponent("HandCards");
      context.playercards.push(temp);
      temp_script.init(cards[i], laizis);
      if (banker == true && i == (cards.length - 1)) {
        temp.parent = context.one_card_panel; //庄家的最后一张牌
      } else {
        temp.parent = context.cards_panel;
      }

      setTimeout(function() {
        temp.parent = context.cards_panel;
      }, 200);
    }
  },

//左上角赖子展示
  showLaizi: function(context, cards) {
    context.laiziNode.active = true;
    for (var i = 0; i < cards.length; i++) {
      var temp;
      if (context.laizicardpool.size() > 0) { // 通过 size 接口判断对象池中是否有空闲的对象
        temp = context.laizicardpool.get();
      } else { // 如果没有空闲对象，也就是对象池中备用对象不够时，我们就用 cc.instantiate 重新创建
        temp = cc.instantiate(this.enemyPrefab);
      }
      let temp_script = temp.getComponent("HandCards");
      temp_script.init(cards[i]);
      context.laizicards.push(temp);
      temp.parent = context.laiziNode;
    }
    let ani = context.laiziNode.getComponent(cc.Animation);
    ani.play("laizi_to_top");
  },

  /**
     * 回收系统资源，用于清理资源
     * @param context
     */
  collect: function(context) {
    for (var i = 0; i < context.playersarray.length;) {
      let player = context.playersarray[i];
      var playerscript = player.getComponent("MaJiangPlayer");
      if (playerscript.data.id != cc.beimi.user.id) { //当前 玩家不回收，最终 Destroy 的时候会被回收
        context.playerspool.put(player);
        context.playersarray.splice(i, 1);
      } else {
        i++;
      }
    }
  },

  /**
     * 按钮操作，点击 开始游戏按钮后的触发动作，进入计时，然后等待服务端推送数据和 状态机流程流转
     */
  waittingForPlayers: function() {
    this.exchange_state("ready", this);
  },

  player: function(pid, context) {
    let player;
    for (var inx = 0; inx < context.playersarray.length; inx++) {
      let temp = context.playersarray[inx].getComponent("MaJiangPlayer");
      if (temp.data.id == pid) {
        player = temp;
        break;
      }
    }
    return player;
  },
  /**
     * 状态切换，使用状态参数 切换，避免直接修改 对象状态，避免混乱
     */
  exchange_state: function(state, object) {
    let readybtn = null,
      waitting = null,
      selectbtn = null,
      banker = null,
      invitefriendsbtn = null;

    for (var i = 0; i < object.statebtn.children.length; i++) {
      let target = object.statebtn.children[i];
      if (target.name == "readybtn") {
        readybtn = target;
      } else if (target.name == "invitefriends") {
        invitefriendsbtn = target;
      } else if (target.name == "waitting") {
        waitting = target;
      } else if (target.name == "select") {
        selectbtn = target;
      } else if (target.name == "banker") {
        banker = target;
      }
      target.active = false;
    };

    console.log("-----exchange_state---------", state);
    switch (state) {
      case "init":
        object.desk_tip.active = false;
        object.laiziNode.active = false;
        if (object.playersarray.length < 4) {
          invitefriendsbtn.active = true;
          readybtn.active = false;
        } else {
          readybtn.active = true;
          invitefriendsbtn.active = false;
        }
        object.actionnode_deal.active = false;
        /**
                 * 探照灯 熄灭
                 */
        object.exchange_searchlight("none", object);

        break;
      case "ready":
        object.laiziNode.active = false;
        waitting.active = true;
        console.log("-----ready---------");
        if (cc.beimi.data != null && cc.beimi.data.enableai == true) {
          object.timer(object, cc.beimi.data.waittime);
        } else {
          object.timer(object, cc.beimi.data.noaiwaitime);
        }
        break;
      case "begin":
        waitting.active = false;
        object.laiziNode.active = false;
        /**
                 * 显示 当前还有多少张底牌
                 * @type {boolean}
                 */
        object.desk_tip.active = true;
        /**
                 * 开始发牌动画，取消所有进行中的计时器
                 */
        object.canceltimer(object);

        break;
      case "play":
        /**
                 * 一个短暂的状态，等待下一步指令是 定缺 还是直接开始打牌 ， 持续时间的计时器是 2秒
                 */
        object.timer(object, 2);
        break;
      case "selectcolor":
        /**
                 * 定缺 ，由服务端确定是否有此个节点，下个版本将会实现流程引擎控制 游戏 节点，一切都在服务端 进行配置工作
                 * @type {boolean}
                 */
        object.exchange_searchlight("current", object);
        selectbtn.active = true;
        object.timer(object, 5);
        break;

      case "selectresult":
        /**
                 * 选择了定缺结果，关闭选择按钮
                 * @type {boolean}
                 */
        selectbtn.active = false;
        object.canceltimer(object);
        break;
      case "lasthands":
        /**
                 * 选择了定缺结果，关闭选择按钮
                 * @type {boolean}
                 */
        banker.active = true;
        /**
                 * 计时器方向
                 */
        object.timer(object, 8);
        break;
      case "otherplayer":
        /**
                 * 计时器方向
                 */
        object.timer(object, 8);
        break;
      case "takecard":
        /**
                 * 选择了定缺结果，关闭选择按钮
                 * @type {boolean}
                 */
        banker.active = false;
        //object.canceltimer(object) ;
        break;
      case "action":
        /**
                 * 隐藏 杠碰吃胡 等 操作
                 */
        object.dealActionProcess(object);
        break;
      case "nextplayer":
        /**
                 * 选择了定缺结果，关闭选择按钮
                 * @type {boolean}
                 */
        object.timer(object, 8);
        break;
      case "allcards":
        /**
                 * 都打完了，结束了，回收计时器，回收定缺，回收庄家
                 * @type {boolean}
                 */
        for (var i = 0; i < object.playersarray.length; i++) {
          let player = object.playersarray[i];
          var playerscript = player.getComponent("MaJiangPlayer");
          playerscript.clean();
        }
        object.canceltimer(object);
        break;
    }
  },

  exchange_searchlight: function(direction, context) {
    for (var inx = 0; inx < context.searchlight.children.length; inx++) {
      if (direction == context.searchlight.children[inx].name) {
        context.searchlight.children[inx].active = true;
      } else {
        context.searchlight.children[inx].active = false;
      }
    }
  },

  dealActionProcess: function(object) {
    console.log("-------dealActionProcess--------");
    var actionevent = cc.moveTo(0.5, 1080, -147);
    actionevent.easing(cc.easeIn(3.0));

    object.actionnode_three.runAction(actionevent);

    if (object.action == "deal") {
      object.actionnode_deal.active = false;
    }
    object.action = null;
  },

  canceltimer: function(object) {
    object.unscheduleAllCallbacks();
    object.mjtimer.string = "00";
  },

  recovery: function() {
    //this.initgame();
  },
  timer: function(object, times, isCancelKouCard) {
    if (times > 9) {
      object.mjtimer.string = times;
    } else {
      object.mjtimer.string = "0" + times;
    }

    object.callback = function() {
      times = times - 1;
      if (times >= 0) {
        let text = times;
        if (times < 10) {
          text = "0" + times;
        }
        object.mjtimer.string = text;
      }
      if (isCancelKouCard&&times==0) {
        console.log("发送不扣的通知");
        object.selectkou_dialog.active = false;
        let socket = this.socket();
        socket.emit("answerKou","0");
        object.isCankou = false;
      }
    }
    object.unscheduleAllCallbacks();
    /**
         * 启动计时器，应该从后台传入 配置数据，控制 等待玩家 的等待时长
         */
    object.schedule(object.callback, 1, times, 0);
  },

  clean: function() {
    /**
         * 销毁玩家数据
         */
    for (var i = 0; i < this.playercards.length; i++) {
      this.playercards[i].destroy();
    }
    this.playercards.splice(0, this.playercards.length);

    //销毁赖子牌
    for (var i = 0; i < this.laizicards.length; i++) {
      this.laizicards[i].destroy();
    }

    this.laizicards.splice(0, this.laizicards.length);

    /**
         * 销毁桌面上已打出的牌
         */
    for (var i = 0; i < this.deskcards.length; i++) {
      this.deskcards[i].destroy();
    }
    this.deskcards.splice(0, this.deskcards.length);
    /**
         * 销毁左侧玩家的手牌
         */
    for (var i = 0; i < this.leftcards.length; i++) {
      this.leftcards[i].destroy();
    }
    this.leftcards.splice(0, this.leftcards.length);
    /**
         * 销毁右侧玩家的手牌
         */
    for (var i = 0; i < this.rightcards.length; i++) {
      this.rightcards[i].destroy();
    }
    this.rightcards.splice(0, this.rightcards.length);
    /**
         * 销毁对家的手牌
         */
    for (var i = 0; i < this.topcards.length; i++) {
      this.topcards[i].destroy();
    }
    this.topcards.splice(0, this.topcards.length);

    //销毁碰杠吃的牌
    for (var i = 0; i < this.actioncards.length; i++) {
      this.actioncards[i].destroy();
    }
    this.actioncards.splice(0, this.actioncards.length);

    //销毁左边玩家碰杠吃的牌
    for (var i = 0; i < this.leftactioncards.length; i++) {
      this.leftactioncards[i].destroy();
    }
    this.leftactioncards.splice(0, this.leftactioncards.length);

    //销毁右边玩家碰杠吃的牌
    for (var i = 0; i < this.rightactioncards.length; i++) {
      this.rightactioncards[i].destroy();
    }
    this.rightactioncards.splice(0, this.rightactioncards.length);

    //销毁上边玩家碰杠吃的牌
    for (var i = 0; i < this.topactioncards.length; i++) {
      this.topactioncards[i].destroy();
    }
    this.topactioncards.splice(0, this.topactioncards.length);

    /**
         * 玩家数据销毁条件（房间解散，或者有玩家退出房价的时候，所有玩家数据销毁后冲洗排序）
         */
    this.mask.active = false;

    if (this.summarypage) {
      this.summarypage.destroy();
    }

  },

  restart: function() {
    /**
         * 清理桌面
         */
    this.clean();
    /**
         * 初始化桌面
         */
    if (this.gameover == true) {
      for (var inx = 0; inx < this.player.length; inx++) {
        this.player[inx].destroy();
      }
      this.player.splice(0, this.player.length); //房间解散，释放资源
      this.player = new Array();

      this.initdata(true);
    } else {
      this.initdata(false);
    }

    /**
         * 系统资源回收完毕，发送一个 重新开启游戏的 通知
         */
    if (this.ready()) {
      let socket = this.socket();
      socket.emit("restart", "restart");
    }
  },
  /**
     * 开始游戏
     */
  startgame: function() {
    if (this.ready()) {
      let socket = this.socket();
      socket.emit("start", "true");
      this.waittingForPlayers();
    }

  },

  onDestroy: function() {
    // if(this.ready()) {
    //     let socket = this.socket();
    //     socket.disconnect();
    // }
    this.inited = false;
    this.cleanmap();
    this.cleanTingTip(this);
    if (this.ready()) {
      let socket = this.socket();
      socket.emit("leave", "leave");
    }
  },
  // called every frame, uncomment this function to activate update callback
  // update: function (dt) {

  // },

  cleanTingTip: function(context){
    if (context.ting_tip.active) {
      context.ting_tip.active = false;
      if (context.huactioncardstip&&context.huactioncardstip.length>0) {
        for (var inx = 0; inx < context.huactioncardstip.length; inx++) {
          context.huactioncardstip[inx].destroy();
        }
      }
    }
  }
});

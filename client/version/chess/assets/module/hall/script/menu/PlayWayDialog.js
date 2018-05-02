cc.Class({
  extends: cc.Component,
  properties: {
    playwayselect: {
      default: null,
      type: cc.Node
    },
    playwayunselect: {
      default: null,
      type: cc.Node
    },
    scoreselect: {
      default: null,
      type: cc.Node
    },
    scoreunselect: {
      default: null,
      type: cc.Node
    },

    playwaybgselect: {
      default: null,
      type: cc.Node
    },
    playwaybgunselect: {
      default: null,
      type: cc.Node
    },
    scorebgselect: {
      default: null,
      type: cc.Node
    },
    scorebgunselect: {
      default: null,
      type: cc.Node
    },

    playwayScrollNode:{
      default: null,
      type: cc.Node
    },

    scoreScrollNode:{
      default: null,
      type: cc.Node
    },

    playway2ScrollNode:{
      default: null,
      type: cc.Node
    },

    scoreScrollNode:{
      default: null,
      type: cc.Node
    },


    playwayScrollview:{
      default: null,
      type: cc.ScrollView
    },

    scoreScrollview:{
      default: null,
      type: cc.ScrollView
    },

   scoreItemPrefab:{
     default : null ,
     type : cc.Prefab
   },


    laiyuanselectbg:{
      default: null,
      type: cc.Node
    },

    laiyuanunselectbg:{
      default: null,
      type: cc.Node
    },

    koudajiangselectbg:{
      default: null,
      type: cc.Node
    },

    koudajiangunselectbg:{
      default: null,
      type: cc.Node
    },

    playwaytitle:{
      default: null,
      type: cc.Label
    }

  },

  onLoad: function () {
    this.topTag =0; //0是玩法 1是历史记录
    this.playwayTag = 0; //0是涞源  1 是扣大将
  },


  init(tag,data){
    let self = this;
    this.scoreList = data;
    this.playwayScrollNode.active=false;
    this.playway2ScrollNode.active=false;
    console.log("----------init-------------",tag);
    if (tag==0) {
      this.selectPlayWay(self);
    }else {
      this.selectScore(self);
    }
  },

  selectPlayWay(){
    this.topTag =0;
    console.log("=========selectPlayWay==========");
    if (this.playwayTag ==0) {
      this.playwayScrollNode.active=true;
      this.playway2ScrollNode.active=false;
    }else {
      this.playwayScrollNode.active=false;
      this.playway2ScrollNode.active=true;
    }

    // this.playwayselect.active=true;
    // this.playwaybgselect.active=true;
    // this.playwayunselect.active=false;
    // this.playwaybgunselect.active=false;

    // this.scoreselect.active=false;
    // this.scorebgselect.active=false;
    // this.scoreunselect.active=true;
    // this.scorebgunselect.active=true;

    this.scoreScrollNode.active=false;
  },

  selectScore(){
    this.topTag =1;
    console.log("=========selectScore==========");
    this.playwayselect.active=false;
    this.playwaybgselect.active=false;
    this.playwayunselect.active=true;
    this.playwaybgunselect.active=true;

    this.scoreselect.active=true;
    this.scorebgselect.active=true;
    this.scoreunselect.active=false;
    this.scorebgunselect.active=false;

    this.playwayScrollNode.active=false;
    this.playway2ScrollNode.active=false;
    this.scoreScrollNode.active=true;

    this.items = [];

    for (let i = 0; i <10; ++i) { // spawn items, we only need to do this once
    		let item = cc.instantiate(this.scoreItemPrefab);
        console.log("---this.scoreScrollview------",this.scoreScrollview);
    		this.scoreScrollview.content.addChild(item);
    		item.setPosition(0, -item.height * (0.5 + i) - 30 * (i + 1));
    		// item.getComponent('Item').updateItem(i, i);
        this.items.push(item);
    	}

  },

  selectLaiyuan(){
    this.playwayTag=0;
    this.playwaytitle.string="来源玩法";
    this.laiyuanselectbg.active = true;
    this.laiyuanunselectbg.active = false;
    this.koudajiangselectbg.active = false;
    this.koudajiangunselectbg.active = true;
    if (this.topTag) {
        console.log("切换来源的历史记录");
    }else {
      this.playwayScrollNode.active=true;
      this.playway2ScrollNode.active=false;
    }

  },

  selectKouDaJiang(){
    this.playwaytitle.string="扣大将玩法";
    this.playwayTag=1;
    this.laiyuanselectbg.active = false;
    this.laiyuanunselectbg.active = true;
    this.koudajiangselectbg.active = true;
    this.koudajiangunselectbg.active = false;
    if (this.topTag) {
       console.log("切换扣大将的历史记录");
    }else {
      this.playwayScrollNode.active=false;
      this.playway2ScrollNode.active=true;
    }
  }

  // let socket = this.socket();
  // socket.emit("getPlayhistory", "");
  // socket.on("getPlayhistory", function(result) {
  //   console.log("getPlayhistory=====>", JSON.stringify(result));

  // update (dt) {},
});

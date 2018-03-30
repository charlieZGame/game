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
   }


  },

  onLoad: function () {
    let self = this;

  },

  init(tag){
    let self = this;
    console.log("----------init-------------",tag);
    if (tag==0) {
      this.selectPlayWay(self);
    }else {
      this.selectScore(self);
    }
  },

  selectPlayWay(){
    console.log("=========selectPlayWay==========");
    this.playwayselect.active=true;
    this.playwaybgselect.active=true;
    this.playwayunselect.active=false;
    this.playwaybgunselect.active=false;

    this.scoreselect.active=false;
    this.scorebgselect.active=false;
    this.scoreunselect.active=true;
    this.scorebgunselect.active=true;

    this.playwayScrollNode.active=true;
    this.scoreScrollNode.active=false;
  },

  selectScore(){
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

  }



  // update (dt) {},
});

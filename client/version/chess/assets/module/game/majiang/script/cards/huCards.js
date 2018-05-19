cc.Class({
    extends: cc.Component,
    properties: {
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
        beimi0: {
            default: null,
            type: cc.SpriteAtlas
        },
        cardvalue:{
            default: null,
            type: cc.Node
        },
        target:{
            default: null,
            type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        this.node.on('mousedown', function ( event ) {
            console.log('Hello!');
        });
        this.node.on('mousemove', function ( event ) {
            console.log('Hello Mover!');
        });
    },

    init:function(cvalue){
        this.value = cvalue ;
        let cardframe ;
        let cardcolors = parseInt(this.value/4 ) ;
        let cardtype  = parseInt(cardcolors / 9);

        this.mjtype = cardtype ;
        this.mjvalue = parseInt((this.value%36)/4 ) ;

        let deskcard ;
        this.lastonecard = false;
        if(cardcolors < 0){
            deskcard = "wind"+(cardcolors + 8) ; //东南西北风 ， 中发白
        }else{
            if(cardtype == 0){ //万
                deskcard = "wan"+ (parseInt((this.value%36)/4)+1) ;
            }else if(cardtype == 1){ //筒
                deskcard = "tong"+ (parseInt((this.value%36)/4)+1) ;
            }else if(cardtype == 2){  //条
                deskcard = "suo"+ (parseInt((this.value%36)/4)+1) ;
            }
        }
        if(deskcard == "suo2"){
            cardframe = this.beimi0.getSpriteFrame('牌面-'+deskcard);
        }else{
            cardframe = this.atlas.getSpriteFrame('牌面-'+deskcard);
        }
        this.cardvalue.getComponent(cc.Sprite).spriteFrame = cardframe;
    }

});

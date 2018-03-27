var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,
    properties: {
        grouptitle:{
            default:null ,
            type : cc.Label
        },
        groupbox:{
            default:null ,
            type : cc.Node
        },
        groupbox_four:{
            default:null ,
            type : cc.Node
        },
        content:{
            default:null ,
            type : cc.Node
        },
        itemname:{
            default:null ,
            type : cc.Label
        },

        checkboxlayout:{
            default:null ,
            type : cc.Node
        },

        checkbox:{
            default:null ,
            type : cc.Node
        },

        checkboxnode:{
            default:null ,
            type : cc.Node
        },

        radiolayout:{
            default:null ,
            type : cc.Node
        },

        radioselect:{
            default:null ,
            type : cc.Node
        },

    },

    // use this for initialization
    onLoad: function () {
        let self = this ;
        this.node.on('checkbox', function (event) {
            if(self.checkbox!=null){
                if(self.checked == false){
                    if(self.data.type == "radio"){
                        for(var inx = 0 ; inx < self.options.length ; inx++){
                            let script = self.options[inx] ;
                            script.doUnChecked() ;
                        }
                    }
                    self.doChecked();
                }else{
                    if(self.data.type == "radio"){
                        for(var inx = 0 ; inx < self.options.length ; inx++){
                            let script = self.options[inx] ;
                            script.doUnChecked() ;
                        }
                        self.doChecked();
                    }else{
                        self.doUnChecked();
                    }
                }
            }
            event.stopPropagation() ;
        });

        this.node.on('radio', function (event) {
            if(self.radiolayout!=null){
                if(self.checked == false){
                    if(self.data.type == "radio"){
                        for(var inx = 0 ; inx < self.options.length ; inx++){
                            let script = self.options[inx] ;
                            script.doUnChecked() ;
                        }
                    }
                    self.doChecked();
                }else{
                    if(self.data.type == "radio"){
                        for(var inx = 0 ; inx < self.options.length ; inx++){
                            let script = self.options[inx] ;
                            script.doUnChecked() ;
                        }
                        self.doChecked();
                    }
                }
            }
            event.stopPropagation() ;
        });
    },
    init:function(group , itempre , items , parentoptions){
        this.data = group ;
        this.options = parentoptions ;

        this.groupoptions = new Array();
        this.checked = false ;

        this.grouptitle.string = group.name ;
        if(this.groupbox!=null && itempre!=null){
            let itemsnum = 0 ;
            for(var inx=0 ; inx<items.length ; inx++){
                if(items[inx].groupid == group.id){
                    itemsnum = itemsnum + 1;
                    let newitem = cc.instantiate(itempre) ;
                    if(group.style != null && group.style == "three"){
                        newitem.parent = this.groupbox ;
                        this.groupbox_four.active = false ;
                        this.groupbox.active = true ;
                    }else{
                        newitem.parent = this.groupbox_four ;
                        this.groupbox_four.active = true;
                        this.groupbox.active = false;
                    }
                    let script = newitem.getComponent("PlaywayGroup") ;
                    this.groupoptions.push(script);
                    script.inititem(items[inx] , group , this.groupoptions);

                }
            }
            if(group.style != null && group.style == "three") {
                if (itemsnum > 4) {
                    this.content.height = 35 + 50 * (parseInt((itemsnum - 1) / 3) + 1);
                    this.groupbox.height = 50 * (parseInt((itemsnum - 1) / 3) + 1);
                }
            }else{
                if (itemsnum > 4) {
                    this.content.height = 35 + 50 * (parseInt((itemsnum - 1)/ 4) + 1);
                    this.groupbox_four.height = 50 * (parseInt((itemsnum - 1)/ 4) + 1);
                }
            }
        }
    },
    inititem:function(item , group , parentoptions){
        this.data = group ;
        this.item = item ;
        this.options = parentoptions;
        this.itemname.string = item.name ;
        /**
         * 以下代码修正 OPTION超出宽度导致 点击错误的 问题
         */
        if(group.style == "four"){
            this.itemname.node.width = 160 ;
            this.itemname.node.x = 107 ;
        }else{
            this.itemname.node.width = 105 ;
            this.itemname.node.x = 77 ;
        }
        if(item.defaultvalue == true){
            this.doChecked();
        }else{
            this.doUnChecked();
        }
        if(group!=null && group.style!=null && group.style == "three"){
            this.checkboxnode.x = -76 ;
        }
    },

    doChecked:function(){
        this.checked = true ;
        if(this.data.type == "radio"){
          this.checkboxlayout.active = false;
          this.radiolayout.active = true;
          this.radioselect.active = true ;
        }else {
            this.checkboxlayout.active = true;
            this.checkbox.active = true ;
            this.radiolayout.active = false;
        }

    },
    doUnChecked:function(){
        this.checked = false ;
        if(this.data.type == "radio"){
          this.checkboxlayout.active = false;
          this.radiolayout.active = true;
          this.radioselect.active = false ;
        }else {
            this.checkboxlayout.active = true;
            this.checkbox.active = false ;
            this.radiolayout.active = false;
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});

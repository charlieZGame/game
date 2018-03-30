"use strict";
cc._RF.push(module, '150633xX7VMZKFzr2J78fnQ', 'agreement');
// module/login/script/agreement.js

"use strict";

cc.Class({
    extends: cc.Component,

    properties: {
        user_agreement: {
            default: null,
            type: cc.Node
        },

        agreement_dialog: {
            default: null,
            type: cc.Node
        },

        close_agreement_dialog: {
            default: null,
            type: cc.Node
        },

        isAgreement: true
    },

    // LIFE-CYCLE CALLBACKS:

    onLoad: function onLoad() {
        this.user_agreement.active = true;
        this.agreement_dialog.active = false;
    },
    start: function start() {},


    onAgreement: function onAgreement() {

        this.user_agreement.active = !this.isAgreement;
        this.isAgreement = !this.isAgreement;
        console.log("onAgreement---------", this.user_agreement.active);
    },

    showAgreement: function showAgreement() {
        this.agreement_dialog.active = true;
    },

    hideAgreement: function hideAgreement() {
        this.agreement_dialog.active = false;
    }

    // update (dt) {},
});

cc._RF.pop();
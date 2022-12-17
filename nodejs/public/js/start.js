'use strict';

//const vConsole = new VConsole();
//const remoteConsole = new RemoteConsole("http://[remote server]/logio-post");
//window.datgui = new dat.GUI();

const base_url = "";

var vue_options = {
    el: "#top",
    mixins: [mixins_bootstrap],
    store: vue_store,
    data: {
        apikey: "",
        notification_send: {
            topic: 'fcm_notification'
        },
        item_list: [],
    },
    computed: {
    },
    methods: {
        notification_send_call: async function(){
            var result = await do_post_with_apikey(base_url + "/notification-push-message", this.notification_send, this.apikey);
            console.log(result);
            alert('送信しました。');
            this.get_all_items_call();
        },
        delete_all_items_call: async function(){
            if( !confirm('本当に全部削除しますか？') )
                return;
            var result = await do_post_with_apikey(base_url + "/notification-delete-allmessage", this.notification_send, this.apikey);
            console.log(result);
            alert('全削除しました。');
            this.get_all_items_call();
        },
        get_all_items_call: async function(){
            var params = {
                client_id: this.notification_send.client_id,
                topic: this.notification_send.topic,
                start: 0,
                end: new Date().getTime()
            };
            var result = await do_post_with_apikey(base_url + "/notification-get-list", params, this.apikey);
            console.log(result);
            this.item_list = result.rows;
        },
        input_clear: function(){
            this.notification_send.title = "";
            this.notification_send.body = "";
        },
        setup_apikey: function(){
            var apikey = prompt("ApiKeyを指定してください。", this.apikey);
            if( apikey ){
                this.apikey = apikey;
                localStorage.setItem("notification_apikey", this.apikey);
            }
        }
    },
    created: function(){
    },
    mounted: function(){
        proc_load();

        this.apikey = localStorage.getItem("notification_apikey");
        this.get_all_items_call();
    }
};
vue_add_data(vue_options, { progress_title: '' }); // for progress-dialog
vue_add_global_components(components_bootstrap);
vue_add_global_components(components_utils);

/* add additional components */
  
window.vue = new Vue( vue_options );

function do_post_with_apikey(url, body, apikey) {
    const headers = new Headers({ "Content-Type": "application/json", "X-API-KEY": apikey });
  
    return fetch(url, {
      method: 'POST',
      body: JSON.stringify(body),
      headers: headers
    })
    .then((response) => {
      if (!response.ok)
        throw new Error('status is not 200');
      return response.json();
    });
  }
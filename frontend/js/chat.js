import API from "./api.js"
import UserRequestDTO from "./dto/UserRequestDTO.js";
import MessageRequestDTO from "./dto/MessageRequestDTO.js";

const userNick = document.querySelector("#userNick");
const logoutForm = document.querySelector("#logoutForm");
const chatList = document.querySelector("#chatList");
const messagesBox = document.querySelector("#messagesBox");

const api = new API;

document.addEventListener("DOMContentLoaded", (e) =>{
    api.setUrl("/messages/conversations/" + localStorage.getItem("username"));

    api.fetchGet()
        .then(messages => {
            fillMessageList(messages);            
        })
        .catch(err => console.error(err));

    
    websocketConnect();
})


userNick.innerHTML = localStorage.getItem("username");

logoutForm.addEventListener("submit", (e) => {
    e.preventDefault();
    api.setUrl("/users/logout");

    let logoutData = new UserRequestDTO(localStorage.getItem("username"));

    api.fetchPut(logoutData);
    localStorage.clear();
    window.location.href = "../index.html";
})

function fillMessageList(messages){
    buildComponentMessageList(messages);
    setUpClassGetter(messages);
}

function fillMessageBox(messages){
    buildComponentMessageBox(messages);
    setUpIdGetter();
}

function setUpClassGetter(messages){
    const conversationList = document.querySelectorAll(`.messageList`)
    conversationList.forEach((conversation, i) => {
        conversation.addEventListener("click", (e) =>{
            localStorage.setItem("withUser", messages[i].withUser);
            
            apiGetFetch();
        })
    })
}

function apiGetFetch(){
    api.setUrl(`/messages/conversation?user1=${localStorage.getItem("username")}&user2=${localStorage.getItem("withUser")}`);

            api.fetchGet()
                .then(messages => {
                    fillMessageBox(messages);
                })
                .catch(err => console.error(err));
}

function buildComponentMessageList(messages){
    messages.forEach(message => {
        chatList.innerHTML += `
            <li  class="nav-item border-bottom d-flex p-2 messageList">
              <svg class="bi p-2 text-white" width="40" height="40" role="img" aria-label="Customers"><use xlink:href="#people-circle-black"/></svg>
                <div>
                  <p class="text-capitalize text-black rounded-0 m-0">
                    ${message.withUser}
                  </p>
                  <p class="text-truncate text-secondary m-0" style="max-width: 200px;">
                    ${message.lastMessage}
                  </p>
                </div>
            </li>
        `
    });
}

function buildComponentMessageBox(messages){
    messagesBox.innerHTML = "";

    messages.forEach(message => {
        messagesBox.innerHTML += `
        <div class="col-12">
            <div class="card">
                <div class="card-body rounded-4 bg-primary text-white align-items-end">
                  <p class="card-text">${message.text}</p>
                </div>
            </div>
        </div>
        `
    });

    messagesBox.innerHTML +=`
        <div" class="col-12 input-group">
            <input type="text" id="messagesInput" class="form-control" placeholder="Scrivi il messaggio...">
        </div>
    `
}

const socket = new SockJS("http://localhost:8080/ws");
const stompClient = Stomp.over(socket);
stompClient.debug = null;
let isConnected = false;

function setUpIdGetter(){
    const messageSendInput = document.querySelector("#messagesInput")
    messageSendInput.addEventListener("keydown", (e) => {
        if(e.key == "Enter"){
            const newMessage = new MessageRequestDTO(localStorage.getItem("username"), localStorage.getItem("withUser"), messageSendInput.value)

            // REST Method
            // api.setUrl("/message");
            
            // const response = api.fetchPost(newMessage);
            // console.log(response); 

            // WebSocket Method
            if(isConnected){
                stompClient.send("/app/chat.send", {}, JSON.stringify(newMessage))
                addMessagesToChat(newMessage)
            }

            messageSendInput.value = "";
        }
    })
}

function websocketConnect(){
    
    stompClient.connect({}, ()=>{
        isConnected = true;

        stompClient.subscribe("/topic/messages/" + localStorage.getItem("username"), (message) => {
            const body = JSON.parse(message.body);

            if(
                body.sender === localStorage.getItem("withUser") ||
                body.receiver === localStorage.getItem("withUser")
            ){
                addMessagesToChat(body);
            }
        });
    })

}

function addMessagesToChat(message){
    messagesBox.innerHTML += `
        <div class="col-12">
            <div class="card">
                <div class="card-body rounded-4 bg-primary text-white align-items-end">
                  <p class="card-text">${message.text}</p>
                </div>
            </div>
        </div>
        `
}
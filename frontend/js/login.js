import API from "./api.js"
import UserRequestDTO from "./dto/UserRequestDTO.js";

const loginForm = document.querySelector("#loginForm");

loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const api = new API;
    api.setUrl("/users/login");

    const login = new UserRequestDTO(document.querySelector("#loginUsername").value);

    try{
        const response = await api.fetchPut(login);
        localStorage.setItem("username", login.username);
        window.location.href = "../pages/chat.html";
        
    }catch(error){
        console.error(error);
    }
})

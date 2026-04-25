import API from "./api.js"
import UserRequestDTO from "./dto/UserRequestDTO.js";

const loginForm = document.querySelector("#loginForm");

loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const api = new API;
    api.setUrl("/user");

    const login = new UserRequestDTO(document.querySelector("#loginUsername").value);

    try{
        const response = await api.fetchPost(login);

        localStorage.setItem("username", login.username);
        window.location.href = "../index.html";
        
    }catch(error){
        console.error(error);
    }
})

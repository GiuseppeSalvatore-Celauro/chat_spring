export default class API{
    url = "";
    
    getUrl(){
        return this.url;
    }

    setUrl(url){
        this.url = `http://localhost:8080/api/chat${url}`;
    }

    async fetchPost(data){
        const rawResponse = await fetch(this.url, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-type" : "application/json"
            },
            body: JSON.stringify(data)
        });
        const content = await rawResponse.json();
        return content;
    }

    async fetchPut(data){
        const rawResponse = await fetch(this.url, {
            method: "PUT",
            headers: {
                "Accept": "application/json",
                "Content-type" : "application/json"
            },
            body: JSON.stringify(data)
        });
        const content = await rawResponse.json();
        return content;
    }

    async fetchGet(){
        const rawResponse = await fetch (this.url);
        const content = await rawResponse.json();
        return content;
        
    }
}
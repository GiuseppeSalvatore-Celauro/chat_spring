export default class MessageRequestDTO{
    constructor(sender, receiver, text){
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }
}
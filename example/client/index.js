import {io} from "socket.io-client";

const socket = io(`http://socketio:3000/`, {
  transports: ["websocket"],
});

socket.connect()

socket.on("connect", () => {
  console.log(`Client up and running`)
});

socket.onAny((topic, payload) => {
  console.log(`Received ${JSON.stringify(payload)} on ${topic}`);
});

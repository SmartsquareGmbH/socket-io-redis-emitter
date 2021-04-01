import {io} from "socket.io-client";

const port = process.argv[2]

const socket = io(`http://localhost:${port}/`, {
  transports: ["websocket"],
});

socket.connect()

socket.on("connect", () => {
  console.log(`Client [${port}]: Up and Running`)
});

socket.onAny((topic, payload) => {
  console.log(`Client [${port}] received ${JSON.stringify(payload)} on ${topic}`);
});

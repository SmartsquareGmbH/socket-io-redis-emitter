import redisAdapter from 'socket.io-redis'
import {createServer} from "http"
import {Server} from "socket.io"

const port = process.argv[2]
const httpServer = createServer()
const io = new Server(httpServer)

io.adapter(redisAdapter({host: 'redis', port: 6379}));

httpServer.listen(port, () => console.log(`Server [${port}]: Up and Running`))

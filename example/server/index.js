import cluster from "cluster"
import http from "http"
import { createClient } from "redis"
import { Server } from "socket.io"
import { createAdapter } from "@socket.io/redis-adapter"
import { setupMaster, setupWorker } from "@socket.io/sticky"

if (cluster.isPrimary) {
  console.log(`Primary ${process.pid} is running`)

  const httpServer = http.createServer()

  setupMaster(httpServer, {
    loadBalancingMethod: "least-connection", // either "random", "round-robin" or "least-connection"
  })

  httpServer.listen(3000, () => console.log(`Primary up and running`))

  for (let i = 0; i < 5; i++) {
    cluster.fork()
  }

  cluster.on("exit", (worker) => {
    console.log(`Worker ${worker.process.pid} died`)
  })
} else {
  console.log(`Worker ${process.pid} started`)

  const pubClient = createClient({ url: "redis://redis:6379" })
  const subClient = pubClient.duplicate()

  await Promise.all([
    pubClient.connect(),
    subClient.connect(),
  ])

  const httpServer = http.createServer()
  const io = new Server(httpServer)
  io.adapter(createAdapter(pubClient, subClient))

  setupWorker(io)
}

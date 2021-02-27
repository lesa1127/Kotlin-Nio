import socket
from threading import Thread
import time
from multiprocessing import Process


def run(*arg) -> None:
    client = socket.socket()
    client.connect(("127.0.0.1", 8881))
    buff = bytearray(4096)
    reader = Rec(client, arg[0])
    reader.start()
    while True:
        client.send(buff)
        # time.sleep(0.05)
        pass
    pass


class Rec(Thread):

    def __init__(self, client: socket = ...,id:str=...) -> None:
        super().__init__()
        self.lastReadTime = time.time()
        self.client = client
        self.readlen = 0
        self.threadId = id

    def run(self) -> None:
        super().run()

        self.lastReadTime = time.time()
        while True:
            self.readlen += len(self.client.recv(4096))
            now = time.time()
            sec = now - self.lastReadTime
            if sec > 1.0:
                self.lastReadTime = now
                print("%s速度:%s mb/s\n" %( self.threadId,str(self.readlen / 1024 / 1024 / sec)))
                self.readlen = 0
            pass


pass

if __name__ == "__main__":
    procpool = []
    # 进程数量
    for i in range(1024):
        tmp = Process(target=run,args=("Thread-ID:%s"%str(i),))
        tmp.start()
        procpool.append(tmp)

    while True:
        txt = input("q 退出")
        if txt == "q":
            break

    for i in procpool:
        i.kill()

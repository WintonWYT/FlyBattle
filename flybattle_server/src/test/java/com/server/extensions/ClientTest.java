package com.server.extensions;

import com.baitian.mobileserver.buffer.IoBuffer;
import com.baitian.mobileserver.util.MsgConstants;
import com.server.protobuf.response.ResultResp;
import com.server.protobuf.response.SyncResp;
import com.server.util.ClientMessageCoderTest;
import com.server.util.IHandler;
import com.server.util.chiper.ChiperFactory;
import com.server.util.chiper.IMessageCipher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public class ClientTest extends Thread {

    private static final Random RANDOM = new Random();
    private SocketChannel channel;
    private Selector selector;
    private ByteBuffer byteBuffer;
    private IHandler handler;
    private IMessageCipher chiper;
    private volatile boolean running;
    private int msgSeqId;
    private int tag;
    private static int num = 0;

    public ClientTest(String host, int port, IHandler handler) {
        this.handler = handler;
        try {
            this.init(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.chiper = ChiperFactory.createMessageCipher("default");
    }

    private void init(String host, int port) throws IOException {
        channel = SocketChannel.open();
        selector = Selector.open();
        channel.configureBlocking(false);

        boolean connect = channel.connect(new InetSocketAddress(host, port));
        if (!connect) {
            channel.register(selector, SelectionKey.OP_CONNECT);
        } else {
            channel.register(selector, SelectionKey.OP_READ);
        }
        running = true;
        byteBuffer = ByteBuffer.allocate(16384);
        this.start();
    }

    @Override
    public void run() {

        try {
            while (running) {
                int select = selector.select();
                if (select == 0) {
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (key.isConnectable()) {
                        if (sc.isConnectionPending()) {
                            sc.finishConnect();
                            sc.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        readBytes(sc);
//                        System.out.println("read::::");
                    } else if (key.isWritable()) {
                        System.err.println("write::::::::");
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readBytes(SocketChannel sc) throws IOException {
        sc.read(byteBuffer);
        byteBuffer.flip();
        if (byteBuffer.remaining() < 4) {
            byteBuffer.position(byteBuffer.limit());
            byteBuffer.limit(byteBuffer.capacity());
            return;
        }

        int size = byteBuffer.getInt();
        boolean hasSizeBytes = true;
        while (byteBuffer.remaining() >= size) {
            hasSizeBytes = false;
            byte[] temp = new byte[size];
            byteBuffer.get(temp);
            IoBuffer ioBuffer = IoBuffer.wrap(temp);
            handleRead(ioBuffer);
            if (byteBuffer.remaining() < 4) {
                byteBuffer.compact();
                break;
            } else {
                size = byteBuffer.getInt();
                hasSizeBytes = true;
            }
        }

        if (hasSizeBytes) {
            byteBuffer.position(byteBuffer.position() - 4);
            byteBuffer.compact();
        }
    }

    private void handleRead(IoBuffer buffer) throws CharacterCodingException {
        byte actionType = buffer.get();
        switch (actionType) {
            case 0:
                byte success = buffer.get();
                String fail = buffer.getPrefixedString(MsgConstants.StringMsgCharset.get().decoder);
                byte isReconnect = buffer.get();
                long userId = buffer.getLong();
                int sessionId = buffer.getInt();
                System.out.printf("login success, userId=%d\n", userId);
                break;
            case 1:
                byte extId = buffer.get();
                byte cmd = buffer.get();
                short upTag = buffer.getShort();
                byte downTag = buffer.get();
                handler.handleResponse(extId, cmd, buffer);
                break;
            default:
                break;
        }
    }

    public void sendLoginCmd(String name, String pwd) throws IOException, InterruptedException {
        Thread.sleep(1200);
        byte[] data = buildLoginData(name, pwd);
        IoBuffer buffer = IoBuffer.allocate(data.length + 8);
        buffer.putInt(data.length + 4); //size
        buffer.putInt(0);   //messageNo
        buffer.put(data);
        buffer.flip();
        channel.write(buffer.buf());
        Thread.sleep(300);
    }

    private byte[] buildLoginData(String adminName, String adminPwd) throws CharacterCodingException {
        IoBuffer buffer = IoBuffer.allocate(6);
        buffer.setAutoExpand(true);
        buffer.put((byte) 0); // ActionType_Login
        buffer.putPrefixedString(adminName, 2, MsgConstants.StringMsgCharset.get().encoder);
        buffer.putPrefixedString(adminPwd, 2, MsgConstants.StringMsgCharset.get().encoder);
        buffer.putInt(0);
        buffer.flip();
        byte[] data = buffer.array();
        return chiper.encryptMsg(data, 0);
    }

    public void sendExtCmd(byte extId, byte cmd, Object object) throws IOException {
        byte[] data = ClientMessageCoder.encode(object);
        byte[] bytes = buildExtCmdData(extId, cmd, data);
        IoBuffer buffer = IoBuffer.allocate(bytes.length + 8);
        buffer.putInt(bytes.length + 4);
        buffer.putInt(this.msgSeqId);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer.buf());
        //this.msgSeqId = nextMsgSeqNo(this.msgSeqId);
    }

    private byte[] buildExtCmdData(byte extId, byte cmd, byte[] data) {
        if (data == null) {
            data = new byte[0];
        }
        IoBuffer buffer = IoBuffer.allocate(9 + data.length);
        buffer.put((byte) 1);
        buffer.put(extId);
        buffer.put(cmd);
        buffer.putShort((short) tag++);
        buffer.putInt(0);
        buffer.put(data);
        buffer.flip();
        return chiper.encryptMsg(buffer.array(), 0);
    }

    private int nextMsgSeqNo(int now) {
        long next = now * 2L + 1; // now *2 + 1可能会导致int溢出，所以定义long
        return next >= Integer.MAX_VALUE ? 0 : (int) next;
    }

    public static ClientTest doSendRequest(String account) throws IOException, InterruptedException {
        IHandler handle = (extId, cmd, buffer) -> {
            SyncResp response = ClientMessageCoderTest.decode(buffer, SyncResp.class);
            // System.out.println(response.parm);
        };
        ClientTest client = new ClientTest("127.0.0.1", 9333, handle);

        client.sendLoginCmd(account, "000000");
        setHandler(client);
        return client;
    }

    public static void setHandler(ClientTest client) throws IOException {
        IHandler handle = (extId, cmd, buffer) -> {
            if (extId == 1) {
                if (cmd == 2 || cmd == 4) {
                    ResultResp response = ClientMessageCoderTest.decode(buffer, ResultResp.class);
                    System.out.println(response.result);
                }
            } else if (extId == 2) {
                SyncResp resultResponse = ClientMessageCoderTest.decode(buffer, SyncResp.class);
//                resultResponse.vec3List.forEach(pos -> System.out.print(pos.x + "  " + pos.y + " "));
//                System.out.println(new Date(System.currentTimeMillis()) + "  数量:" + num++);

            }

        };
        client.handler = handle;
    }

//    public static void doSendBattleRequest(ClientTest clientTest, byte cmdId, BattleReq request) throws IOException {
//        clientTest.sendExtCmd((byte) 1, cmdId, request);
//    }
//
//    public static void doSendSyncRequest(ClientTest client, byte cmdId, BattleReq request) throws IOException {
//        client.sendExtCmd((byte) 2, cmdId, request);
//    }


    public static void main(String[] args) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch timeStartLatch = new CountDownLatch(1);
        CountDownLatch timeEndLatch = new CountDownLatch(1);
        for (int i = 0; i < 1; i++) {
            new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                    String account = "141281";//RANDOM.nextInt(100) + "12" + RANDOM.nextInt(100);
                    ClientTest client = doSendRequest(account);
                    //timeStartLatch.countDown();
//                    doSendBattleRequest(client, (byte) 2, new BattleReq(Integer.parseInt(account)));
//                    Thread.sleep(2000);
//                    doSendSyncRequest(client, (byte) 1, new BattleReq(Integer.parseInt(account)));
                    //timeEndLatch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        //  timeStartLatch.await();
        long start = System.currentTimeMillis();
        //timeEndLatch.await();
        System.out.println("运行时间：" + (System.currentTimeMillis() - start));
        //doSendRequest();
    }
}

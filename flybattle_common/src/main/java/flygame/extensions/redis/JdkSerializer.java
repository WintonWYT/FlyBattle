package flygame.extensions.redis;

import flygame.common.ApplicationLocal;

import java.io.*;


public class JdkSerializer implements IRedisSerializer {

    @Override
    public byte[] serialize(Object o) {
        byte[] result = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            result = bos.toByteArray();
        } catch (IOException e) {
            ApplicationLocal.instance().error("<<JdkSerializer>> serialize error", e);
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
                bos.close();
            } catch (IOException e) {
                ApplicationLocal.instance().error("<<JdkSerializer>> serialize stream close error", e);
            }
        }
        return result;
    }

    @Override
    public <T> T deserialize(byte[] src, Class<T> cls) {
        T result = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(src);
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(bis);
            result = cls.cast(input.readObject());
        } catch (IOException | ClassNotFoundException e) {
            ApplicationLocal.instance().error("<<JdkSerializer>> deserialize error", e);
        } finally {
            try {
                if(input != null) {
                    input.close();
                }
                bis.close();
            } catch (IOException e) {
                ApplicationLocal.instance().error("<<JdkSerializer>> deserialize stream close error", e);
            }
        }
        return result;
    }
}

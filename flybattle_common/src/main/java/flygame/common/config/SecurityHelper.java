package flygame.common.config;

import flygame.common.ApplicationLocal;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class SecurityHelper {
	private byte[] data = "#~Yasdf2".getBytes();
	private static final String Algorithm = "DES"; // 定义 加密算法,可用 DES,DESede,Blowfish
	private Cipher crypter;
	private static SecurityHelper instance = new SecurityHelper();

	static
	{
		// 添加新安全算法,如果用JCE就要把它添加进去
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
	}

	private SecurityHelper(){
		try {
			crypter = Cipher.getInstance(Algorithm);
			SecretKey deskey = new SecretKeySpec(data,Algorithm);
			crypter.init(Cipher.DECRYPT_MODE, deskey);
		} catch (Exception e){
			ApplicationLocal.instance().error(e.getMessage(), e);
		}
	}

	public static SecurityHelper Instance(){
		return instance;
	}

	public String decrypt(String cipherText) {
		byte[] clearByte = null;
		try {
			clearByte = crypter.doFinal(str2byte(cipherText));
		} catch (Exception e) {
			ApplicationLocal.instance().error(e.getMessage(), e);
		}
		return new String(clearByte);
	}

	public static byte[] str2byte(String s){
		String [] temp = s.split(":");
		byte[] result = new byte[temp.length];
		for(int i = 0 ; i < result.length; i++){
			result[i]= (byte)Integer.parseInt(temp[i], 16);
		}
		return result;
	}
}

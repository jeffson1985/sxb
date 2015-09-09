/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sxb.kit;

import java.security.MessageDigest;
import java.util.Arrays;
/**
 * Hash tools
 * 
 * @author Jeffson
 *
 */
public class HashKit {
	
	private static java.security.SecureRandom random = new java.security.SecureRandom();
	
	/**
	 * MD5加密
	 * @param srcStr
	 * @return String  加密后字符串
	 */
	public static String md5(String srcStr){
		return hash("MD5", srcStr);
	}
	
	/**
	 * SHA1 加密
	 * @param srcStr
	 * @return 加密后字符串
	 */
	public static String sha1(String srcStr){
		return hash("SHA-1", srcStr);
	}
	
	/**
	 * SHA256加密
	 * @param srcStr
	 * @return String  加密后字符串
	 */
	public static String sha256(String srcStr){
		return hash("SHA-256", srcStr);
	}
	
	/**
	 * SHA384加密
	 * @param srcStr
	 * @return String  加密后字符串
	 */
	public static String sha384(String srcStr){
		return hash("SHA-384", srcStr);
	}
	
	public static String sha512(String srcStr){
		return hash("SHA-512", srcStr);
	}
	
	/**
	 * 统一执行方法
	 * @param srcStr
	 * @return String  加密后字符串
	 */
	public static String hash(String algorithm, String srcStr) {
		try {
			StringBuilder result = new StringBuilder();
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] bytes = md.digest(srcStr.getBytes("utf-8"));
			for (byte b : bytes) {
				String hex = Integer.toHexString(b&0xFF);
				if (hex.length() == 1)
					result.append("0");
				result.append(hex);
			}
			return result.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 转换成16进制
	 * @param bytes
	 * @return
	 */
	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(b&0xFF);
			if (hex.length() == 1)
				result.append("0");
			result.append(hex);
		}
		return result.toString();
	}
	
	/**
	 * 自动生成加密salt
	 * md5 128bit 16bytes
	 * sha1 160bit 20bytes
	 * sha256 256bit 32bytes
	 * sha384 384bit 48bites
	 * sha512 512bit 64bites
	 */
	public static String generateSalt(int numberOfBytes) {
		byte[] salt = new byte[numberOfBytes];
		random.nextBytes(salt);
		return toHex(salt);
	}
	
	public static int calc(boolean b){
		return (b ? 0 : 1);
	}
	
	public static int calc(byte b){
		return (int)b;
	}
	
	public static int calc(char ch){
		return (int)ch;
	}
	
	public static int calc(short s){
		return (int)s;
	}
	
	public static int calc(int i){
		return i;
	}
	
	public static int calc(long f){
		return (int)(f ^ (f >>> 32));
	}
	
	public static int calc(float f){
		return Float.floatToIntBits(f);
	}
	
	public static int calc(double f){
		long longValue = Double.doubleToLongBits(f);
		return calc(longValue);
	}
	
	public static int calc(boolean[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(byte[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(char[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(double[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(float[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(int[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(long[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calc(short[] a){
		return Arrays.hashCode(a);
	}
	
	public static int calcObject(Object obj){
		return (obj == null ? 0 : obj.hashCode());
	}
	
	public static int calcShallow(Object[] a){
		return Arrays.hashCode(a);
	}
	
	/**<pre> 
	 * 指定された配列の「深層内容」に基づくハッシュコードを返します。
	 * 配列に要素以外の配列が含まれる場合、ハッシュコードは内容およびその他すべてに基づいたものになります。
	 * このため、自身を要素として含む配列に対して、
	 * このメソッドを 1 つまたは複数の配列レベルを介して直接または間接的に呼び出すことはできません。
	 * この種の呼び出し動作は、定義されていません。
	 *</pre>
	 */
	public static int calcDeep(Object[] a){
		return Arrays.deepHashCode(a);
	}
	
	
	
	// [memo] ここにhash値を合成するメソッドを追加しても良いかも知れない。
	/**<pre>
	 * <b>未テストです.</b>
	 * 複数のhash値を合成します。
	 * (使用例)
	 * 		@Override
	 *		public int hashCode(){
	 *			return HashMaker.combineHashValues(
	 *				HashMaker.calc(_str),
	 *				HashMaker.calc(_longValue)
	 *			);
	 *		}
	 *
	 *</pre>
	 */
	public static int combineHashValues(int... hashList){
		int result = 17;
		for(int hash: hashList){
			result = 37 * result + hash;
		}
		return result;
	}
	
}





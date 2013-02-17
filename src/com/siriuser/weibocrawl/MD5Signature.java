/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siriuser.weibocrawl;

import org.apache.hadoop.io.MD5Hash;

/**
 * Default implementation of a page signature. It calculates an MD5 hash of the
 * raw binary content of a page. In case there is no content, it calculates a
 * hash from the page's URL.
 * 
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class MD5Signature {

	public String calculate(String content) {

		byte[] data = content.getBytes();

		return toHexString(MD5Hash.digest(data).getDigest());
	}

	public String toHexString(byte[] buf) {
		return toHexString(buf, null, Integer.MAX_VALUE);
	}

	/**
	 * Get a text representation of a byte[] as hexadecimal String, where each
	 * pair of hexadecimal digits corresponds to consecutive bytes in the array.
	 * 
	 * @param buf
	 *            input data
	 * @param sep
	 *            separate every pair of hexadecimal digits with this separator,
	 *            or null if no separation is needed.
	 * @param lineLen
	 *            break the output String into lines containing output for
	 *            lineLen bytes.
	 */
	public String toHexString(byte[] buf, String sep, int lineLen) {
		if (buf == null)
			return null;
		if (lineLen <= 0)
			lineLen = Integer.MAX_VALUE;
		StringBuffer res = new StringBuffer(buf.length * 2);
		for (int i = 0; i < buf.length; i++) {
			int b = buf[i];
			res.append(HEX_DIGITS[(b >> 4) & 0xf]);
			res.append(HEX_DIGITS[b & 0xf]);
			if (i > 0 && (i % lineLen) == 0)
				res.append('\n');
			else if (sep != null && i < lineLen - 1)
				res.append(sep);
		}
		return res.toString();
	}

	private final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}

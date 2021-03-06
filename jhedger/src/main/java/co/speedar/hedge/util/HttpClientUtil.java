/**
 * 
 */
package co.speedar.hedge.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Http request related methods.
 * 
 * @author ben
 */
public class HttpClientUtil {
	protected static final Logger log = Logger.getLogger(HttpClientUtil.class);

	/**
	 * Execute an http get request and return the response content.
	 * 
	 * @param hostPath
	 *            the remote host and path, eg. http://www.baidu.com/wtf
	 * @param paramMap
	 *            key-value based request parameters, values will be encoded
	 * @param encoding
	 *            request and response character encoding, defaults to utf-8
	 * @return the response content in string
	 */
	public static String getStringFromHost(String hostPath, Map<String, String> paramMap, Map<String, String> headerMap,
			String encoding) {
		String result = null;
		if (StringUtils.isBlank(encoding)) {
			encoding = "utf-8";
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String uri = buildUri(hostPath, paramMap, encoding);
			HttpGet httpget = new HttpGet(uri);
			log.debug("executing request " + uri);
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				HttpEntity entity = response.getEntity();
				log.debug("Response status: " + response.getStatusLine());
				if (entity != null && response.getStatusLine().getStatusCode() == 200) {
					log.debug("Response content length: " + entity.getContentLength());
					result = EntityUtils.toString(entity, encoding);
					log.debug("Response content: " + result);
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			log.error(e, e);
		} catch (ParseException e) {
			log.error(e, e);
		} catch (IOException e) {
			log.error(e, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				log.error(e, e);
			}
		}
		return result;
	}

	public static String buildUri(String host, Map<String, String> params, String encoding)
			throws UnsupportedEncodingException {
		StringBuffer param = new StringBuffer();
		if (params != null && !params.isEmpty()) {
			param.append("?");
			for (Map.Entry<String, String> entry : params.entrySet()) {
				param.append(entry.getKey());
				param.append("=");
				param.append(URLEncoder.encode(entry.getValue(), encoding));
				param.append("&");
			}
		}
		StringBuffer uri = new StringBuffer(host);
		if (StringUtils.isNotBlank(param.toString())) {
			uri.append(StringUtils.removeEnd(param.toString(), "&"));
		}
		return uri.toString();
	}

	public static void main(String[] args) {
		Map<String, String> params = new HashMap<>();
		params.put("prefixsug", "医药广告");
		System.out.println(getStringFromHost("http://www.baidu.com", params, null, "utf-8"));
	}
}

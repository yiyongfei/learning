/**
 * Copyright 2014 伊永飞
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ea.core.bridge.ws.rest.client;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import com.ea.core.base.model.BaseModel;
import com.ea.core.bridge.ws.AbstractClient;
import com.ea.core.jackson.json.JsonUtil;

public abstract class AbstractRestClient extends AbstractClient {
	private CloseableHttpClient client;
	
	public AbstractRestClient(URL httpUrl) {
		super(httpUrl);
		
        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
            @Override
            public HttpMessageParser<HttpResponse> create(
                SessionInputBuffer buffer, MessageConstraints constraints) {
                LineParser lineParser = new BasicLineParser() {
                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }
                };
                return new DefaultHttpResponseParser(
                    buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {
                    @Override
                    protected boolean reject(final CharArrayBuffer line, int count) {
                        // try to ignore all garbage preceding a status line infinitely
                        return false;
                    }
                };
            }
        };
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                requestWriterFactory, responseParserFactory);

        SSLContext sslcontext = SSLContexts.createSystemDefault();
        X509HostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https", new SSLConnectionSocketFactory(sslcontext, hostnameVerifier))
            .build();

        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("myhost") || host.equalsIgnoreCase("localhost")) {
                    return new InetAddress[] { InetAddress.getByAddress(new byte[] {127, 0, 0, 1}) };
                } else {
                    return super.resolve(host);
                }
            }

        };

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, connFactory, dnsResolver);

        SocketConfig socketConfig = SocketConfig.custom()
            .setTcpNoDelay(true)
            .build();
        connManager.setDefaultSocketConfig(socketConfig);
        connManager.setSocketConfig(new HttpHost("somehost", 80), socketConfig);

        MessageConstraints messageConstraints = MessageConstraints.custom()
            .setMaxHeaderCount(200)
            .setMaxLineLength(2000)
            .build();
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setMalformedInputAction(CodingErrorAction.IGNORE)
            .setUnmappableInputAction(CodingErrorAction.IGNORE)
            .setCharset(Consts.UTF_8)
            .setMessageConstraints(messageConstraints)
            .build();
        connManager.setDefaultConnectionConfig(connectionConfig);
        connManager.setConnectionConfig(new HttpHost("somehost", 80), ConnectionConfig.DEFAULT);
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 20);

        CookieStore cookieStore = new BasicCookieStore();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BEST_MATCH)
            .setExpectContinueEnabled(true)
            .setStaleConnectionCheckEnabled(true)
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .setConnectionRequestTimeout(3000)
            .setConnectTimeout(3000)
            .setSocketTimeout(3000)
            .build();

        client = HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultCookieStore(cookieStore)
            .setDefaultCredentialsProvider(credentialsProvider)
//            .setProxy(new HttpHost("myproxy", 8080))
            .setDefaultRequestConfig(defaultRequestConfig)
            .build();
	}
	
	/**
	 * Params的说明：
	 * Rest服务提供方在提供服务时对请求参数的传递方式有些限制，框架只支持以Path方式放在Url里或者放在Http请求的正文里
	 * Params存放欲发送到服务器的请求参数，类型有2种：
	 * String类型，允许多个，将按序以Path的方式放在Url里（要求服务方以@PathParam("id")方式获取）发送
	 * BaseModel类型，只允许一个，该Bean参数将会转换成Json的方式放在请求的正文里发送
	 * GET和DELETE只允许按Url的方式传递参数，一般来说，查看查项资源的详细内容可以用GET来获取，但如果是根据一定条件来检索符合条件的资源，则需要权衡
	 * 
	 * 举例：
	 * @PUT
	 * @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     * @Path("/update/{id}")
	 * public void update(@PathParam("id") String id, ExampleUsersDTO exampleDto);
	 * 
	 * 这时params里有2个参数，第1个参数是字符串参数id，第2个参数是JavaBean参数DTO，发送时，id将作为Path里的一份子，而DTO将转成JSON放在Entity里以Params的方式发送
	 * 
	 */
	protected Object transferWS(String methodName, Object... params) throws Exception {
		HttpRequestBase request = getMethod(getUrl(methodName, params));
		
		if(request instanceof HttpEntityEnclosingRequestBase){
			HttpEntity entity = getHttpEntity(params);
			if(entity != null){
				((HttpEntityEnclosingRequestBase)request).setEntity(entity);
			}
		}

		CloseableHttpResponse response = null;
		try{
		    response = client.execute(request);
		    if(response.getStatusLine().getStatusCode() == 200){
		    	return EntityUtils.toString(response.getEntity());
		    } else {
		    	if(response.getStatusLine().getStatusCode() == 204){
		    		//使用REST时，如果返回结果是void时，此时Http的返回码是204
		    		//HTTP的204(No Content)响应,就表示执行成功,但是服务器没有返回数据.
		    		return "";
		    	} else {
		    		throw new Exception("调用服务失败，返回结果码为" + response.getStatusLine().getStatusCode());
				}
		    }
	    	
		} finally {
			client.close();
			if(response != null){
				response.close();
			}
		}
		
    }
	
	protected abstract HttpRequestBase getMethod(String url);

	/**
	 * 按REST方式发送数据，如果提供的参数里有字符串内容，这些字符串内容将按PATH的数据组织方式发送给服务提供方
	 * 
	 * @param methodName
	 * @param params
	 * @return
	 */
	protected String getUrl(String methodName, Object... params){
		StringBuffer url = new StringBuffer(httpUrl.toString());
		if(httpUrl.toString().endsWith("/")){
			url.append(methodName);
		} else {
			url.append("/").append(methodName);
		}
		
		if(params != null){
			for(Object obj : params){
				if(obj instanceof String){
					url.append("/").append(obj);
				}
			}
		}
		
		return url.toString();
	}
	
	protected HttpEntity getHttpEntity(Object... params) throws Exception{
		if(params != null){
			BaseModel jsonObject = null;
			for(Object obj : params){
				if(obj instanceof BaseModel){
					jsonObject = (BaseModel)obj;
					break;
				}
			}
			if(jsonObject != null){
				String jsonString = JsonUtil.generatorJson(jsonObject);
				StringEntity entity = new StringEntity(jsonString, "UTF-8");
				entity.setContentType(ContentType.APPLICATION_JSON.toString());
				return entity;
			}
		}
		return null;
	}
}

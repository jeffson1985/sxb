package org.sxb.http.converter.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.sxb.http.HttpInputMessage;
import org.sxb.http.HttpOutputMessage;
import org.sxb.http.MediaType;
import org.sxb.http.converter.AbstractHttpMessageConverter;
import org.sxb.http.converter.HttpMessageNotReadableException;
import org.sxb.http.converter.HttpMessageNotWritableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * Implementation of {@link org.sxb.http.converter.HttpMessageConverter HttpMessageConverter} that
 * can read and write JSON using fastJson1.26
 *
 * <p>This converter can be used to bind to typed beans, or untyped {@link java.util.HashMap HashMap} instances.
 *
 * <p>By default, this converter supports {@code application/json} and {@code application/*+json}.
 * This can be overridden by setting the {@link #setSupportedMediaTypes supportedMediaTypes} property.
 *
 *
 * <p>Compatible with fastJson 1.2.6 and higher.
 *
 * @author Jeffson
 * @since 2.0
 */
public class FastJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public final static Charset UTF8     = Charset.forName("UTF-8");

    private Charset             charset  = UTF8;

    private SerializerFeature[] features = new SerializerFeature[0];

    public FastJsonHttpMessageConverter(){
        super(new MediaType("application", "json", UTF8), new MediaType("application", "*+json", UTF8));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public SerializerFeature[] getFeatures() {
        return features;
    }

    public void setFeatures(SerializerFeature... features) {
        this.features = features;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException,
                                                                                               HttpMessageNotReadableException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream in = inputMessage.getBody();

        byte[] buf = new byte[1024];
        for (;;) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }

            if (len > 0) {
                baos.write(buf, 0, len);
            }
        }

        byte[] bytes = baos.toByteArray();
        System.out.println("FastJson:" + new String(bytes));
        return JSON.parseObject(bytes, 0, bytes.length, charset.newDecoder(), clazz);
    }

    @Override
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException,
                                                                             HttpMessageNotWritableException {

        OutputStream out = outputMessage.getBody();
        String text = JSON.toJSONString(obj, features);
        byte[] bytes = text.getBytes(charset);
        out.write(bytes);
    }

}

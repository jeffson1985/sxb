package org.sxb.kit.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.*;

import java.lang.reflect.Type;

/**
 *  未使用
 *  此类，使用了阿里巴巴的fastjson包
 * Created by son on 14-12-31.
 */
public class Jsoner {
	private static SerializeConfig config = SerializeConfig.getGlobalInstance();
	private static ParserConfig parserConfig = ParserConfig.getGlobalInstance();

	static {
		config.put(java.util.Date.class, new SimpleDateFormatSerializer(
				"yyyy-MM-dd HH:mm:ss"));
		config.put(java.sql.Date.class, new SimpleDateFormatSerializer(
				"yyyy-MM-dd"));
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer(
				"yyyy-MM-dd HH:mm:ss.SSS"));
		config.put(java.sql.Time.class, new SimpleDateFormatSerializer(
				"HH:mm:ss"));
	}

	public static void addConfig(Type type, ObjectSerializer serializer,
			ObjectDeserializer deserializer) {
		addSerializer(type, serializer);
		addDeserializer(type, deserializer);
	}

	public static void addSerializer(Type type, ObjectSerializer serializer) {
		config.put(type, serializer);
	}

	public static void addDeserializer(Type type,
			ObjectDeserializer deserializer) {
		parserConfig.putDeserializer(type, deserializer);
	}

	public static String toJSON(Object object) {
		return JSON.toJSONString(object,
				SerializerFeature.DisableCircularReferenceDetect);
	}

	public static String toJSON(Object object, SerializerFeature... features) {
		return JSON.toJSONString(object, features);
	}

	public static String toJSON(Object object, SerializeFilter[] filters,
			SerializerFeature... features) {
		return JSON.toJSONString(object, filters, features);
	}

	public static String toJSON(Object object, SerializeConfig config,
			SerializerFeature... features) {
		return JSON.toJSONString(object, config, features);
	}

	public static String toJSON(Object object, SerializeConfig config,
			SerializeFilter filter, SerializerFeature... features) {
		return JSON.toJSONString(object, config, filter, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T toObject(String json) {
		if(json == null) return null;
		try {
			return (T) JSON.parse(json);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ JSONObject.class.getName(), e);
		}
	}

	public static <T> T toObject(String json, Class<T> clazz) {
		try {
			return JSON.parseObject(json, clazz);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ clazz.getName(), e);
		}
	}

	public static <T> T toObject(String json, Class<T> clazz,
			Feature... features) {
		try {
			return JSON.parseObject(json, clazz, features);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ clazz.getName(), e);
		}
	}

	public static <T> T toObject(String json, Class<T> clazz,
			ParseProcess processor, Feature... features) {
		try {
			return JSON.parseObject(json, clazz, processor, features);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ clazz.getName(), e);
		}
	}

	public static <T> T toObject(String json, TypeReference<T> type,
			Feature... features) {
		try {
			return JSON.parseObject(json, type, features);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ type.getClass().getName(), e);
		}
	}

	public static <T> T toObject(String json, Type type, Feature... features) {
		try {
			return JSON.parseObject(json, type, features);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ type.getClass().getName(), e);
		}
	}

	public static <T> T toObject(String json, Type type,
			ParseProcess processor, Feature... features) {
		try {
			return JSON.parseObject(json, type, processor, features);
		} catch (JSONException e) {
			throw new JsonException("Could not cast \"" + json + "\" to "
					+ type.getClass().getName(), e);
		}
	}

}

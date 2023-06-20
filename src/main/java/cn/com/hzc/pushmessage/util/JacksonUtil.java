package cn.com.hzc.pushmessage.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        // 忽略json字符串中不识别的属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略无法转换的对象
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // PrettyPrinter 格式化输出 请用toPrettyJsonString()
//        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        // NULL不参与序列化
//        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 指定时区
        OBJECT_MAPPER.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        // 日期类型字符串处理
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // java8日期日期处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        OBJECT_MAPPER.registerModule(javaTimeModule);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化json出错：" + e.getMessage(), e);
        }
    }

    public static String toPrettyJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化json出错：" + e.getMessage(), e);
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化对象出错：" + e.getMessage(), e);
        }
    }

    public static <T> T[] toArray(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readerForArrayOf(clazz).readValue(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化数组出错：" + e.getMessage(), e);
        }
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readerForListOf(clazz).readValue(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化list出错：" + e.getMessage(), e);
        }
    }

    public static List<Map<String, Object>> toList(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化list map出错：" + e.getMessage(), e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化map出错：" + e.getMessage(), e);
        }
    }

    /**
     * JsonNode toString()返回json字符串
     * 也可被ObjectMapper转回json字符串
     */
    public static JsonNode toJsonNode(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json反序列化出错：" + e.getMessage(), e);
        }
    }

    public static JsonNode toJsonNode(byte[] json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("json反序列化出错：" + e.getMessage(), e);
        }
    }

    public static <T> T treeToValue(TreeNode treeNode, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.treeToValue(treeNode, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json对象转java对象出错：" + e.getMessage(), e);
        }
    }
}

package com.mt.mtsocket.distribute;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.mt.mtcommon.UtilKt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Abstract base class for Jackson based and content type independent
 * { HttpMessageConverter} implementations.
 *
 * <p>Compatible with Jackson 2.9 and higher, as of Spring 5.0.
 *
 * @author Arjen Poutsma
 * @author Keith Donald
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 4.1
 */
public class AbstractJackson2NettyMessageConverter {
    protected final Log logger = LogFactory.getLog(getClass());

    protected ObjectMapper objectMapper;

    protected AbstractJackson2NettyMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(UtilKt.getJavaTimeModule());
        this.objectMapper.registerModule(new KotlinModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean canRead(Type type, @Nullable Class<?> contextClass) {
        JavaType javaType = getJavaType(type, contextClass);
        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canDeserialize(javaType, causeRef)) {
            return true;
        }
        logWarningIfNecessary(javaType, causeRef.get());
        return false;
    }

    /**
     * Determine whether to log the given exception coming from a
     * {@link ObjectMapper#canDeserialize} / {@link ObjectMapper#canSerialize} check.
     *
     * @param type  the class that Jackson tested for (de-)serializability
     * @param cause the Jackson-thrown exception to evaluate
     *              (typically a {@link JsonMappingException})
     * @since 4.3
     */
    protected void logWarningIfNecessary(Type type, @Nullable Throwable cause) {
        if (cause == null) {
            return;
        }

        // Do not log warning for serializer not found (note: different message wording on Jackson 2.9)
        boolean debugLevel = (cause instanceof JsonMappingException && cause.getMessage().startsWith("Cannot find"));

        if (debugLevel ? logger.isDebugEnabled() : logger.isWarnEnabled()) {
            String msg = "Failed to evaluate Jackson " + (type instanceof JavaType ? "de" : "") +
                    "serialization for type [" + type + "]";
            if (debugLevel) {
                logger.debug(msg, cause);
            } else if (logger.isDebugEnabled()) {
                logger.warn(msg, cause);
            } else {
                logger.warn(msg + ": " + cause);
            }
        }
    }

    public Object read(Type type, @Nullable Class<?> contextClass, ServiceRequestInfo inputMessage) {
        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, ServiceRequestInfo inputMessage) {
        return objectMapper.convertValue(inputMessage.getData(), javaType);
    }

    /**
     * Return the Jackson {@link JavaType} for the specified type and context class.
     *
     * @param type         the generic type to return the Jackson JavaType for
     * @param contextClass a context class for the target type, for example a class
     *                     in which the target type appears in a method signature (can be {@code null})
     * @return the Jackson JavaType
     */
    protected JavaType getJavaType(Type type, @Nullable Class<?> contextClass) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }


}


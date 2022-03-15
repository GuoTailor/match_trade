package com.mt.mtsocket.distribute.mapping;

import com.mt.mtsocket.distribute.DispatcherServlet;
import com.mt.mtsocket.distribute.ServiceRequestInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 定义请求和{@link HandlerMethod}之间映射的HandlerMapping实现的抽象基类。
 *
 * <p>对于每个注册的处理程序方法，都维护一个惟一的映射，子类定义映射类型{@code <T>}的详细信息。
 *
 * @param <T> the mapping for a {@link HandlerMethod} containing the conditions
 *            needed to match the handler method to incoming request.
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractWebSocketHandlerMethodMapping<T> extends ApplicationObjectSupport implements InitializingBean {
    protected final Log logger = LogFactory.getLog(this.getClass());
    /**
     * Bean name prefix for target beans behind scoped proxies. Used to exclude those
     * targets from handler method detection, in favor of the corresponding proxies.
     * <p>We're not checking the autowire-candidate status here, which is how the
     * proxy target filtering problem is being handled at the autowiring level,
     * since autowire-candidate may have been turned to {@code false} for other
     * reasons, while still expecting the bean to be eligible for handler methods.
     * <p>Originally defined in {@link org.springframework.aop.scope.ScopedProxyUtils}
     * but duplicated here to avoid a hard dependency on the spring-aop module.
     */
    private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

    private final boolean detectHandlerMethodsInAncestorContexts = false;

    @Nullable
    private HandlerMethodMappingNamingStrategy<T> namingStrategy;

    private final MappingRegistry mappingRegistry = new MappingRegistry();

    /**
     * 配置命名策略，以便为每个映射的处理程序方法分配默认名称。
     * <p>The default naming strategy is based on the capital letters of the
     * class name followed by "#" and then the method name, e.g. "TC#getFoo"
     * for a class named TestController with method getFoo.
     */
    public void setHandlerMethodMappingNamingStrategy(HandlerMethodMappingNamingStrategy<T> namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    /**
     * 返回配置的命名策略或 {@code null}.
     */
    @Nullable
    public HandlerMethodMappingNamingStrategy<T> getNamingStrategy() {
        return this.namingStrategy;
    }

    /**
     * 在初始化时检测处理程序方法。
     *
     * @see #initHandlerMethods
     */
    @Override
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    /**
     * 扫描ApplicationContext中的bean，检测和注册处理程序方法
     *
     * @see #getCandidateBeanNames()
     * @see #processCandidateBean
     */
    protected void initHandlerMethods() {
        for (String beanName : getCandidateBeanNames()) {
            if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
                processCandidateBean(beanName);
            }
        }
        obtainApplicationContext().getBean(DispatcherServlet.class).initHandlerMappings(obtainApplicationContext());
    }

    /**
     * 确定应用程序上下文中候选bean的名称。
     *
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors
     * @since 5.1
     */
    protected String[] getCandidateBeanNames() {
        return (this.detectHandlerMethodsInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
                obtainApplicationContext().getBeanNamesForType(Object.class));
    }

    /**
     * 确定指定的候选bean的类型，如果确定为处理程序类型，则调用
     * {@link #detectHandlerMethods} 如果标识为处理程序类型。
     * <p>这个实现通过检查避免了bean的创建
     * {@link org.springframework.beans.factory.BeanFactory#getType}
     * 和调用 {@link #detectHandlerMethods} 使用bean名称。
     *
     * @param beanName the name of the candidate bean
     * @see #isHandler
     * @see #detectHandlerMethods
     * @since 5.1
     */
    protected void processCandidateBean(String beanName) {
        Class<?> beanType = null;
        try {
            beanType = obtainApplicationContext().getType(beanName);
        } catch (Throwable ex) {
            // An unresolvable bean type, probably from a lazy bean - let's ignore it.
            if (logger.isTraceEnabled()) {
                logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
            }
        }
        if (beanType != null && isHandler(beanType)) {
            detectHandlerMethods(beanName);
        }
    }

    /**
     * 在指定的处理程序bean中查找处理程序方法。
     *
     * @param handler either a bean name or an actual handler instance
     * @see #getMappingForMethod
     */
    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = (handler instanceof String ?
                obtainApplicationContext().getType((String) handler) : handler.getClass());

        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<T>) method -> {
                        try {
                            return getMappingForMethod(method, userType);
                        } catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });
            if (logger.isTraceEnabled()) {
                logger.trace(formatMappings(userType, methods));
            }
            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handler, invocableMethod, mapping);
            });
        }
    }

    private String formatMappings(Class<?> userType, Map<Method, T> methods) {

        String formattedType = Arrays.stream(userType.getPackage().getName().split("\\."))
                .map(p -> p.substring(0, 1))
                .collect(Collectors.joining(".", "", ".")) + userType.getSimpleName();

        Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(",", "(", ")"));

        return methods.entrySet().stream()
                .map(e -> {
                    Method method = e.getKey();
                    return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
                })
                .collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
    }

    /**
     * 注册一个处理程序方法及其惟一映射。在启动时为每个检测到的处理程序方法调用。
     *
     * @param handler 处理程序或处理程序实例的bean名称
     * @param method  注册方法
     * @param mapping 与处理程序方法关联的映射条件
     * @throws IllegalStateException if another method was already registered
     *                               under the same mapping
     */
    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        this.mappingRegistry.register(mapping, handler, method);
    }

    /**
     * 创建HandlerMethod实例。
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        HandlerMethod handlerMethod;
        if (handler instanceof String) {
            String beanName = (String) handler;
            handlerMethod = new HandlerMethod(beanName,
                    obtainApplicationContext().getAutowireCapableBeanFactory(), method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }
        return handlerMethod;
    }

    /**
     * 查找给定请求的处理程序方法。
     */
    public HandlerMethod getHandlerInternal(ServiceRequestInfo request) {
        String lookupPath = request.getOrder();
        this.mappingRegistry.acquireReadLock();
        try {
            HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
            return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.mappingRegistry.releaseReadLock();
        }
        return null;
    }

    /**
     * 查找当前请求的最佳匹配处理程序方法。如果找到多个匹配项，则选择最佳匹配项。
     *
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     * @return the best-matching handler method, or {@code null} if no match
     */
    @Nullable
    protected HandlerMethod lookupHandlerMethod(String lookupPath, ServiceRequestInfo request) {
        List<Match> matches = new ArrayList<>();
        List<T> directPathMatches = this.mappingRegistry.getMappingsByUrl(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches, request);
        }
        if (matches.isEmpty()) {
            // 除了遍历所有映射之外别无选择…
            addMatchingMappings(this.mappingRegistry.getMappings().keySet(), matches, request);
        }

        if (!matches.isEmpty()) {
            Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
            matches.sort(comparator);
            Match bestMatch = matches.get(0);
            if (matches.size() > 1) {
                if (logger.isTraceEnabled()) {
                    logger.trace(matches.size() + " matching mappings: " + matches);
                }
                Match secondBestMatch = matches.get(1);
                if (comparator.compare(bestMatch, secondBestMatch) == 0) {
                    Method m1 = bestMatch.handlerMethod.getMethod();
                    Method m2 = secondBestMatch.handlerMethod.getMethod();
                    //String uri = request.getRequestURI();
                    throw new IllegalStateException(
                            "Ambiguous handler methods mapped for '" + "': {" + m1 + ", " + m2 + "}");
                }
            }
            //request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, bestMatch.handlerMethod);
            handleMatch(bestMatch.mapping, lookupPath, request);
            return bestMatch.handlerMethod;
        } else {
            return null;
        }
    }

    private void addMatchingMappings(Collection<T> mappings, List<Match> matches, ServiceRequestInfo request) {
        for (T mapping : mappings) {
            T match = getMatchingMapping(mapping, request);
            if (match != null) {
                matches.add(new Match(match, this.mappingRegistry.getMappings().get(mapping)));
            }
        }
    }

    /**
     * 在找到匹配映射时调用。
     *
     * @param mapping    匹配映射
     * @param lookupPath 映射当前servlet映射中的查找路径
     * @param request    当前请求
     */
    protected void handleMatch(T mapping, String lookupPath, ServiceRequestInfo request) {
        //request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }

    /**
     * Whether the given type is a handler with handler methods.
     *
     * @param beanType the type of the bean being checked
     * @return "true" if this a handler type, "false" otherwise.
     */
    protected abstract boolean isHandler(Class<?> beanType);

    /**
     * 为处理程序方法提供映射。不能提供映射的方法不是处理程序方法。
     *
     * @param method      the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's
     *                    declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    @Nullable
    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    /**
     * Extract and return the URL paths contained in a mapping.
     */
    protected abstract Set<String> getMappingPathPatterns(T mapping);

    /**
     * Check if a mapping matches the current request and return a (potentially
     * new) mapping with conditions relevant to the current request.
     *
     * @param mapping the mapping to get a match for
     * @param request the current HTTP servlet request
     * @return the match, or {@code null} if the mapping doesn't match
     */
    @Nullable
    protected abstract T getMatchingMapping(T mapping, ServiceRequestInfo request);

    /**
     * Return a comparator for sorting matching mappings.
     * The returned comparator should sort 'better' matches higher.
     *
     * @param request the current request
     * @return the comparator (never {@code null})
     */
    protected abstract Comparator<T> getMappingComparator(ServiceRequestInfo request);


    /**
     * A registry that maintains all mappings to handler methods, exposing methods
     * to perform lookups and providing concurrent access.
     * <p>Package-private for testing purposes.
     */
    class MappingRegistry {

        private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

        private final MultiValueMap<String, T> urlLookup = new LinkedMultiValueMap<>();

        private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();

        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        /**
         * Return all mappings and handler methods. Not thread-safe.
         *
         * @see #acquireReadLock()
         */
        public Map<T, HandlerMethod> getMappings() {
            return this.mappingLookup;
        }

        /**
         * Return matches for the given URL path. Not thread-safe.
         *
         * @see #acquireReadLock()
         */
        @Nullable
        public List<T> getMappingsByUrl(String urlPath) {
            return this.urlLookup.get(urlPath);
        }

        /**
         * Acquire the read lock when using getMappings and getMappingsByUrl.
         */
        public void acquireReadLock() {
            this.readWriteLock.readLock().lock();
        }

        /**
         * Release the read lock after using getMappings and getMappingsByUrl.
         */
        public void releaseReadLock() {
            this.readWriteLock.readLock().unlock();
        }

        public void register(T mapping, Object handler, Method method) {
            this.readWriteLock.writeLock().lock();
            try {
                HandlerMethod handlerMethod = createHandlerMethod(handler, method);
                assertUniqueMethodMapping(handlerMethod, mapping);
                // 路径方法对
                this.mappingLookup.put(mapping, handlerMethod);

                List<String> directUrls = getDirectUrls(mapping);
                for (String url : directUrls) {
                    // 路径RequestMapperInfo
                    this.urlLookup.add(url, mapping);
                }

                String name;
                if (getNamingStrategy() != null) {
                    name = getNamingStrategy().getName(handlerMethod, mapping);
                    addMappingName(name, handlerMethod);
                }
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        private void assertUniqueMethodMapping(HandlerMethod newHandlerMethod, T mapping) {
            HandlerMethod handlerMethod = this.mappingLookup.get(mapping);
            if (handlerMethod != null && !handlerMethod.equals(newHandlerMethod)) {
                throw new IllegalStateException(
                        "Ambiguous mapping. Cannot map '" + newHandlerMethod.getBean() + "' method \n" +
                                newHandlerMethod + "\nto " + mapping + ": There is already '" +
                                handlerMethod.getBean() + "' bean method\n" + handlerMethod + " mapped.");
            }
        }

        private List<String> getDirectUrls(T mapping) {
            List<String> urls = new ArrayList<>(1);
            urls.addAll(getMappingPathPatterns(mapping));
            return urls;
        }

        private void addMappingName(String name, HandlerMethod handlerMethod) {
            List<HandlerMethod> oldList = this.nameLookup.get(name);
            if (oldList == null) {
                oldList = Collections.emptyList();
            }

            for (HandlerMethod current : oldList) {
                if (handlerMethod.equals(current)) {
                    return;
                }
            }

            List<HandlerMethod> newList = new ArrayList<>(oldList.size() + 1);
            newList.addAll(oldList);
            newList.add(handlerMethod);
            this.nameLookup.put(name, newList);
        }

    }


    /**
     * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
     * comparing the best match with a comparator in the context of the current request.
     */
    private class Match {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        public Match(T mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }


    private class MatchComparator implements Comparator<Match> {

        private final Comparator<T> comparator;

        public MatchComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Match match1, Match match2) {
            return this.comparator.compare(match1.mapping, match2.mapping);
        }
    }

}


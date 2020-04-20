package com.mt.mtsocket.distribute.condition;

import com.mt.mtsocket.distribute.ServiceRequestInfo;
import org.springframework.lang.Nullable;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * 根据一组URL路径模式匹配请求的逻辑分离(' || ')请求条件。
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
//TODO 路径匹配待做
public final class PatternsRequestCondition {

    private final Set<String> patterns;

    /**
     * 使用给定的URL模式创建一个新实例。
     * Each pattern that is not empty and does not start with "/" is prepended with "/".
     * @param patterns 0 or more URL patterns; if 0 the condition will match to every request.
     */
    public PatternsRequestCondition(String... patterns) {
        this(Arrays.asList(patterns));
    }

    /**
     * 接受模式集合的私有构造函数。
     */
    private PatternsRequestCondition(Collection<String> patterns) {
        this.patterns = new LinkedHashSet<>(patterns);
    }

    /**
     * Indicates whether this condition is empty, i.e. whether or not it
     * contains any discrete items.
     * @return {@code true} if empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return getContent().isEmpty();
    }

    public Set<String> getPatterns() {
        return this.patterns;
    }

    protected Collection<String> getContent() {
        return this.patterns;
    }

    /**
     * Returns a new instance with URL patterns from the current instance ("this") and
     * the "other" instance as follows:
     * <ul>
     * <li>If there are patterns in both instances, combine the patterns in "this" with
     * the patterns in "other" using {@link PathMatcher#combine(String, String)}.
     * <li>If only one instance has patterns, use them.
     * <li>If neither instance has patterns, use an empty String (i.e. "").
     * </ul>
     */
    public PatternsRequestCondition combine(PatternsRequestCondition other) {
        Set<String> result = new LinkedHashSet<>();
        //TODO
        if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
            result.addAll(this.patterns);
            result.addAll(other.patterns);
        }
        else if (!this.patterns.isEmpty()) {
            result.addAll(this.patterns);
        }
        else if (!other.patterns.isEmpty()) {
            result.addAll(other.patterns);
        }
        else {
            result.add("");
        }
        return new PatternsRequestCondition(result);
    }

    /**
     * Checks if any of the patterns match the given request and returns an instance
     * that is guaranteed to contain matching patterns, sorted via
     * {@link PathMatcher#getPatternComparator(String)}.
     * <p>A matching pattern is obtained by making checks in the following order:
     * <ul>
     * <li>Direct match
     * <li>Pattern match with ".*" appended if the pattern doesn't already contain a "."
     * <li>Pattern match
     * <li>Pattern match with "/" appended if the pattern doesn't already end in "/"
     * </ul>
     * @param request the current request
     * @return 如果条件不包含模式，则使用相同的实例;或具有排序匹配模式的新条件;如果没有模式匹配，则为 {@code null} 。
     */
    @Nullable
    public PatternsRequestCondition getMatchingCondition(ServiceRequestInfo request) {
        if (this.patterns.isEmpty()) {
            return this;
        }
        String lookupPath = request.getOrder();
        List<String> matches = getMatchingPatterns(lookupPath);
        return (!matches.isEmpty() ?
                new PatternsRequestCondition(matches) : null);
    }

    /**
     * 找到与给定查找路径匹配的模式。调用此方法应产生与调用相同的结果
     * { #getMatchingCondition(javax.servlet.http.HttpServletRequest)}.
     * 如果没有可用的请求(例如自检、工具等)，则提供此方法作为替代方法。
     * @param lookupPath 匹配现有模式的查找路径
     * @return 匹配模式的集合，按最接近的匹配在顶部排序
     */
    public List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                matches.add(match);
            }
        }
        if (matches.size() > 1) {
            matches.sort(String::compareTo);
        }
        return matches;
    }

    @Nullable
    private String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        return null;
    }

    /**
     * Compare the two conditions based on the URL patterns they contain.
     * Patterns are compared one at a time, from top to bottom via
     * {@link PathMatcher#getPatternComparator(String)}. If all compared
     * patterns match equally, but one instance has more patterns, it is
     * considered a closer match.
     * <p>It is assumed that both instances have been obtained via
     * { #getMatchingCondition(HttpServletRequest)} to ensure they
     * contain only patterns that match the request and are sorted with
     * the best matches on top.
     */
    public int compareTo(PatternsRequestCondition other, ServiceRequestInfo request) {
        return 0;
    }

}

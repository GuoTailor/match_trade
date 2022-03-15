package com.mt.mtsocket.distribute.mapping;

import com.mt.mtsocket.distribute.ServiceRequestInfo;
import com.mt.mtsocket.distribute.condition.PatternsRequestCondition;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Request mapping information. Encapsulates the following request mapping conditions:
 * <ol>
 * <li>{@code RequestCondition} (optional, custom request condition)
 * </ol>
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public final class RequestMappingInfo {

    @Nullable
    private final String name;

    private final PatternsRequestCondition patternsCondition;


    public RequestMappingInfo(@Nullable String name, @Nullable PatternsRequestCondition patterns) {

        this.name = (StringUtils.hasText(name) ? name : null);
        this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
    }

    /**
     * Return the URL patterns of this {@link RequestMappingInfo};
     * or instance with 0 patterns (never {@code null}).
     */
    public PatternsRequestCondition getPatternsCondition() {
        return this.patternsCondition;
    }

    /**
     * Return the name for this mapping, or {@code null}.
     */
    @Nullable
    public String getName() {
        return this.name;
    }


    /**
     * Combine "this" request mapping info (i.e. the current instance) with another request mapping info instance.
     * <p>Example: combine type- and method-level request mappings.
     * @return a new request mapping info instance; never {@code null}
     */
    public RequestMappingInfo combine(RequestMappingInfo other) {
        String name = combineNames(other);
        PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
        return new RequestMappingInfo(name, patterns);
    }

    @Nullable
    private String combineNames(RequestMappingInfo other) {
        if (this.name != null && other.name != null) {
            String separator = "#";
            return this.name + separator + other.name;
        }
        else if (this.name != null) {
            return this.name;
        }
        else {
            return other.name;
        }
    }

    /**
     * Checks if all conditions in this request mapping info match the provided request and returns
     * a potentially new request mapping info with conditions tailored to the current request.
     * <p>For example the returned instance may contain the subset of URL patterns that match to
     * the current request, sorted with best matching patterns on top.
     * @return a new instance in case all conditions match; or {@code null} otherwise
     */
    @Nullable
    public RequestMappingInfo getMatchingCondition(ServiceRequestInfo request) {
        PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
        if (patterns == null) {
            return null;
        }
        return new RequestMappingInfo(this.name, patterns);
    }

    /**
     * Compares "this" info (i.e. the current instance) with another info in the context of a request.
     * <p>Note: It is assumed both instances have been obtained via
     * { #getMatchingCondition(HttpServletRequest)} to ensure they have conditions with
     * content relevant to current request.
     */
    public int compareTo(RequestMappingInfo other, ServiceRequestInfo request) {
        int result;
        result = this.patternsCondition.compareTo(other.getPatternsCondition(), request);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RequestMappingInfo)) {
            return false;
        }
        RequestMappingInfo otherInfo = (RequestMappingInfo) other;
        return (this.patternsCondition.equals(otherInfo.patternsCondition));
    }

    @Override
    public int hashCode() {
        return (this.patternsCondition.hashCode() * 31);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (!this.patternsCondition.isEmpty()) {
            Set<String> patterns = this.patternsCondition.getPatterns();
            builder.append(" ").append(patterns.size() == 1 ? patterns.iterator().next() : patterns);
        }
        builder.append('}');
        return builder.toString();
    }


    /**
     * Create a new {@code RequestMappingInfo.Builder} with the given paths.
     * @param paths the paths to use
     * @since 4.2
     */
    public static Builder paths(String... paths) {
        return new DefaultBuilder(paths);
    }


    /**
     * Defines a builder for creating a RequestMappingInfo.
     * @since 4.2
     */
    public interface Builder {

        /**
         * Set the path patterns.
         */
        Builder paths(String... paths);

        /**
         * Set the mapping name.
         */
        Builder mappingName(String name);

        /**
         * Build the RequestMappingInfo.
         */
        RequestMappingInfo build();
    }


    private static class DefaultBuilder implements Builder {

        private String[] paths;

        @Nullable
        private String mappingName;

        public DefaultBuilder(String... paths) {
            this.paths = paths;
        }

        @Override
        public Builder paths(String... paths) {
            this.paths = paths;
            return this;
        }


        @Override
        public DefaultBuilder mappingName(String name) {
            this.mappingName = name;
            return this;
        }

        @Override
        public RequestMappingInfo build() {
            return new RequestMappingInfo(this.mappingName,
                    new PatternsRequestCondition(this.paths));
        }
    }


}

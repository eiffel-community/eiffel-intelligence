package com.ericsson.ei.jmespath;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class MatchFunction extends BaseFunction {

    public MatchFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.STRING),
                ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    /**
     * This method match a regex pattern with a given string. It is used for
     * extracting groupID component of GAV from purl
     *
     * @param string
     *            A given string, in this case purl
     * @param pattern
     *            Any regex pattern
     * @return matching pattern
     */
    public String match(String string, String pattern) {
        final Pattern REQUIRED_PATTERN_REGEX = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = REQUIRED_PATTERN_REGEX.matcher(string);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.
     * Adapter, java.util.List)
     *
     * Takes a string and regex pattern string. Then apply regex on the given string
     * for a match
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T string_val = arguments.get(0).value();
        T pattern_val = arguments.get(1).value();
        String string = runtime.toString(string_val);
        String pattern = runtime.toString(pattern_val);
        String result = match(string, pattern);
        return runtime.createString(result.toString());
    }
}

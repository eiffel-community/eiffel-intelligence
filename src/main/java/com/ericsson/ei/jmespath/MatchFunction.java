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

    public String match(String string, String pattern) {
        final Pattern REQUIRED_PATTERN_REGEX = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = REQUIRED_PATTERN_REGEX.matcher(string);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T value1 = arguments.get(0)
                            .value();
        T value2 = arguments.get(1)
                            .value();
        String val1 = runtime.toString(value1);
        String val2 = runtime.toString(value2);
        String result = match(val1, val2);
        return runtime.createString(result.toString());
    }
}

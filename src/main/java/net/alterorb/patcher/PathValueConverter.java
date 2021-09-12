package net.alterorb.patcher;

import joptsimple.ValueConverter;

import java.nio.file.Path;

public class PathValueConverter implements ValueConverter<Path> {

    @Override
    public Path convert(String value) {
        return Path.of(value);
    }

    @Override
    public Class<? extends Path> valueType() {
        return Path.class;
    }

    @Override
    public String valuePattern() {
        return null;
    }
}

package com.example;

import org.jetbrains.annotations.NotNull;

import io.github.duzhaokun123.module.base.ModuleEntry;
import io.github.duzhaokun123.yabr.module.UICategory;
import io.github.duzhaokun123.yabr.module.base.BaseModule;
import io.github.duzhaokun123.yabr.module.base.UIEntry;

@ModuleEntry(
        id = "com.example.ExampleJava"
)
public class ExampleJava extends BaseModule implements UIEntry {
    public static ExampleJava INSTANCE = new ExampleJava();

    @Override
    @NotNull
    public CharSequence getName() {
        return "ExampleJava";
    }

    @Override
    @NotNull
    public CharSequence getDescription() {
        return "example module in java";
    }

    @Override
    @NotNull
    public String getCategory() {
        return UICategory.FUN;
    }

    @Override
    public boolean onLoad() {
        return false;
    }
}

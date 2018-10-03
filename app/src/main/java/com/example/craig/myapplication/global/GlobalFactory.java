package com.example.craig.myapplication.global;

public final class GlobalFactory
{
    private GlobalFactory() {}

    public static Global make()
    {
        return global;
    }

    private final static Global global = new Global();
}

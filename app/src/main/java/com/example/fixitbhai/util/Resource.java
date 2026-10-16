package com.example.fixitbhai.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic wrapper class representing UI state (Loading, Success, Error, Empty).
 * Encapsulates data payload and error messages across architecture layers.
 */
public class Resource<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }

    @NonNull
    private final Status status;

    @Nullable
    private final T data;

    @Nullable
    private final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    @NonNull
    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    @NonNull
    public static <T> Resource<T> success(@Nullable T data) {
        if (data == null) {
            return new Resource<>(Status.EMPTY, null, null);
        }
        return new Resource<>(Status.SUCCESS, data, null);
    }

    @NonNull
    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    @NonNull
    public static <T> Resource<T> empty() {
        return new Resource<>(Status.EMPTY, null, null);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
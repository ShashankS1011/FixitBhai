package com.example.fixitbhai.util;

import androidx.annotation.Nullable;

/**
 * Wrapper for LiveData events that are emitted only once (e.g. Navigation, Toasts, Snackbars).
 * Prevents single-time events from re-firing upon configuration changes like screen rotation.
 */
public class Event<T> {

    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content if it hasn't been handled yet, otherwise returns null.
     */
    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it has already been handled.
     */
    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}
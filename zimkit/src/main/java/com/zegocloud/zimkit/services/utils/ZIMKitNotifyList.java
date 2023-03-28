package com.zegocloud.zimkit.services.utils;

import androidx.core.util.Consumer;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZIMKitNotifyList<T> {

    private final CopyOnWriteArrayList<WeakReference<T>> weakRefList;
    private final CopyOnWriteArrayList<T> list;

    public ZIMKitNotifyList() {
        weakRefList = new CopyOnWriteArrayList<>();
        list = new CopyOnWriteArrayList<>();
    }

    public void addListener(T t) {
        addListener(t, false);
    }

    public void addWeakRefListener(T t) {
        addListener(t, true);
    }

    public void addListener(T t, boolean weakRef) {
        if (weakRef) {
            WeakReference<T> reference = new WeakReference<>(t);
            weakRefList.add(reference);
        } else {
            list.add(t);
        }
    }

    public void removeListener(T t) {
        removeListener(t, false);
    }

    public void removeWeakRefListener(T t) {
        removeListener(t, true);
    }

    public void removeListener(T t, boolean weakRef) {
        if (weakRef) {
            for (WeakReference<T> reference : weakRefList) {
                if (Objects.equals(reference.get(), t)) {
                    weakRefList.remove(reference);
                    break;
                }
            }
        } else {
            list.remove(t);
        }
    }

    public void notifyAllListener(Consumer<T> notifier) {

        for (WeakReference<T> reference : weakRefList) {
            T t = reference.get();
            if (t != null) {
                notifier.accept(t);
            }
        }
        for (T t : list) {
            notifier.accept(t);
        }
    }

    public void clear() {
        list.clear();
        for (WeakReference<T> reference : weakRefList) {
            reference.clear();
        }
        weakRefList.clear();
    }

}

package com.ittybitty.locator.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markgarab on 18/04/2016.
 */
public class Result<T> {

    public final List<Throwable> errors;

    public final T value;

    public Result(T value, List<Throwable> errors){
        this.value = value;
        this.errors = errors;
    }

    public Result(T value, Throwable error){
        this.value = value;
        if(error != null){
            this.errors = new ArrayList<Throwable>();
            this.errors.add(error);
        } else {
            this.errors = null;
        }
    }

    public Result(T value){
        this.value = value;
        this.errors = null;
    }

    public Result(Throwable error){
        this(null, error);
    }

    public Result(List<Throwable> errors){
        this(null, errors);
    }

}

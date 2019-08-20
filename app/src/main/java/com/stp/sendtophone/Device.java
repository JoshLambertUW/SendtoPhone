package com.stp.sendtophone;

import androidx.annotation.Nullable;

public class Device {
    private String name;
    private String id;

    public Device(){
    }

    public Device(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Device){
            Device c = (Device )obj;
            return c.getName().equals(name) && c.getId() == id;
        }

        return false;
    }
}

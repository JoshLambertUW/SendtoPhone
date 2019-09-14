package com.stp.sendtophone;

public class Message {
    private String body;
    private Device selectedDevice;
    private String error;

    public Message(){
    }

    public Message(String body) {
        this.body = body;
    }

    public Message(String body, Device selectedDevice) {
        this.body = body;
        this.selectedDevice = selectedDevice;
    }

    public Message(String body, Device selectedDevice, String error) {
        this.body = body;
        this.selectedDevice = selectedDevice;
        this.error = error;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Device getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(Device selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return body;
    }
}

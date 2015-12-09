package com.itraveller;

/**
 * Created by rohan bundelkhandi on 11/29/2015.
 */
public  class UserData {

    //private variables
    int _id;
    String time_stamp;
    String _imei;
    String _name;
    String _message;
    String _to_imei;
    String _receiver_name;
    String _email;
    // Empty constructor
    public UserData(){

    }
    // constructor
    public UserData(int id, String name, String imei, String message,String time_stamp){
        this._id      = id;
        this._imei    = imei;
        this._name    = name;
        this._message = message;
        this.time_stamp=time_stamp;

    }

    // constructor
    public UserData(int id, String imei, String name, String message,String _to_imei,String _receiver_name){
        this._id      = id;
        this._imei    = imei;
        this._name    = name;
        this._message = message;
        this._to_imei=_to_imei;
        this._receiver_name=_receiver_name;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }


    // setting id
    public void setTime(String time_stamp){
        this.time_stamp = time_stamp;
    }

    public String getTime(){
        return this.time_stamp;
    }


    // getting ID
    public String getEmail(){
        return this._email;
    }

    // setting id
    public void setEmail(String _email){
        this._email = _email;
    }

    // getting imei
    public String getIMEI(){
        return this._imei;
    }

    // setting imei
    public void setIMEI(String imei){
        this._imei = imei;
    }


    // getting imei
    public String getToIMEI(){
        return this._imei;
    }

    // setting imei
    public void setToIMEI(String imei){
        this._imei = imei;
    }


    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }


    // getting name
    public String getReceiverName(){
        return this._receiver_name;
    }

    // setting name
    public void setReceiverName(String _receiver_name){
        this._receiver_name = _receiver_name;
    }

    // getting Message
    public String getMessage(){
        return this._message;
    }

    // setting Message
    public void setMessage(String message){
        this._message = message;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserInfo [name=" + _name + "]";
    }

}

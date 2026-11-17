package com.back.global.rsData;

// record : 데이터를 담기만 하는 불변 클래스
public record RsData<T>(String resultCode, String msg, T data) {


    // data가 없는 경우를 위한 보조생성자
    public RsData(String resultCode, String msg){
        this(resultCode,msg,null);
    }

}

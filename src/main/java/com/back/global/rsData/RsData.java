package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

// record : 데이터를 담기만 하는 불변 클래스
public record RsData<T>(String resultCode, String msg, T data) {


    // data가 없는 경우를 위한 보조생성자
    public RsData(String resultCode, String msg){
        this(resultCode,msg,null);
    }

    // "201-1" → 201 : resultCode 앞부분을 HTTP 상태코드로 써요
    // JSON 응답에는 이미 resultCode가 있으니까 중복으로 내보내지 않아요
    @JsonIgnore
    public int statusCode() {
        return Integer.parseInt(resultCode.split("-", 2)[0]);
    }

}

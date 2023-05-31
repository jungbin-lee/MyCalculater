package com.h2square.mycalculater

enum class CalculateAction(val info:String,val symbol:String) {
    Plus("더하기","+"),//더하기
    Minus("뺴기","-"),//뺴기
    Divide("나누기","÷"),//나누기
    Multiply("곱하기","x"),//곱하기
    AllClear("모두삭제","AC"),//모두삭제
    Del("지우기","<-"),//지우기
    Calcultae("계산하기","=")//계산하기
}